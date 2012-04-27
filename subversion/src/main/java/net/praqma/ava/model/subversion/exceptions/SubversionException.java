package net.praqma.ava.model.subversion.exceptions;

import net.praqma.ava.model.exceptions.APIException;

public class SubversionException extends APIException {

	public SubversionException( String s ) {
		super( s );
	}
	
	public SubversionException( String s, Exception e ) {
		super( s, e );
	}

	public SubversionException( String s, FailureType type, Exception e ) {
		super( s, type, e );
	}
}
