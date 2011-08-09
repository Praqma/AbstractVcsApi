package net.praqma.vcs.model;

import java.io.File;
import java.util.List;

import net.praqma.exceptions.OperationNotSupportedException;

public abstract class AbstractBranch {
	
	protected String name;
	protected File localRepositoryPath;
	
	public AbstractBranch( File localRepositoryPath, String name ) {
		this.name = name;
		this.localRepositoryPath = localRepositoryPath;
		
		/* Create path */
		localRepositoryPath.mkdirs();
	}
	
	public String getName() {
		return name;
	}
	
	public File getPath() {
		return this.localRepositoryPath;
	}
	
	public String toString() {
		return name;
	}
}
