package net.praqma.vcs.model.git;

public class GitException extends Exception {

	private static final long serialVersionUID = 5511630514361323388L;
	
	public enum FailureType {
		UNKNOWN,
		
	}
	
	public GitException() {
		super();
	}
	
	public GitException(String s) {
		super(s);
	}
	
}
