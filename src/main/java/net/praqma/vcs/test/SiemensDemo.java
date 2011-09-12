package net.praqma.vcs.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.praqma.clearcase.Cool;
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
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;
import net.praqma.vcs.AVA;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.clearcase.ClearcaseBranch;
import net.praqma.vcs.model.clearcase.ClearcaseReplay;
import net.praqma.vcs.model.clearcase.ClearcaseVCS;
import net.praqma.vcs.model.exceptions.ElementAlreadyExistsException;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.UnableToCheckoutCommitException;
import net.praqma.vcs.model.exceptions.UnableToReplayException;
import net.praqma.vcs.model.git.GitBranch;
import net.praqma.vcs.model.git.GitReplay;


/**
 * INIT:<br>
 * java -classpath ava.jar net.praqma.vcs.test.SiemensDemo -w c:\Temp\views\siemens_out\001 -t siemens_out_view_tag -o Siemens -c Siemens -S Siemens_int -p siemens -g c:\projects\other\Siemens -V
 * java -classpath ava.jar net.praqma.vcs.test.SiemensDemo -w c:\Temp\views\siemens_in\001 -t siemens_in_view_tag -o Siemens -c Siemens -S Siemens_int -p siemens -g c:\projects\other\Siemens -V
 * <br>
 * 
 * RUN:<br>
 * java -classpath ava.jar net.praqma.vcs.test.SiemensDemo -G -w c:\Temp\views\siemens_out\001 -t siemens_out_view_tag -o Siemens -b Siemens_Structure_1_0 -S Siemens_int -s siemens_out_dev -g c:\projects\other\Siemens -V
 * java -classpath ava.jar net.praqma.vcs.test.SiemensDemo -w c:\Temp\views\siemens_in\001 -t siemens_in_view_tag -o Siemens -b Siemens_Structure_1_0 -S Siemens_int -s siemens_in_dev -g c:\projects\other\Siemens -V
 * 
 * 
 * @author wolfgang
 *
 */
public class SiemensDemo {
	private static Logger logger = Logger.getLogger();
	
    static class MyShutdown extends Thread {
        public void run() {
            System.out.println( "Terminating AVA process" );
        }
    }
	
	public static void main(String[] args) throws IOException, UCMException, ElementNotCreatedException, ElementAlreadyExistsException, ElementDoesNotExistException, UnableToCheckoutCommitException, UnableToReplayException {
		
        Options o = new Options( "1.0.0" );

        Option oview = new Option( "view", "w", true, 1, "path to ClearCase view" );
        Option oviewtag = new Option( "viewtag", "t", true, 1, "Viewtag name" );
        Option ovobname = new Option( "vob", "o", true, 1, "Vob name" );
        Option ocomponent = new Option( "component", "c", false, 1, "Component name" );
        Option ostreamname = new Option( "stream", "S", true, 1, "Parent stream name" );
        Option ochildstreamname = new Option( "childstream", "s", false, 1, "Child stream name" );
        Option oprojectname = new Option( "project", "p", false, 1, "Project name" );
        Option obaselinename = new Option( "baseline", "b", false, 1, "Foundation Baseline name" );
        Option otogit = new Option( "togit", "G", false, 0, "The to Git solution" );
        Option ogit = new Option( "git", "g", true, 1, "Path to git repo" );
        
        o.setOption( oview );
        o.setOption( obaselinename );
        o.setOption( ovobname );
        o.setOption( ocomponent );
        o.setOption( ostreamname );
        o.setOption( ochildstreamname );
        o.setOption( oprojectname );
        o.setOption( ogit );
        o.setOption( oviewtag );
        o.setOption( otogit );
        
        o.setDefaultOptions();
        
        o.parse( args );
        
        logger.toStdOut( true );

        try {
            o.checkOptions();
        } catch( Exception e ) {
        	logger.error( "Incorrect option: " + e.getMessage() );
            o.display();
            System.exit( 1 );
        }

        if( o.isVerbose() ) {
        	logger.setMinLogLevel( LogLevel.DEBUG );
        } else {
        	logger.setMinLogLevel( LogLevel.INFO );
        }
        
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		net.praqma.util.debug.PraqmaLogger.Logger plogger = net.praqma.util.debug.PraqmaLogger.getLogger();
		plogger.subscribeAll();
		plogger.setLocalLog(new File("demo.log"));
		Cool.setLogger( plogger );
		
		new AVA();
		
		/* Setup ClearCase */
		PVob pvob = ClearcaseVCS.bootstrap();
		
		Baseline baseline = null;
		if( ocomponent.isUsed() ) {
			ClearcaseVCS cc = new ClearcaseVCS( null, ovobname.getString(), ocomponent.getString(), oprojectname.getString(), ostreamname.getString(), 
	                Project.POLICY_INTERPROJECT_DELIVER  | 
	                Project.POLICY_CHSTREAM_UNRESTRICTED | 
	                Project.POLICY_DELIVER_NCO_DEVSTR, pvob );
			
			cc.get(true);
			logger.info( "Clearcase initialized" );
			return;
			
		} else {
			baseline = UCMEntity.getBaseline( obaselinename.getString(), pvob, false );
		}
		
		if( !ochildstreamname.isUsed() ) {
			logger.error( "The child stream must be given" );
			System.exit( 1 );
		}
		
		Vob vob = new Vob( "\\" + ovobname.getString() );
		Stream stream = UCMEntity.getStream( ostreamname.getString(), pvob, false );
		
		ClearcaseBranch ccbranch = new ClearcaseBranch( pvob, vob, stream, baseline, new File( oview.getString() ), oviewtag.getString(), ochildstreamname.getString() );
		ccbranch.initialize(true);
		ccbranch.update();
		
		/* Setup Git */
		GitBranch gitbranch = new GitBranch( new File( ogit.getString() ), "master" );
		gitbranch.update();
		
        MyShutdown sh = new MyShutdown();
        Runtime.getRuntime().addShutdownHook(sh);
        
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        Date now = null;
        
        GitReplay gr = new GitReplay( gitbranch );
        ClearcaseReplay cr = new ClearcaseReplay( ccbranch );
		
		while( true ) {
			if( now != null ) {
				logger.info( "Getting baselines after " + now );
			}
			
			/* To Git solution */
			if( otogit.isUsed() ) {
				logger.info( "Getting ClearCase commits" );
				List<AbstractCommit> cccommits = ccbranch.getCommits(true, now);
				for( AbstractCommit commit : cccommits ) {
					ccbranch.checkoutCommit( commit );
					gr.replay( commit );
				}
			/* To clearcase solution */
			} else {
				logger.info( "Getting Git commits" );
				List<AbstractCommit> gitcommits = gitbranch.getCommits(true, now);
				for( AbstractCommit commit : gitcommits ) {
					gitbranch.checkoutCommit( commit );
					cr.replay( commit );
				}
			}
			
			now = new Date();
			
	        logger.info( "Press any key to continue" );
	        
	        String s = stdin.readLine();
		}
	}
}
