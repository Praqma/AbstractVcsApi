package net.praqma.ava.model.mercurial.exceptions;

import net.praqma.ava.model.exceptions.APIException;
import net.praqma.ava.model.exceptions.APIException.FailureType;

public class MercurialException extends APIException {

	public MercurialException(String s) {
		super(s);
	}
	
	public MercurialException( String s, Exception e ) {
		super( s, e );
	}
	
	public MercurialException( String s, FailureType type, Exception e ) {
		super( s, type, e );
	}
}
