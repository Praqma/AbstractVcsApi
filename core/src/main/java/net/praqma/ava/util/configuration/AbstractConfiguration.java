package net.praqma.ava.util.configuration;

import java.io.File;
import java.io.Serializable;

import net.praqma.util.debug.Logger;
import net.praqma.ava.model.AbstractBranch;
import net.praqma.ava.model.AbstractReplay;
import net.praqma.ava.model.Repository;
import net.praqma.ava.model.exceptions.ElementDoesNotExistException;
import net.praqma.ava.model.exceptions.ElementNotCreatedException;
import net.praqma.ava.model.exceptions.UnsupportedBranchException;
import net.praqma.ava.util.configuration.exception.ConfigurationException;

public abstract class AbstractConfiguration implements Serializable {
	private static final long serialVersionUID = -812250782421147883L;
	private static Logger logger = Logger.getLogger();

	transient protected File path;
	protected String pathName;
	
	protected Repository parent;
	
	protected AbstractBranch branch;
	
	public AbstractConfiguration( String pathName ) {
		this.pathName = pathName;
	}
	
	public AbstractConfiguration( String pathName, String parentLocation, String parentName ) {
		this.pathName = pathName;
		this.parent = new Repository( parentLocation, parentName );
	}
	
	public AbstractConfiguration( File path ) {
		this.path = path;
	}
	
	public AbstractConfiguration( File path, String parentLocation, String parentName ) {
		this.path = path;
		this.parent = new Repository( parentLocation, parentName );
	}
	
	public void generate() throws ConfigurationException {
		logger.debug( "Creating path " + pathName );
		this.path = new File( pathName );
	}
	
	public void setPath( File path ) {
		this.path = path;
	}
	
	public void setPathName( String path ) {
		this.pathName = path;
	}
	
	public File getPath() {
		return path;
	}
	
	public String getPathName() {
		if( this.path != null ) {
			return path.toString();
		} else {
			return pathName;
		}
	}
	
	public Repository getParent() {
		return parent;
	}
	
	public abstract AbstractBranch getBranch() throws ElementNotCreatedException, ElementDoesNotExistException;
	public abstract AbstractReplay getReplay() throws UnsupportedBranchException, ElementNotCreatedException, ElementDoesNotExistException;
	
	public String toString() {
		return "Path: " + getPathName() + "\n";
	}
}
