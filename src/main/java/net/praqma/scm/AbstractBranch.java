package net.praqma.scm;

import java.util.List;

import net.praqma.exceptions.OperationNotSupportedException;

public abstract class AbstractBranch {
	
	protected String name;
	
	public AbstractBranch( String name ) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public List<AbstractCommit> getCommits() throws OperationNotSupportedException {
		throw new OperationNotSupportedException( "Cannot get commits" );
	}
}
