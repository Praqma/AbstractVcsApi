package net.praqma.ava.model.exceptions;

public class ElementDoesNotExistException extends ElementException {

	private static final long serialVersionUID = 5511630514361323388L;

	public ElementDoesNotExistException() {
		super();
	}

	public ElementDoesNotExistException( String s ) {
		super( s );
	}

	public ElementDoesNotExistException( String s, Exception e ) {
		super( s, e );
	}
}
