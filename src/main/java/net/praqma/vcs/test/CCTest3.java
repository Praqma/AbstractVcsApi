package net.praqma.vcs.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
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
import net.praqma.vcs.model.clearcase.ClearcaseBranch;
import net.praqma.vcs.model.clearcase.ClearcaseReplay;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.OperationNotImplementedException;
import net.praqma.vcs.model.exceptions.OperationNotSupportedException;
import net.praqma.vcs.model.exceptions.UnableToPerformException;
import net.praqma.vcs.model.exceptions.UnableToReplayException;
import net.praqma.vcs.model.git.GitBranch;

public class CCTest3 {

	static net.praqma.util.debug.Logger logger = net.praqma.util.debug.Logger.getLogger();
	
	public static void main( String[] args ) throws UnableToPerformException, URISyntaxException, OperationNotSupportedException, MalformedURLException, OperationNotImplementedException, UnableToReplayException, UCMException, ElementDoesNotExistException, ElementNotCreatedException {
		
		logger.toStdOut( true );
		new AVA();
		
		File gitpath = new File( "c:\\temp\\git_tests\\repo1" );
		
		//Repository parent = new Repository( "git@github.com:Praqma/MonKit.git", "origin" );
		File parent = new File( "file://C:/projects/monkit/branches", "origin" );
		//GitSCM.create( new File( "c:\\temp\\git_tests\\repo1" ) );
		
		//GitSCM git = new GitSCM( parent, path, new GitBranch( "master" ) );
		//GitBranch branch = new GitBranch( gitpath, "master" );
		GitBranch branch = new GitBranch( new File( "C:/projects/monkit/branches" ), "master" );
		//GitVCS git = GitVCS.create( branch );
		//branch.pull();
		
		List<AbstractCommit> commits = branch.getCommits(true);
		
		logger.info( commits.size() + " commits on branch " + branch );
		
		logger.info( "Commit #1: " + commits.get( 0 ) );
		
		
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		/* Setup the logger */
        Logger logger = PraqmaLogger.getLogger(false);
        logger.subscribeAll();
        logger.setLocalLog( new File( "gittest.log") );
        Cool.setLogger(logger);
        
        /*
        String viewtag = "OpenSCM_test";
        PVob pvob = new PVob( "\\" + vname + "_PVOB" );
		Stream intStream = UCMEntity.getStream( "Development_int", pvob, true );
		Baseline baseline = UCMEntity.getBaseline( "Structure_1_0", pvob, true );
		Component component = UCMEntity.getComponent( cname, pvob, true );
		*/
		
		String append = "001";
		File path = new File( "C:/Temp/views/" + append );
		String vname = "Wolle003";
		String cname = "Core";
		String viewtag = vname + "_" + append;
		
		Vob vob = new Vob( "\\" + vname );
		PVob pvob = new PVob( "\\" + vname + "_PVOB" );
		Stream parentStream = UCMEntity.getStream( "Development_int", pvob, true );
		Baseline baseline = UCMEntity.getBaseline( "Structure_1_0", pvob, true );
		Component component = UCMEntity.getComponent( cname, pvob, true );
		
		String name = vname + "_" + append;
		
		/*
		ClearcaseBranch ccbranch = new ClearcaseBranch( vob, pvob, parentStream, baseline, path, viewtag, name );
		ccbranch.get();
		ccbranch.pull();
		
		File devview = new File( path, vname + "/" + cname );
		
		
		
		ClearcaseReplay cr = new ClearcaseReplay( ccbranch );
		cr.replay( commits.get( 0 ) );
		cr.replay( commits.get( 1 ) );
		cr.replay( commits.get( 2 ) );
		cr.replay( commits.get( 3 ) );
		cr.replay( commits.get( 4 ) );
		*/
	}

}
