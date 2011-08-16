package net.praqma.vcs.model;

import java.io.File;
import java.util.Date;
import java.util.List;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.exceptions.ElementAlreadyExistsException;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.OperationNotSupportedException;
import net.praqma.vcs.model.extensions.PullListener;
import net.praqma.vcs.model.interfaces.Cleanable;

public abstract class AbstractBranch implements Cleanable {
	
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
	 * @throws ElementAlreadyExistsException 
	 * @throws ElementNotCreatedException 
	 * @throws OperationNotSupportedException
	 */
	public abstract void initialize() throws ElementNotCreatedException, ElementAlreadyExistsException;
	public abstract void initialize( boolean get ) throws ElementNotCreatedException, ElementAlreadyExistsException;
	
	protected final boolean doInitialize( Initialize initialize ) throws ElementNotCreatedException, ElementAlreadyExistsException {
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
		
		public boolean initialize() throws ElementNotCreatedException, ElementAlreadyExistsException {
			return true;
		}
	}


	public abstract boolean exists();
	
	public abstract void get() throws ElementDoesNotExistException;
	public abstract void get( boolean initialize ) throws ElementNotCreatedException, ElementDoesNotExistException;
	
	
	/**
	 * Pulls from the previously specified repository location
	 * @throws OperationNotSupportedException
	 */
	public abstract void update();
	
	public abstract void update( AbstractCommit commit );
	
	protected final void doUpdate( Update update ) {

		/* Run the pre pull listener */
		PullListener.runPreCheckoutListener();
		
		boolean status = update.setup();
		
		/* Only perform if pre step went good */
		if( status ) {
			status = update.update();
		}
		
		/* Run the post pull listener */
		PullListener.runPostCheckoutListener();
	}
	
	protected abstract class Update extends AbstractConstructSequence{
		protected AbstractCommit commit;
		public Update( AbstractCommit commit ) {
			this.commit = commit;
		}
		
		public boolean update() {
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
