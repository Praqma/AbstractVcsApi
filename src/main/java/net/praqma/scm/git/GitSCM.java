package net.praqma.scm.git;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.praqma.scm.AbstractBranch;
import net.praqma.scm.AbstractCommit;
import net.praqma.scm.AbstractSCM;
import net.praqma.scm.Repository;
import net.praqma.scm.util.CommandLine;

public class GitSCM extends AbstractSCM {

	public GitSCM( Repository parent, File localRepositoryPath, AbstractBranch branch ) {
		super( parent, localRepositoryPath, branch );
		
		/*
		String cmd = "git checkout -b " + branch.getName();
		CommandLine.getInstance().run( cmd, localRepositoryPath );
		*/
	}
	
	public static GitSCM create( File localRepositoryPath ) {
		GitSCM git = new GitSCM( null, localRepositoryPath, new GitBranch( "master" ) );
		git.initialize();
		return git;
	}
	
	@Override
	public void initialize() {
		String cmd = "git init";
		CommandLine.run( cmd, localRepositoryPath );
	}
	
	public void pull() {
		pull( this.parent );
	}
	
	public void pull( Repository parent ) {
		PullImpl pull = new PullImpl( parent );
		doPull( pull );
	}
	
	public class PullImpl extends Pull {
		
		public PullImpl( Repository parent ) {
			super( parent );
		}

		public boolean perform() {
			System.out.println( "GIT: perform pull" );
			
			if( this.parent == null ) {
				System.err.println( "GIT: Could not pull a null branch" );
				return false;
			}
			
			String cmd = "git pull " + this.parent;
			CommandLine.run( cmd, localRepositoryPath );
			
			return true;
		}
	}
	
	@Override
	public List<AbstractCommit> getCommits() {
		String cmd = "git rev-list --all";
		List<String> cs = CommandLine.run( cmd, localRepositoryPath ).stdoutList;
		
		List<AbstractCommit> commits = new ArrayList<AbstractCommit>();
		
		for(String c : cs) {
			commits.add( new GitCommit(c) );
		}
		
		return commits;
	}

}
