package net.praqma.vcs.model.exceptions;

public class UnableToReplayException extends Exception {

	private static final long serialVersionUID = 5511630514361323388L;
	
	public UnableToReplayException() {
		super();
	}
	
	public UnableToReplayException(String s) {
		super(s);
	}
}
