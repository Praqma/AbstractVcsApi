package net.praqma.scm;

import java.util.Calendar;

public abstract class AbstractCommit {
	
	protected String key;
	protected String title;
	
	protected String author;
	protected Calendar authorDate;
	
	protected String committer;
	protected Calendar committerDate;
	
	public AbstractCommit( String key ) {
		this.key = key;
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
			System.out.println( "Abstract: pre commit load" );
			return true;
		}
		
		public boolean perform() {
			System.out.println( "Abstract: perform commit load" );
			return true;
		}
		
		public boolean postLoad( boolean status ) {
			System.out.println( "Abstract: post commit load " + status );
			return true;
		}
	}
	
}