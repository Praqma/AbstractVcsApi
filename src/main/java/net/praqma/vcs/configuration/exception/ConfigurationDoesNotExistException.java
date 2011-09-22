package net.praqma.vcs.configuration.exception;

public class ConfigurationDoesNotExistException extends Exception {

	private static final long serialVersionUID = 55116214361323388L;

	public ConfigurationDoesNotExistException() {
		super();
	}

	public ConfigurationDoesNotExistException( String s ) {
		super( s );
	}
}
