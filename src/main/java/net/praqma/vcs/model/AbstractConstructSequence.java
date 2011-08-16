package net.praqma.vcs.model;

import java.util.ArrayList;
import java.util.List;

import net.praqma.vcs.model.exceptions.ElementException;


public abstract class AbstractConstructSequence {

	private List<String> messages = new ArrayList<String>();
	private ElementException exception;

	public boolean setup() {
		return true;
	}
	
	public boolean cleanup( boolean status ) {
		return status;
	}
	
	public void addMessage( String msg ) {
		messages.add( msg );
	}
	
	public List<String> getMessages() {
		return messages;
	}
	
	public void setException( ElementException exception ) {
		this.exception = exception;
	}
	
	public ElementException getException() {
		return exception;
	}
}
