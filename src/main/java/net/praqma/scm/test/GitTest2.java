package net.praqma.scm.test;

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
import net.praqma.exceptions.OperationNotImplementedException;
import net.praqma.exceptions.OperationNotSupportedException;
import net.praqma.exceptions.UnableToPerformException;
import net.praqma.exceptions.UnableToReplayException;
import net.praqma.scm.clearcase.ClearcaseReplay;
import net.praqma.scm.model.AbstractCommit;
import net.praqma.scm.model.Repository;
import net.praqma.scm.model.git.GitBranch;
import net.praqma.scm.model.git.GitSCM;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;

public class GitTest2 {

	public static void main( String[] args ) throws UnableToPerformException, URISyntaxException, OperationNotSupportedException, MalformedURLException, OperationNotImplementedException, UnableToReplayException, UCMException {
		
        File view = new File( args[0] );
        String vname = args[1];
        String cname = args[2];
		
		File gitpath = new File( "c:\\temp\\git_tests\\repo1" );
		
		//Repository parent = new Repository( "git@github.com:Praqma/MonKit.git", "origin" );
		Repository parent = new Repository( "file://C:/projects/monkit", "origin" );
		//GitSCM.create( new File( "c:\\temp\\git_tests\\repo1" ) );
		
		//GitSCM git = new GitSCM( parent, path, new GitBranch( "master" ) );
		GitBranch branch = new GitBranch( gitpath, "master" );
		GitSCM git = GitSCM.create( branch );
		git.pull(parent);
		
		List<AbstractCommit> commits = git.getCommits();
		
		System.out.println( commits.size() + " commits on branch " + branch );
		
		commits.get( 0 ).load();
		commits.get( 1 ).load();
		commits.get( 2 ).load();
		commits.get( 3 ).load();
		commits.get( 4 ).load();

		System.out.println( "Commit #1: " + commits.get( 90 ) );
		
		
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
		if( !UCMView.ViewExists( viewtag ) ) {
			sview = Utilities.CreateView("stream:OpenSCM_dev" + "@" + pvob, intStream, baseline, view, viewtag );
		} else {
			sview = UCMView.GetSnapshotView(view);	
		}
		
		File devview = new File( view, vname + "/" + cname );
		
		
		
		ClearcaseReplay cr = new ClearcaseReplay( devview, sview, component, pvob );
		cr.replay( commits.get( 0 ) );
		cr.replay( commits.get( 1 ) );
		cr.replay( commits.get( 2 ) );
		cr.replay( commits.get( 3 ) );
		cr.replay( commits.get( 4 ) );
	}

}
