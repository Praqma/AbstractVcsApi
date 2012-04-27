package net.praqma.ava.cli;

import java.io.IOException;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.StreamAppender;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;
import net.praqma.ava.AVA;
import net.praqma.ava.model.clearcase.ClearCaseVCS;
import net.praqma.ava.model.exceptions.ElementAlreadyExistsException;
import net.praqma.ava.model.exceptions.ElementDoesNotExistException;
import net.praqma.ava.model.exceptions.ElementNotCreatedException;
import net.praqma.ava.model.exceptions.UnableToCheckoutCommitException;
import net.praqma.ava.model.exceptions.UnableToReplayException;

public class CreateClearCaseRepository {
	private static Logger logger = Logger.getLogger();
	private static StreamAppender app = new StreamAppender( System.out );
	
    static class MyShutdown extends Thread {
        public void run() {
            System.out.println( "Terminating AVA process" );
        }
    }
	
	public static void main(String[] args) throws IOException, UCMException, ElementNotCreatedException, ElementAlreadyExistsException, ElementDoesNotExistException, UnableToCheckoutCommitException, UnableToReplayException {
		
        Options o = new Options( "1.0.0" );

        Option ovobname = new Option( "vob", "o", true, 1, "Vob name" );
        Option ocomponent = new Option( "component", "c", true, 1, "Component name" );
        Option ostreamname = new Option( "stream", "s", true, 1, "Parent stream name" );
        Option oprojectname = new Option( "project", "p", true, 1, "Project name" );
        
        o.setOption( ovobname );
        o.setOption( ocomponent );
        o.setOption( ostreamname );
        o.setOption( oprojectname );
        
        o.setDefaultOptions();
        
        o.parse( args );
        
        Logger.addAppender( app );

        try {
            o.checkOptions();
        } catch( Exception e ) {
        	logger.error( "Incorrect option: " + e.getMessage() );
            o.display();
            System.exit( 1 );
        }

        if( o.isVerbose() ) {
        	app.setMinimumLevel( LogLevel.DEBUG );
        } else {
        	app.setMinimumLevel( LogLevel.INFO );
        }
        
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		new AVA( null );
		
		/* Setup ClearCase */
		PVob pvob = ClearCaseVCS.bootstrap();
		
		ClearCaseVCS cc = new ClearCaseVCS( null, ovobname.getString(), ocomponent.getString(), oprojectname.getString(), ostreamname.getString(), 
                Project.POLICY_INTERPROJECT_DELIVER  | 
                Project.POLICY_CHSTREAM_UNRESTRICTED | 
                Project.POLICY_DELIVER_NCO_DEVSTR, pvob );
		
		cc.get(true);
		logger.info( "Clearcase initialized" );
	}
}
