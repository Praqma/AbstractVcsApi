package net.praqma.vcs.model;

import java.util.List;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.exceptions.OperationNotImplementedException;
import net.praqma.vcs.model.exceptions.UnableToReplayException;

public abstract class AbstractReplay {
	
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
		
		replay.cleanup( status );
	}
	
	protected abstract class Replay{
		protected AbstractCommit commit;
		
		public Replay( AbstractCommit commit ) {
			this.commit = commit;
		}
		
		public boolean setup() {
			logger.log( "Abstract replay setup" );
			return true;
		}
		
		public boolean replay() {
			logger.log( "Abstract replay" );
			return true;
		}
		
		public boolean cleanup( boolean status ) {
			logger.log( "Abstract replay cleanup: " + status );
			return true;
		}
	}
}
