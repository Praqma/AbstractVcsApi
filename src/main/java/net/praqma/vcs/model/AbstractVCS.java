package net.praqma.vcs.model;

import java.io.File;
import java.util.List;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
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
	
	public abstract boolean exists();
	
	/**
	 * Initialize the given branch
	 * @param branch
	 * @throws OperationNotSupportedException
	 */
	public abstract void initialize() throws ElementNotCreatedException;
	
	public abstract void initialize( boolean get ) throws ElementNotCreatedException;
	
	public abstract void get() throws ElementDoesNotExistException;
	public abstract void get( boolean initialize ) throws ElementNotCreatedException, ElementDoesNotExistException;
	
	/**
	 * Runs the implementation of {@link Initialize}.
	 * @param initialize Instance of {@link Initialize} implementation.
	 * @return True if both initialize - and clean up part returns true 
	 */
	protected final boolean doInitialize( Initialize initialize ) {
		boolean status = initialize.setup();
		
		/* Only initialize if setup went good */
		if( status ) {
			status = initialize.initialize();
		}
		
		return initialize.cleanup( status ) && status;
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

}
