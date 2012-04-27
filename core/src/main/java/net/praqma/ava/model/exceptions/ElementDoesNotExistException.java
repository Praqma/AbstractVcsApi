package net.praqma.ava.model.exceptions;

public class ElementDoesNotExistException extends ElementException {
	
	public ElementDoesNotExistException( String m ) {
		super( m );
	}
	
	public ElementDoesNotExistException( Exception e ) {
		super( e );
	}

	public ElementDoesNotExistException( String s, Exception e ) {
		super( s, e );
	}
}
