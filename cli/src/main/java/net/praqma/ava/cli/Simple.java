package net.praqma.ava.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.FileAppender;
import net.praqma.util.debug.appenders.StreamAppender;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;
import net.praqma.ava.AVA;
import net.praqma.ava.model.AbstractBranch;
import net.praqma.ava.model.AbstractCommit;
import net.praqma.ava.model.AbstractReplay;
import net.praqma.ava.model.clearcase.ClearCaseBranch;
import net.praqma.ava.model.clearcase.ClearCaseReplay;
import net.praqma.ava.model.clearcase.ClearCaseVCS;
import net.praqma.ava.model.exceptions.ElementAlreadyExistsException;
import net.praqma.ava.model.exceptions.ElementDoesNotExistException;
import net.praqma.ava.model.exceptions.ElementNotCreatedException;
import net.praqma.ava.model.exceptions.UnableToCheckoutCommitException;
import net.praqma.ava.model.exceptions.UnableToReplayException;
import net.praqma.ava.model.exceptions.UnsupportedBranchException;
import net.praqma.ava.model.git.GitBranch;
import net.praqma.ava.model.git.GitReplay;
import net.praqma.ava.persistence.XMLStrategy;
import net.praqma.ava.util.Cycle;
import net.praqma.ava.util.configuration.AbstractConfigurationReader;
import net.praqma.ava.util.configuration.Configuration;
import net.praqma.ava.util.configuration.exception.ConfigurationDoesNotExistException;
import net.praqma.ava.util.configuration.exception.ConfigurationException;

public class Simple {
	private static Logger logger = Logger.getLogger();
	private static StreamAppender app = new StreamAppender( System.out );
	
    static class MyShutdown extends Thread {
        public void run() {
            System.out.println( "Terminating AVA process" );
        }
    }
	
	public static void main(String[] args) throws IOException, UCMException, ElementNotCreatedException, ElementAlreadyExistsException, ElementDoesNotExistException, UnableToCheckoutCommitException, UnableToReplayException, ConfigurationDoesNotExistException, ConfigurationException, UnsupportedBranchException, InterruptedException {
		
        Options o = new Options( "1.0.0" );

        Option oconfig = new Option( "config", "c", true, 1, "Path to configuration" );
        Option opreview = new Option( "preview", "p", false, 0, "Preview configuration" );
        
        Option ointeractive = new Option( "interactive", "i", false, 0, "Interactive" );
        Option ointerval = new Option( "interval", "I", false, 1, "Interval" );
        
        o.setOption( oconfig );
        o.setOption( opreview );
        o.setOption( ointeractive );
        o.setOption( ointerval );
        
        app.setTemplate( "[%level]%space %message%newline" );
        Logger.addAppender( app );
        
        o.setDefaultOptions();
        
        o.parse( args );
        
        if( o.isVerbose() ) {
        	app.setMinimumLevel( LogLevel.DEBUG );
        } else {
        	app.setMinimumLevel( LogLevel.INFO );
        }
        

        try {
            o.checkOptions();
        } catch( Exception e ) {
        	logger.error( "Incorrect option: " + e.getMessage() );
            o.display();
            System.exit( 1 );
        }

		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		File p = new File( "ava.xml" );
		p.createNewFile();
		new AVA( new XMLStrategy( p ) );
		
		Integer interval = null;
		if( ointeractive.isUsed() && ointerval.isUsed() ) {
			System.err.println( "Interactive and interval cannot be used at the same time" );
			System.exit( 1 );
		}
		
		if( ointeractive.isUsed() ) {
			logger.debug( "Interactive mode selected" );
			interval = 0;
		}
		
		if( ointerval.isUsed() ) {
			interval = ointerval.getInteger();
			logger.debug( "Cycles of " + interval + " is used" );
		}
		
		Configuration config = AbstractConfigurationReader.getConfiguration( new File( oconfig.getString() ) );
		
		logger.debug( "Preview is " + opreview.isUsed() );
		if( opreview.isUsed() ) {
			logger.info( config.toString() );
			return;
		}
		
		AbstractBranch source = config.getSourceConfiguration().getBranch();
		AbstractReplay replay = config.getTargetConfiguration().getReplay();
		
        Cycle.cycle( source, replay, interval );
	}
}
