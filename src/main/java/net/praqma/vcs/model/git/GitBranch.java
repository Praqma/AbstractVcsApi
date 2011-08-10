package net.praqma.vcs.model.git;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.Repository;
import net.praqma.vcs.util.CommandLine;

public class GitBranch extends AbstractBranch{

	public GitBranch( File localRepositoryPath, String name ) {
		super( localRepositoryPath, name );
	}
	
	public GitBranch( File localRepositoryPath, String name, Repository parent ) {
		super( localRepositoryPath, name, parent );
	}

	public static GitBranch create( File localRepository, String name, Repository parent ) {
		GitBranch gb = new GitBranch( localRepository, name, parent );
		gb.initialize();
		return gb;
	}
	
	public static GitBranch get( File localRepository, String name, Repository parent ) {
		GitBranch gb = new GitBranch( localRepository, name, parent );
		gb.initialize();
		return gb;
	}
	
	public boolean initialize() {
		return doInitialize( new InitializeImpl() );
	}
	
	private class InitializeImpl extends Initialize {
		public boolean initialize() {
			String cmd = "git branch " + name + " .";
			CommandLine.run( cmd, localRepositoryPath.getAbsoluteFile() );
			
			return true;
		}
		
	}

	public void pull() {
		PullImpl pull = new PullImpl();
		doPull( pull );
	}
	
	public class PullImpl extends Pull {


		public boolean pull() {
			logger.debug( "GIT: perform pull" );
			
			if( parent == null ) {
				System.err.println( "GIT: Could not pull a null branch" );
				return false;
			}
			
			String cmd = "git pull " + parent;
			CommandLine.run( cmd, localRepositoryPath );
			
			return true;
		}
	}
	
	@Override
	public List<AbstractCommit> getCommits() {
		return getCommits(false);
	}
	
	@Override
	public List<AbstractCommit> getCommits( boolean load ) {
		logger.info( "Pulling git branch " + name );
		
		String cmd = "git rev-list --all";
		List<String> cs = CommandLine.run( cmd, localRepositoryPath.getAbsoluteFile() ).stdoutList;
		
		List<AbstractCommit> commits = new ArrayList<AbstractCommit>();
		
		for(String c : cs) {
			GitCommit commit = new GitCommit(c, GitBranch.this);
			if( load ) {
				commit.load();
			}
			
			commits.add( commit );
		}
		
		Collections.reverse( commits );
		
		return commits;
	}
}
