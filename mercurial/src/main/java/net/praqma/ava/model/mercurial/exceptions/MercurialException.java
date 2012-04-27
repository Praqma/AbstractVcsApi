package net.praqma.ava.model.mercurial.exceptions;

import net.praqma.ava.model.exceptions.VCSException;
import net.praqma.ava.model.exceptions.VCSException.FailureType;

public class MercurialException extends VCSException {

	private static final long serialVersionUID = 5511630514361323388L;
	
	public MercurialException() {
		super();
	}
	
	public MercurialException(String s) {
		super(s);
	}
	
	public MercurialException( String s, FailureType type ) {
		super( s, type );
	}
}
