package net.praqma.vcs.model;

import java.io.File;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.OperationNotSupportedException;
import net.praqma.vcs.model.interfaces.Cleanable;

public abstract class AbstractVCS implements Cleanable {

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
	 * @throws ElementNotCreatedException 
	 * @throws ElementDoesNotExistException
	 */
	public abstract void initialize() throws ElementNotCreatedException, ElementDoesNotExistException;
	
	public abstract void initialize( boolean get ) throws ElementNotCreatedException, ElementDoesNotExistException;
	
	public abstract void get() throws ElementDoesNotExistException;
	public abstract void get( boolean initialize ) throws ElementNotCreatedException, ElementDoesNotExistException;
	
	/**
	 * Runs the implementation of {@link Initialize}.
	 * @param initialize Instance of {@link Initialize} implementation.
	 * @return True if both initialize - and clean up part returns true 
	 * @throws ElementNotCreatedException 
	 * @throws ElementDoesNotExistException 
	 */
	protected final boolean doInitialize( Initialize initialize ) throws ElementNotCreatedException, ElementDoesNotExistException {
		boolean status = initialize.setup();
		
		/* Only initialize if setup went good */
		if( status ) {
			status = initialize.initialize();
		}
		
		return status;
	}
	
	protected abstract class Initialize extends AbstractConstructSequence {

		protected boolean get = false;
		public Initialize( boolean get ) {
			this.get = get;
		}
		
		public boolean initialize() throws ElementNotCreatedException, ElementDoesNotExistException {
			return true;
		}
	}

}
