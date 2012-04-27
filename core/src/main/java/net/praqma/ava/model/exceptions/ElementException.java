package net.praqma.ava.model.exceptions;

public class ElementException extends AVAException {

	private static final long serialVersionUID = 5511630514361323388L;

	public enum FailureType {
		UNKNOWN, EXISTS, INITIALIZATON, DEPENDENCY, MULTIPLE
	}

	public FailureType type = FailureType.UNKNOWN;

	public ElementException( String s ) {
		super( s );
	}
	
	public ElementException( Exception e ) {
		super( e );
	}

	public ElementException( String s, Exception e ) {
		super( s, e );
	}

	public ElementException( String s, FailureType type ) {
		super( s );
		this.type = type;
	}

	public ElementException( String s, FailureType type, Exception e ) {
		super( s, e );
		this.type = type;
	}
	
	public FailureType getType() {
		return this.type;
	}

}
