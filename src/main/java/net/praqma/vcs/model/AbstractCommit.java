package net.praqma.vcs.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.praqma.exceptions.OperationNotImplementedException;
import net.praqma.util.debug.Logger;

public abstract class AbstractCommit {
	
	protected String key;
	protected String parentKey;
	protected String title;
	
	protected String author;
	protected Date authorDate;
	
	protected String committer;
	protected Date committerDate;
	
	private Logger logger = Logger.getLogger();
	
	protected List<ChangeSetElement> changeSet = new ArrayList<ChangeSetElement>();
	
	protected AbstractBranch branch;
	
	public AbstractCommit( String key, AbstractBranch branch ) {
		this.key = key;
		this.branch = branch;
	}
	
	public void load() throws OperationNotImplementedException {
		throw new OperationNotImplementedException( "load" );
	}
	
	protected void doLoad( Load load ) {
		boolean status = load.preLoad();
		
		/* Only perform if pre step went good */
		if( status ) {
			status = load.perform();
		}
		
		load.postLoad( status );
	}
	
	public abstract class Load {
		public boolean preLoad() {
			logger.debug( "Abstract: pre commit load" );
			return true;
		}
		
		public boolean perform() {
			logger.debug( "Abstract: perform commit load" );
			return true;
		}
		
		public boolean postLoad( boolean status ) {
			logger.debug( "Abstract: post commit load " + status );
			return true;
		}
	}
	
	public AbstractBranch getBranch() {
		return branch;
	}
	
	public List<ChangeSetElement> getChangeSet() {
		return this.changeSet;
	}
	
	public String getKey() {
		return key;
	}
	
	public Date getAuthorDate() {
		return authorDate;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();

		if( title != null ) {
			sb.append( " -- " + title + " --\n" );
			sb.append( " " + authorDate + "\n" );
		}
		
		sb.append( "Key: " + key + "\n" );
		
		for(ChangeSetElement cs : changeSet) {
			sb.append( " * " + cs.getFile() + "\n" );
		}
		
		return sb.toString();
	}
	
}