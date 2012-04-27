package net.praqma.ava.model.exceptions;

public class UnableToCheckoutCommitException extends ElementException {

	public UnableToCheckoutCommitException( String s ) {
		super( s );
	}
	
	public UnableToCheckoutCommitException( String s, Exception e ) {
		super( s, e );
	}

}
