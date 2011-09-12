package net.praqma.vcs.model;

import java.io.File;

import net.praqma.util.debug.Logger;

public class ChangeSetElement {
	private File file;
	private File renameFrom;
	
	private static Logger logger = Logger.getLogger();
	
	public enum Status {
		CREATED,
		CHANGED,
		DELETED,
		RENAMED
	}
	
	private Status status;
	
	public ChangeSetElement( File file ) {
		this.file = file;
		this.status = Status.CHANGED;
	}
	
	public ChangeSetElement( File file, Status status ) {
		this.file = file;
		this.status = status;
	}
	
	public File getFile() {
		return file;
	}
	
	public void setRenameFromFile( File file ) {
		logger.debug( "Rename file: " + file );
		this.renameFrom = file;
	}
	
	public File getRenameFromFile() {
		return this.renameFrom;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus( Status status ) {
		this.status = status;
	}
}
