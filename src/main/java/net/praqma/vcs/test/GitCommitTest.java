package net.praqma.vcs.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.util.Utilities;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;
import net.praqma.vcs.AVA;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.Repository;
import net.praqma.vcs.model.clearcase.ClearcaseReplay;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.OperationNotImplementedException;
import net.praqma.vcs.model.exceptions.OperationNotSupportedException;
import net.praqma.vcs.model.exceptions.UnableToPerformException;
import net.praqma.vcs.model.exceptions.UnableToReplayException;
import net.praqma.vcs.model.git.GitBranch;
import net.praqma.vcs.model.git.GitVCS;

public class GitCommitTest {

	static net.praqma.util.debug.Logger logger = net.praqma.util.debug.Logger.getLogger();
	public static void main( String[] args ) throws UnableToPerformException, URISyntaxException, OperationNotSupportedException, MalformedURLException, OperationNotImplementedException, UnableToReplayException, UCMException, ElementNotCreatedException {
		
		logger.toStdOut( true );
		new AVA();
		
        File view = new File( args[0] );
        String vname = args[1];
        String cname = args[2];
		
		File gitpath = new File( "c:\\temp\\git_tests\\repo1" );
		
		//Repository parent = new Repository( "git@github.com:Praqma/MonKit.git", "origin" );
		File parent = new File( "file://C:/projects/monkit/branches", "origin" );
		//GitSCM.create( new File( "c:\\temp\\git_tests\\repo1" ) );
		
		//GitSCM git = new GitSCM( parent, path, new GitBranch( "master" ) );
		GitBranch branch = new GitBranch( gitpath, "master" );
		//GitVCS git = GitVCS.create( branch );
		branch.update();
		
		List<AbstractCommit> commits = branch.getCommits();
		
		logger.info( commits.size() + " commits on branch " + branch );
		
		commits.get( 0 ).load();
		commits.get( 1 ).load();
		commits.get( 2 ).load();
		commits.get( 3 ).load();
		commits.get( 4 ).load();

		logger.info( "Commit #1: " + commits.get( 90 ) );
		
		
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		/* Setup the logger */
        Logger logger = PraqmaLogger.getLogger(false);
        logger.subscribeAll();
        logger.setLocalLog( new File( "gittest.log") );
        Cool.setLogger(logger);
        
        String viewtag = "OpenSCM_test";
        PVob pvob = new PVob( "\\" + vname + "_PVOB" );
		Stream intStream = UCMEntity.getStream( "Development_int", pvob, true );
		Baseline baseline = UCMEntity.getBaseline( "Structure_1_0", pvob, true );
		Component component = UCMEntity.getComponent( cname, pvob, true );
		
        SnapshotView sview = null;
		if( !UCMView.viewExists( viewtag ) ) {
			sview = Utilities.CreateView("stream:OpenSCM_dev" + "@" + pvob, intStream, baseline, view, viewtag );
		} else {
			sview = UCMView.getSnapshotView(view);	
		}
		
		File devview = new File( view, vname + "/" + cname );
		
		
		/*
		ClearcaseReplay cr = new ClearcaseReplay( devview, sview, component, pvob );
		cr.replay( commits.get( 0 ) );
		cr.replay( commits.get( 1 ) );
		cr.replay( commits.get( 2 ) );
		cr.replay( commits.get( 3 ) );
		cr.replay( commits.get( 4 ) );
		*/
	}

}
