package net.praqma.scm.model;

import java.io.File;

public class ChangeSetElement {
	private File file;
	
	public ChangeSetElement( File file ) {
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}
}
