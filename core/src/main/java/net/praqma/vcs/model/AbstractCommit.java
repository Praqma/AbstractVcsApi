package net.praqma.vcs.model;

import java.util.Date;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.exceptions.OperationNotImplementedException;
import net.praqma.vcs.model.extensions.CommitLoadListener;

public abstract class AbstractCommit implements Comparable<AbstractCommit> {
	
	protected String key;
	protected String parentKey;
	protected String title;
	
	protected String author;
	protected Date authorDate;
	
	protected String committer;
	protected Date committerDate;
	
	protected int number = -1;
	
	private Logger logger = Logger.getLogger();
	
	//protected List<ChangeSetElement> changeSet = new ArrayList<ChangeSetElement>();
	protected ChangeSet changeSet = new ChangeSet();
	
	protected AbstractBranch branch;
	
	public AbstractCommit( String key, AbstractBranch branch ) {
		this.key = key;
		this.branch = branch;
	}
	
	public AbstractCommit( String key, AbstractBranch branch, int number ) {
		this.key = key;
		this.branch = branch;
		this.number = number;
	}
	
	public abstract void load();
	
	protected void doLoad( Load load ) {
		boolean status = load.preLoad();
		
		CommitLoadListener.runPreCommitLoadListener( this );
		
		/* Only perform if pre step went good */
		if( status ) {
			status = load.perform();
		}
		
		CommitLoadListener.runPostCommitLoadListener( this, status );
		
		load.postLoad( status );
	}
	
	public abstract class Load {
		public boolean preLoad() {
			return true;
		}
		
		public boolean perform() {
			return true;
		}
		
		public boolean postLoad( boolean status ) {
			return true;
		}
	}
	
	public AbstractBranch getBranch() {
		return branch;
	}
	
	public ChangeSet getChangeSet() {
		return this.changeSet;
	}
	
	public String getKey() {
		return key;
	}
	
	public Date getAuthorDate() {
		return authorDate;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setNumber( int i ) {
		this.number = i;
	}
	
	public int getNumber() {
		return this.number;
	}
	
	public String toString() {
		return title + " @ " + authorDate;
	}
	
	public int compareTo( AbstractCommit other ) {
		return this.committerDate.compareTo( other.getAuthorDate() ); 
	}
	
	public String getAuthor() {
		return author;
	}

}