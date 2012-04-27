package net.praqma.ava.model.exceptions;

public class UnsupportedBranchException extends AVAException {

	public UnsupportedBranchException( String s ) {
		super( s );
	}
	
	public UnsupportedBranchException( String s, Exception e ) {
		super( s, e );
	}
}
