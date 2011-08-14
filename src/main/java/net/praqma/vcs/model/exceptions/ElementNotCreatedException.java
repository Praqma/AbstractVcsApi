package net.praqma.vcs.model.exceptions;

public class ElementNotCreatedException extends Exception {

	private static final long serialVersionUID = 5511630514361323388L;
	
	public enum FailureType {
		UNKNOWN,
		EXISTS,
		INITIALIZATON,
		DEPENDENCY
	}
	
	private FailureType type = FailureType.UNKNOWN;
	
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
	
	public FailureType getType() {
		return this.type;
	}
}
