package net.praqma.vcs.model.exceptions;

import net.praqma.vcs.model.AbstractCommit;

public class UnableToCheckoutCommitException extends ElementException {

    private AbstractCommit commit;  
	private static final long serialVersionUID = 5511630514361323388L;
	
	public UnableToCheckoutCommitException() {
		super("Some elements could not be checked out");
	}
    
    public UnableToCheckoutCommitException(AbstractCommit commit) {
		super("Could not checkout: "+ commit.toString());
        this.commit = commit;
	}
    
    public UnableToCheckoutCommitException(String message, AbstractCommit commit) {
		super(message+ ": "+commit.toString());
        this.commit = commit;
	}
	
	public UnableToCheckoutCommitException(String s) {
		super(s);
	}
	
}
