package net.praqma.vcs.model;

import java.io.File;
import java.util.List;

import net.praqma.exceptions.OperationNotImplementedException;
import net.praqma.exceptions.OperationNotSupportedException;
import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.extensions.PullListener;

public abstract class AbstractSCM {

	protected AbstractBranch branch;
	protected Repository parent;
	
	private Logger logger = Logger.getLogger();
	
	public AbstractSCM( ) {
	}	
	
	public AbstractSCM( Repository parent ) {
		this.parent = parent;
	}
	
	public AbstractSCM( Repository parent, AbstractBranch branch ) {
		this.branch = branch;
		this.parent = parent;
	}
	
	public void changeBranch( AbstractBranch branch ) {
		this.branch = branch;
	}
	
	public boolean branchExists( AbstractBranch branch ) throws OperationNotImplementedException {
		throw new OperationNotImplementedException( "Branch exists" );
	}
	
	public void initialize( AbstractBranch branch ) throws OperationNotSupportedException {
		throw new OperationNotSupportedException( "Cannot initialize this kind of repository" );
	}
	
	public void pull() throws OperationNotSupportedException {
		throw new OperationNotSupportedException( "Cannot pull from this kind of repository" );
	}
	
	public void pull( Repository parent ) throws OperationNotSupportedException {
		throw new OperationNotSupportedException( "Cannot pull from this kind of repository" );
	}
	
	protected void doPull( Pull pull ) {

		/* Run the pre pull listener */
		PullListener.runPrePullListener();
		
		boolean status = pull.prePull();
		
		/* Only perform if pre step went good */
		if( status ) {
			status = pull.perform();
		}
		
		pull.postPull( status );
		
		/* Run the post pull listener */
		PullListener.runPostPullListener();
	}
	
	protected abstract class Pull{
		protected Repository parent;
		public Pull( Repository parent ) {
			this.parent = parent;
		}
		
		public boolean prePull() {
			logger.debug( "Abstract: pre pull" );
			return true;
		}
		
		public boolean perform() {
			logger.debug( "Abstract: perform pull" );
			return true;
		}
		
		public boolean postPull( boolean status ) {
			logger.debug( "Abstract: post pull " + status );
			return true;
		}
	}
	
	public void changeBranch( File localRepositoryPath, AbstractBranch branch ) throws OperationNotSupportedException {
		throw new OperationNotSupportedException( "Cannot change branch" );
	}
	
	
	public List<AbstractCommit> getCommits() throws OperationNotImplementedException {
		throw new OperationNotImplementedException( "getCommits" );
	}
	
	public List<AbstractCommit> getCommits( boolean load ) throws OperationNotImplementedException {
		throw new OperationNotImplementedException( "getCommits" );
	}

}
