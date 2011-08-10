package net.praqma.vcs.model;

import java.io.File;
import java.util.List;

import net.praqma.exceptions.ElementDoesNotExistException;
import net.praqma.exceptions.OperationNotImplementedException;
import net.praqma.exceptions.OperationNotSupportedException;
import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.extensions.PullListener;

public abstract class AbstractBranch {
	
	protected String name;
	protected File localRepositoryPath;
	protected Repository parent;
	
	protected Logger logger = Logger.getLogger();
	
	public AbstractBranch() {}
	
	public AbstractBranch( File localRepositoryPath, String name ) {
		this.name = name;
		this.localRepositoryPath = localRepositoryPath;
		
		/* Create path */
		localRepositoryPath.mkdirs();
	}
	
	public AbstractBranch( File localRepositoryPath, String name, Repository parent ) {
		this.name = name;
		this.localRepositoryPath = localRepositoryPath;
		this.parent = parent;
		
		/* Create path */
		localRepositoryPath.mkdirs();
	}
	
	
	/**
	 * Initialize the given branch
	 * @param branch
	 * @throws OperationNotSupportedException
	 */
	public boolean initialize() throws OperationNotSupportedException {
		throw new OperationNotSupportedException( "Cannot initialize this kind of repository" );
	}
	
	protected final boolean doInitialize( Initialize initialize ) {
		boolean status = initialize.setup();
		
		/* Only initialize if setup went good */
		if( status ) {
			status = initialize.initialize();
		}
		
		return initialize.cleanup( status );
	}
	
	protected abstract class Initialize {
		public Initialize() {
		}
		
		public boolean setup() {
			logger.debug( "Abstract initialize branch setup" );
			return true;
		}
		
		public boolean initialize() {
			logger.debug( "Abstract initialize branch" );
			return true;
		}
		
		public boolean cleanup( boolean status ) {
			logger.debug( "Abstract initialize branch cleanup " + status );
			return true;
		}
	}


	public boolean exists() throws OperationNotImplementedException {
		throw new OperationNotImplementedException( "Branch exists" );
	}
	
	public boolean get() throws OperationNotImplementedException, ElementDoesNotExistException {
		throw new OperationNotImplementedException( "Get branch" );
	}
	
	
	/**
	 * Pulls from the previously specified repository location
	 * @throws OperationNotSupportedException
	 */
	public void pull() throws OperationNotSupportedException {
		throw new OperationNotSupportedException( "Cannot pull from this kind of repository" );
	}
	
	protected final void doPull( Pull pull ) {

		/* Run the pre pull listener */
		PullListener.runPrePullListener();
		
		boolean status = pull.setup();
		
		/* Only perform if pre step went good */
		if( status ) {
			status = pull.pull();
		}
		
		pull.cleanup( status );
		
		/* Run the post pull listener */
		PullListener.runPostPullListener();
	}
	
	protected abstract class Pull{
		
		public Pull() {
		}
		
		public boolean setup() {
			logger.debug( "Abstract: pre pull" );
			return true;
		}
		
		public boolean pull() {
			logger.debug( "Abstract: perform pull" );
			return true;
		}
		
		public boolean cleanup( boolean status ) {
			logger.debug( "Abstract: post pull " + status );
			return true;
		}
	}
	
	public List<AbstractCommit> getCommits() throws OperationNotImplementedException {
		throw new OperationNotImplementedException( "getCommits" );
	}
	
	public List<AbstractCommit> getCommits( boolean load ) throws OperationNotImplementedException {
		throw new OperationNotImplementedException( "getCommits" );
	}
	
	
	public String getName() {
		return name;
	}
	
	public File getPath() {
		return this.localRepositoryPath;
	}
	
	public String toString() {
		return name;
	}
}
