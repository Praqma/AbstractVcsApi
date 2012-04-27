package net.praqma.vcs.model.exceptions;

public class ElementNotCreatedException extends ElementException {

	private static final long serialVersionUID = 5511630514361323388L;

	
	public ElementNotCreatedException() {
		super();
	}
	
	public ElementNotCreatedException(String s) {
		super(s);
	}
	
	public ElementNotCreatedException(String s, FailureType type) {
		super(s);
		this.type = type;
	}
	

}
