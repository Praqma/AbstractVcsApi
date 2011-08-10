package net.praqma.exceptions;

public class ElementDoesNotExistException extends Exception {

	private static final long serialVersionUID = 5511630514361323388L;
	
	public ElementDoesNotExistException() {
		super();
	}
	
	public ElementDoesNotExistException(String s) {
		super(s);
	}
}
