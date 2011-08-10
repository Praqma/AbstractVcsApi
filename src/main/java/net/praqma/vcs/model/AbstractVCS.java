package net.praqma.vcs.model;

import java.io.File;
import java.util.List;

import net.praqma.exceptions.OperationNotImplementedException;
import net.praqma.exceptions.OperationNotSupportedException;
import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.extensions.PullListener;

public abstract class AbstractVCS {

	protected File location;
	
	private Logger logger = Logger.getLogger();
	
	/*
	public AbstractVCS( ) {
	}
	*/	
	
	public AbstractVCS( File location ) {
		this.location = location;
	}
	
	/*
	public AbstractVCS( Repository parent, AbstractBranch branch ) {
		this.branch = branch;
		this.location = parent;
	}
	*/

	/*
	public boolean branchExists( AbstractBranch branch ) throws OperationNotImplementedException {
		throw new OperationNotImplementedException( "Branch exists" );
	}
	*/
	
	/**
	 * Initialize the given branch
	 * @param branch
	 * @throws OperationNotSupportedException
	 */
	public void initialize() throws OperationNotSupportedException {
		throw new OperationNotSupportedException( "Cannot initialize this kind of repository" );
	}
	
	protected final void doInitialize( Initialize initialize ) {
		boolean status = initialize.setup();
		
		/* Only initialize if setup went good */
		if( status ) {
			status = initialize.initialize();
		}
		
		initialize.cleanup( status );
	}
	
	protected abstract class Initialize {

		public Initialize() {
		}
		
		public boolean setup() {
			logger.debug( "Abstract initialize VCS setup" );
			return true;
		}
		
		public boolean initialize() {
			logger.debug( "Abstract initialize VCS" );
			return true;
		}
		
		public boolean cleanup( boolean status ) {
			logger.debug( "Abstract initialize VCS cleanup " + status );
			return true;
		}
	}
	
	
	public void changeBranch( File localRepositoryPath, AbstractBranch branch ) throws OperationNotSupportedException {
		throw new OperationNotSupportedException( "Cannot change branch" );
	}

}
