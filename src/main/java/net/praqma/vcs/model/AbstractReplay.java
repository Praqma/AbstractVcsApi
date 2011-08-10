package net.praqma.vcs.model;

import java.io.File;
import java.util.List;

import net.praqma.exceptions.OperationNotImplementedException;
import net.praqma.exceptions.UnableToReplayException;
import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.extensions.PullListener;

public abstract class AbstractReplay {
	
	protected File path;
	
	protected Logger logger = Logger.getLogger();
	
	public AbstractReplay( File path ) {
		this.path = path;
	}
	
	public void perform( List<AbstractCommit> commits ) throws OperationNotImplementedException, UnableToReplayException {
		for( AbstractCommit c : commits ) {
			replay(c);
		}
	}
	
	public void replay( AbstractCommit commit ) throws OperationNotImplementedException, UnableToReplayException {
		throw new OperationNotImplementedException( "Replay" );
	}
	
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
