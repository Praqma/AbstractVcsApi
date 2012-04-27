package net.praqma.ava.util.configuration.exception;

public class ConfigurationException extends Exception {

	private static final long serialVersionUID = 55116214361323388L;

	public ConfigurationException() {
		super();
	}

	public ConfigurationException( String s ) {
		super( s );
	}
	
	public ConfigurationException( String s, Exception e ) {
		super( s, e );
	}
}
