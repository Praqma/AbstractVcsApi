package net.praqma.ava.model.exceptions;

public class AVAException extends Exception {

	public AVAException( String m ) {
		super( m );
	}	
	
	public AVAException( Exception e ) {
		super( e );
	}	
	
	public AVAException( String m, Exception e ) {
		super( m, e );
	}
}
