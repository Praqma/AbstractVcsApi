package net.praqma.vcs.model;

import java.util.Date;

import net.praqma.vcs.model.extensions.CommitLoadListener;

public abstract class AbstractCommit implements Comparable<AbstractCommit> {
	
	protected String key;
	protected String parentKey;
	protected String title;
	
	protected String author;
	protected Date authorDate;
	
	protected String committer;
	private Date committerDate;
	
	protected int number = -1;
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

    /**
     * @return the committerDate
     */
    public Date getCommitterDate() {
        return committerDate;
    }

    /**
     * @param committerDate the committerDate to set
     */
    public void setCommitterDate(Date committerDate) {
        this.committerDate = committerDate;
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
	
    @Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		if( title != null ) {
			sb.append( " -- " + title + " --\n" );
			sb.append( " " + authorDate + "\n" );
		}
		
		sb.append( "Key: " + key + "\n" );
		
		for(ChangeSetElement cs : changeSet.asList()) {
			sb.append( " * " + cs.getFile() + "(" + cs.getStatus() + ")\n" );
			if( cs.getRenameFromFile() != null ) {
				sb.append( "   " + cs.getRenameFromFile() + "\n" );
			}
		}
		
		return sb.toString();
	}
	
    @Override
	public int compareTo( AbstractCommit other ) {
		return this.committerDate.compareTo( other.getCommitterDate()); 
	}
	
	public String getAuthor() {
		return author;
	}

}