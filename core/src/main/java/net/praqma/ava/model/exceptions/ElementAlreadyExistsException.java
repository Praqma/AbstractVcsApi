package net.praqma.ava.model.exceptions;

public class ElementAlreadyExistsException extends ElementException {

	private static final long serialVersionUID = 5511630514361323388L;
	
	public ElementAlreadyExistsException() {
		super();
	}
	
	public ElementAlreadyExistsException(String s) {
		super(s);
	}
	
}
