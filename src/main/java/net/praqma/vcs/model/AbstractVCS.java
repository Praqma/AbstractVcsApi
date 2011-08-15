package net.praqma.vcs.model;

import java.io.File;
import java.util.List;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.OperationNotImplementedException;
import net.praqma.vcs.model.exceptions.OperationNotSupportedException;
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
	public abstract boolean initialize() throws ElementNotCreatedException;
	
	public abstract boolean initialize( boolean get ) throws ElementNotCreatedException;
	
	public abstract boolean get() throws ElementNotCreatedException;
	
	protected final boolean doInitialize( Initialize initialize ) {
		boolean status = initialize.setup();
		
		/* Only initialize if setup went good */
		if( status ) {
			status = initialize.initialize();
		}
		
		return initialize.cleanup( status );
	}
	
	protected abstract class Initialize {

		protected boolean get = false;
		public Initialize( boolean get ) {
			this.get = get;
		}
		
		public boolean setup() {
			return true;
		}
		
		public boolean initialize() {
			return true;
		}
		
		public boolean cleanup( boolean status ) {
			return true;
		}
	}
	
	
	public void changeBranch( File localRepositoryPath, AbstractBranch branch ) throws OperationNotSupportedException {
		throw new OperationNotSupportedException( "Cannot change branch" );
	}

}
