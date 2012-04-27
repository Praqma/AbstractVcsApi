package net.praqma.ava.model.exceptions;


public class APIException extends AVAException {
	
	public enum FailureType {
		UNKNOWN,
		NO_OUTPUT,
		NOTHING_CHANGED
	}

	public FailureType type = FailureType.UNKNOWN;
	
	public APIException( String m ) {
		super( m );
	}
	
	public APIException( String m, Exception e ) {
		super( m, e );
	}

	public APIException( String m, FailureType type, Exception e ) {
		super( m, e );
		this.type = type;
	}
	
	public FailureType getFailureType() {
		return type;
	}

}
