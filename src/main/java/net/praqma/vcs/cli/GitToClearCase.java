package net.praqma.vcs.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.StreamAppender;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;
import net.praqma.vcs.AVA;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.clearcase.ClearCaseBranch;
import net.praqma.vcs.model.clearcase.ClearCaseReplay;
import net.praqma.vcs.model.clearcase.ClearCaseVCS;
import net.praqma.vcs.model.exceptions.ElementAlreadyExistsException;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.UnableToCheckoutCommitException;
import net.praqma.vcs.model.exceptions.UnableToReplayException;
import net.praqma.vcs.model.git.GitBranch;
import net.praqma.vcs.persistence.XMLStrategy;

public class GitToClearCase {
	private static Logger logger = Logger.getLogger();
	private static StreamAppender app = new StreamAppender( System.out );
	
    static class MyShutdown extends Thread {
        public void run() {
            System.out.println( "Terminating AVA process" );
        }
    }
	
	public static void main(String[] args) throws IOException, ElementNotCreatedException, UnableToInitializeEntityException, ElementAlreadyExistsException, ElementDoesNotExistException, UnableToCheckoutCommitException, UnableToReplayException, UnableToInitializeEntityException {
		
        Options o = new Options( "1.0.0" );

        Option oview = new Option( "view", "w", true, 1, "path to ClearCase view" );
        Option oviewtag = new Option( "viewtag", "t", true, 1, "Viewtag name" );
        Option ovobname = new Option( "vob", "o", true, 1, "Vob name" );
        Option ostreamname = new Option( "stream", "S", true, 1, "Parent stream name" );
        Option ochildstreamname = new Option( "childstream", "s", false, 1, "Child stream name" );
        Option obaselinename = new Option( "baseline", "b", false, 1, "Foundation Baseline name" );
        Option ogit = new Option( "git", "g", true, 1, "Path to git repo" );
        
        Option ointeractive = new Option( "interactive", "i", false, 1, "Interactive" );
        
        o.setOption( oview );
        o.setOption( obaselinename );
        o.setOption( ovobname );
        o.setOption( ostreamname );
        o.setOption( ochildstreamname );
        o.setOption( ogit );
        o.setOption( oviewtag );
        o.setOption( ointeractive );
        
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
		//UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		new AVA( new XMLStrategy( new File( "ava.xml" ) ) );
		
		/* Setup ClearCase */
		PVob pvob = ClearCaseVCS.bootstrap();
		
		Baseline baseline = Baseline.get( obaselinename.getString(), pvob );
		
		if( !ochildstreamname.isUsed() ) {
			logger.error( "The child stream must be given" );
			System.exit( 1 );
		}
		
		Vob vob = new Vob( "\\" + ovobname.getString() );
		Stream stream = Stream.get( ostreamname.getString(), pvob );
		
		ClearCaseBranch ccbranch = new ClearCaseBranch( pvob, stream, baseline, new File( oview.getString() ), oviewtag.getString(), ochildstreamname.getString() );
		ccbranch.initialize(true);
		ccbranch.update();
		
		/* Setup Git */
		GitBranch gitbranch = new GitBranch( new File( ogit.getString() ), "master" );
		gitbranch.update();
		
        MyShutdown sh = new MyShutdown();
        Runtime.getRuntime().addShutdownHook(sh);
        
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        Date now = null;
        
        ClearCaseReplay cr = new ClearCaseReplay( ccbranch );
        
        boolean interactive = ointeractive.isUsed();
		
		do {
			if( now != null ) {
				logger.info( "Getting commits after " + now );
			}
			

			logger.info( "Getting Git commits" );
			List<AbstractCommit> gitcommits = gitbranch.getCommits(true, now);
			for( AbstractCommit commit : gitcommits ) {
				gitbranch.checkoutCommit( commit );
				cr.replay( commit );
			}
			
			now = new Date();
			
	        logger.info( "Press any key to continue" );
	        
	        String s = stdin.readLine();
		} while( interactive );
	}
}
