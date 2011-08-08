package net.praqma.scm;

import java.io.File;
import java.util.List;

import net.praqma.exceptions.OperationNotImplementedException;

public abstract class AbstractReplay {
	
	protected File path;
	
	public AbstractReplay( File path ) {
		this.path = path;
	}
	
	public void perform( List<AbstractCommit> commits ) throws OperationNotImplementedException {
		for( AbstractCommit c : commits ) {
			replay(c);
		}
	}
	
	public void replay( AbstractCommit commit ) throws OperationNotImplementedException {
		throw new OperationNotImplementedException( "Replay" );
	}
}
