package net.praqma.vcs.model.git;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.AbstractSCM;
import net.praqma.vcs.model.Repository;
import net.praqma.vcs.util.CommandLine;

public class GitSCM extends AbstractSCM {
	
	private Logger logger = Logger.getLogger();

	public GitSCM( Repository parent, AbstractBranch branch ) {
		super( parent, branch );
	}
	
	public static GitSCM create( GitBranch branch ) {
		GitSCM git = new GitSCM( null, branch );
		git.initialize( branch );
		return git;
	}
	
	@Override
	public void initialize( AbstractBranch branch ) {
		String cmd = "git init";
		CommandLine.run( cmd, branch.getPath() );
	}
	
	public void changeBranch( AbstractBranch branch ) {
		super.changeBranch( branch );
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
			logger.debug( "GIT: perform pull" );
			
			if( this.parent == null ) {
				System.err.println( "GIT: Could not pull a null branch" );
				return false;
			}
			
			String cmd = "git pull " + this.parent;
			CommandLine.run( cmd, branch.getPath() );
			
			return true;
		}
	}
	
	@Override
	public List<AbstractCommit> getCommits() {
		return getCommits(false);
	}
	
	@Override
	public List<AbstractCommit> getCommits( boolean load ) {
		String cmd = "git rev-list --all";
		List<String> cs = CommandLine.run( cmd, branch.getPath() ).stdoutList;
		
		List<AbstractCommit> commits = new ArrayList<AbstractCommit>();
		
		for(String c : cs) {
			GitCommit commit = new GitCommit(c, branch);
			if( load ) {
				commit.load();
			}
			
			commits.add( commit );
		}
		
		Collections.reverse( commits );
		
		return commits;
	}

}
