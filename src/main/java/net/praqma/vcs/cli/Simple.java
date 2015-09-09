package net.praqma.vcs.cli;

import java.io.File;
import java.io.IOException;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.StreamAppender;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;
import net.praqma.vcs.AVA;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractReplay;
import net.praqma.vcs.model.exceptions.ElementAlreadyExistsException;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.UnableToCheckoutCommitException;
import net.praqma.vcs.model.exceptions.UnableToReplayException;
import net.praqma.vcs.model.exceptions.UnsupportedBranchException;
import net.praqma.vcs.persistence.XMLStrategy;
import net.praqma.vcs.util.Cycle;
import net.praqma.vcs.util.configuration.AbstractConfigurationReader;
import net.praqma.vcs.util.configuration.Configuration;
import net.praqma.vcs.util.configuration.exception.ConfigurationDoesNotExistException;
import net.praqma.vcs.util.configuration.exception.ConfigurationException;

public class Simple {
	private static Logger logger = Logger.getLogger();
	private static StreamAppender app = new StreamAppender( System.out );
	
    static class MyShutdown extends Thread {
        public void run() {
            System.out.println( "Terminating AVA process" );
        }
    }
	
	public static void main(String[] args) throws IOException, ElementNotCreatedException, ElementAlreadyExistsException, ElementDoesNotExistException, UnableToCheckoutCommitException, UnableToReplayException, ConfigurationDoesNotExistException, ConfigurationException, UnsupportedBranchException, InterruptedException {
		
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
		//UCM.setContext( UCM.ContextType.CLEARTOOL );
		
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
