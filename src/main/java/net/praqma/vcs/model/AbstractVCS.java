package net.praqma.vcs.model;

import java.io.File;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.OperationNotSupportedException;

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
	public abstract void initialize() throws ElementException;
	
	public abstract void initialize( boolean get ) throws ElementException;
	
	public abstract void get() throws ElementException;
	public abstract void get( boolean initialize ) throws ElementException;
	
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
	
	protected abstract class Initialize extends AbstractConstructSequence {

		protected boolean get = false;
		public Initialize( boolean get ) {
			this.get = get;
		}
		
		public boolean initialize() {
			return true;
		}
	}

}
