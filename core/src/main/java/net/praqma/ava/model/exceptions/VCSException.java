package net.praqma.ava.model.exceptions;

public class VCSException extends Exception {

	public enum FailureType {
		UNKNOWN,
		NO_OUTPUT,
		NOTHING_CHANGED
	}

	public FailureType type = FailureType.UNKNOWN;

	public VCSException() {
		super();
	}

	public VCSException( String s ) {
		super( s );
	}
	
	public VCSException( String s, Exception e ) {
		super( s, e );
	}

	public VCSException( String s, FailureType type ) {
		super( s );
		this.type = type;
	}
	
	public VCSException( String s, FailureType type, Exception e ) {
		super( s, e );
		this.type = type;
	}

	public FailureType getType() {
		return this.type;
	}

}
