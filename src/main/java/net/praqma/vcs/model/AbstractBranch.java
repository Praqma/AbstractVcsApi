package net.praqma.vcs.model;

import java.io.File;
import java.util.Date;
import java.util.List;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.OperationNotImplementedException;
import net.praqma.vcs.model.exceptions.OperationNotSupportedException;
import net.praqma.vcs.model.extensions.PullListener;

public abstract class AbstractBranch {
	
	protected String name;
	protected File localRepositoryPath;
	protected Repository parent;
	
	protected AbstractCommit lastCommit;
	
	protected Logger logger = Logger.getLogger();
	
	public AbstractBranch() throws ElementNotCreatedException {}
	
	public AbstractBranch( File localRepositoryPath, String name ) throws ElementNotCreatedException {
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
	public abstract void initialize() throws ElementException;
	public abstract void initialize( boolean get ) throws ElementException;
	
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


	public abstract boolean exists();
	
	public abstract void get() throws ElementException;
	public abstract void get( boolean initialize ) throws ElementException;
	
	
	/**
	 * Pulls from the previously specified repository location
	 * @throws OperationNotSupportedException
	 */
	public abstract void checkout();
	
	public abstract void checkout( AbstractCommit commit );
	
	protected final void doCheckout( Checkout checkout ) {

		/* Run the pre pull listener */
		PullListener.runPreCheckoutListener();
		
		boolean status = checkout.setup();
		
		/* Only perform if pre step went good */
		if( status ) {
			status = checkout.checkout();
		}
		
		checkout.cleanup( status );
		
		/* Run the post pull listener */
		PullListener.runPostCheckoutListener();
	}
	
	protected abstract class Checkout extends AbstractConstructSequence{
		protected AbstractCommit commit;
		public Checkout( AbstractCommit commit ) {
			this.commit = commit;
		}
		
		public boolean checkout() {
			return true;
		}
	}
	
	public abstract List<AbstractCommit> getCommits();
	public abstract List<AbstractCommit> getCommits( boolean load );
	public abstract List<AbstractCommit> getCommits( boolean load, Date offset );
	
	public AbstractCommit getLastCommit() {
		return lastCommit;
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
