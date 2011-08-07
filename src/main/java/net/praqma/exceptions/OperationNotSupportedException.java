package net.praqma.exceptions;

public class OperationNotSupportedException extends Exception {

	private static final long serialVersionUID = 5511630514361323388L;
	
	public OperationNotSupportedException() {
		super();
	}
	
	public OperationNotSupportedException(String s) {
		super(s);
	}
}
