package net.praqma.vcs.configuration;

import java.io.File;

import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractReplay;
import net.praqma.vcs.model.Repository;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.UnsupportedBranchException;

public abstract class AbstractConfiguration {
	protected File path;
	
	protected Repository parent;
	
	protected AbstractBranch branch;
	
	public AbstractConfiguration( File path ) {
		this.path = path;
	}
	
	public AbstractConfiguration( File path, String parentLocation, String parentName ) {
		this.path = path;
		this.parent = new Repository( parentLocation, parentName );
	}
	
	public File getPath() {
		return path;
	}
	
	public Repository getParent() {
		return parent;
	}
	
	public abstract AbstractBranch getBranch() throws ElementNotCreatedException, ElementDoesNotExistException;
	public abstract AbstractReplay getReplay() throws UnsupportedBranchException, ElementNotCreatedException, ElementDoesNotExistException;
	
	public String toString() {
		return "Path: " + path.getAbsolutePath() + ( parent != null ? "\nParent: " + parent : "" );
	}
}
