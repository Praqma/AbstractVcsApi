package net.praqma.vcs.model.mercurial.exceptions;

import net.praqma.vcs.model.exceptions.VCSException;
import net.praqma.vcs.model.exceptions.VCSException.FailureType;

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
