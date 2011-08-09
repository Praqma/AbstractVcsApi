package net.praqma.scm.model;

import java.io.File;
import java.util.List;

import net.praqma.exceptions.OperationNotImplementedException;
import net.praqma.exceptions.UnableToReplayException;

public abstract class AbstractReplay {
	
	protected File path;
	
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
}
