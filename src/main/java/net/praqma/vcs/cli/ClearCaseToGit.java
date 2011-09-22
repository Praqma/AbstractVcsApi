package net.praqma.vcs.cli;

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
import net.praqma.util.debug.appenders.StreamAppender;
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

public class ClearCaseToGit {
	private static Logger logger = Logger.getLogger();
	private static StreamAppender app = new StreamAppender( System.out );
	
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
        Option ostreamname = new Option( "stream", "S", true, 1, "Parent stream name" );
        Option ochildstreamname = new Option( "childstream", "s", true, 1, "Child stream name" );
        Option obaselinename = new Option( "baseline", "b", true, 1, "Foundation Baseline name for child stream" );
        Option ogit = new Option( "git", "g", true, 1, "Path to git repo" );
        
        Option ointeractive = new Option( "interactive", "i", false, 1, "Interactive" );
        Option ointerval = new Option( "interval", "I", false, 1, "Interval" );
        
        o.setOption( oview );
        o.setOption( obaselinename );
        o.setOption( ovobname );
        o.setOption( ostreamname );
        o.setOption( ochildstreamname );
        o.setOption( ogit );
        o.setOption( oviewtag );
        o.setOption( ointeractive );
        o.setOption( ointerval );
        
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
		PVob pvob = ClearcaseVCS.bootstrap();
		
		Baseline baseline = UCMEntity.getBaseline( obaselinename.getString(), pvob, false );
		
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
        
        boolean interactive = ointeractive.isUsed();
        int interval = ointerval.getInteger();
		
		while( true ) {
			if( now != null ) {
				logger.info( "Getting commits after " + now );
			}
			
			logger.info( "Getting ClearCase commits" );
			List<AbstractCommit> cccommits = ccbranch.getCommits(true, now);
			for( AbstractCommit commit : cccommits ) {
				ccbranch.checkoutCommit( commit );
				gr.replay( commit );
			}
			
			now = new Date();
			
	        logger.info( "Press any key to continue" );
	        
	        String s = stdin.readLine();
		}
	}
}
