package net.praqma.ava.model.subversion.exceptions;

import net.praqma.ava.model.exceptions.VCSException;

public class SubversionException extends VCSException {

	private static final long serialVersionUID = 5511630514361323388L;

	public SubversionException() {
		super();
	}

	public SubversionException( String s ) {
		super( s );
	}
	
	public SubversionException( String s, Exception e ) {
		super( s, e );
	}

	public SubversionException( String s, FailureType type ) {
		super( s, type );
	}
	
	public SubversionException( String s, FailureType type, Exception e ) {
		super( s, type, e );
	}
}
