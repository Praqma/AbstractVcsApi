package net.praqma.ava.model.exceptions;

public class ElementNotCreatedException extends ElementException {

	public ElementNotCreatedException( String s ) {
		super( s );
	}
	
	public ElementNotCreatedException( String s, FailureType type, Exception e ) {
		super( s, e );
		this.type = type;
	}

}
