package net.praqma.ava.model.git.exceptions;

import net.praqma.ava.model.exceptions.APIException;

public class GitException extends APIException {

	public GitException(String s) {
		super(s);
	}
	
	public GitException( String s, Exception e ) {
		super( s, e );
	}

	public GitException( String s, FailureType type, Exception e ) {
		super( s, type, e );
	}
}
