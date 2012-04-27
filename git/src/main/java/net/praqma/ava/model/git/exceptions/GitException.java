package net.praqma.vcs.model.git.exceptions;

import net.praqma.vcs.model.exceptions.VCSException;

public class GitException extends VCSException {

	private static final long serialVersionUID = 5511630514361323388L;
		
	public GitException() {
		super();
	}
	
	public GitException(String s) {
		super(s);
	}
	
	public GitException( String s, FailureType type ) {
		super( s, type );
	}
	
}
