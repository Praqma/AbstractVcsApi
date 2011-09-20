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
import net.praqma.util.debug.Logger.LogLevel;
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

public class GitTest2 {

	static net.praqma.util.debug.Logger logger = net.praqma.util.debug.Logger.getLogger();
	public static void main( String[] args ) throws UnableToPerformException, URISyntaxException, OperationNotSupportedException, MalformedURLException, OperationNotImplementedException, UnableToReplayException, UCMException, ElementNotCreatedException {
		
		new AVA();

		GitBranch branch = new GitBranch( new File( "c:/projects/monkit/branches" ), "master" );
		
		List<AbstractCommit> commits = branch.getCommits( true );
		
		logger.info( commits.size() + " commits on branch " + branch );
		
		for( AbstractCommit c : commits ) {
			logger.info( "Commit #" + c.getNumber() + ":\n" + c );
		}
	}

}
