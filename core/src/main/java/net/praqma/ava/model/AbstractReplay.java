package net.praqma.ava.model;

import java.io.File;
import java.util.List;

import net.praqma.util.debug.Logger;
import net.praqma.ava.model.exceptions.OperationNotImplementedException;
import net.praqma.ava.model.exceptions.UnableToReplayException;
import net.praqma.ava.model.extensions.ReplayListener;

public abstract class AbstractReplay {
	
	/**
	 * The branch of the target repository
	 */
	protected AbstractBranch branch;
	
	protected Logger logger = Logger.getLogger();
	
	public AbstractReplay( AbstractBranch branch ) {
		this.branch = branch;
	}
	
	public void perform( List<AbstractCommit> commits ) throws OperationNotImplementedException, UnableToReplayException {
		for( AbstractCommit c : commits ) {
			replay(c);
		}
	}
	
	public void setBranch( AbstractBranch branch ) {
		this.branch = branch;
	}
	
	public abstract void replay( AbstractCommit commit ) throws UnableToReplayException;
	
	protected void doReplay( Replay replay ) {
		
		boolean status = replay.setup();
		
		/* Only replay if setup went good */
		if( status ) {
			status = replay.replay();
		}
		
		if( status ) {
			if( replay.commit() ) {
				ReplayListener.runCommitCreatedListener( this, replay.getCommit() );
			}
		}
		
		replay.cleanup( status );
		
		ReplayListener.runPostReplayListener( this, replay.getCommit(), status );
	}
	
	protected abstract class Replay{
		protected AbstractCommit commit;
		
		public Replay( AbstractCommit commit ) {
			this.commit = commit;
		}
		
		public boolean setup() {
			logger.debug( "Abstract replay setup" );
			return true;
		}
		
		public boolean replay() {
			logger.debug( "Abstract replay" );
			return true;
		}
		
		/**
		 * Removes a file, if it exists
		 * @param file
		 * @return
		 */
		public boolean cleanRename( File file ) {
			if( file.exists() ) {
				logger.debug( "The file still lives, let's kill it" );
				try {
					file.delete();
					return true;
				} catch( Exception e ) {
					logger.warning( "Could not delete " + file + ": " + e.getMessage() );
					return false;
				}
			}
			return true;
		}
		
		public abstract boolean commit();
		
		public boolean cleanup( boolean status ) {
			logger.debug( "Abstract replay cleanup: " + status );
			return true;
		}
		
		public AbstractCommit getCommit() {
			return commit;
		}
	}
	
	public AbstractBranch getBranch() {
		return branch;
	}
}
