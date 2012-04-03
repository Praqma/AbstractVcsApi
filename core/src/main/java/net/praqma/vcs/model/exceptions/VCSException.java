package net.praqma.vcs.model.exceptions;

public class VCSException extends Exception {

	private static final long serialVersionUID = 5511630514361323388L;

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
