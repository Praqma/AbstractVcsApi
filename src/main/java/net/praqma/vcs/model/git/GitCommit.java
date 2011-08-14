package net.praqma.vcs.model.git;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.ChangeSetElement;
import net.praqma.vcs.model.ChangeSetElement.Status;
import net.praqma.vcs.util.CommandLine;

public class GitCommit extends AbstractCommit {

	private Logger logger = Logger.getLogger();
	
	public GitCommit( String key, AbstractBranch branch ) {
		super(key, branch);
	}
	
	public GitCommit( String key, AbstractBranch branch, int number ) {
		super( key, branch, number );
	}

	public static GitCommit create() {
		return null;
	}
	
	public void load() {
		LoadImpl load = new LoadImpl();
		doLoad( load );
	}
	
	private static final Pattern rx_getChangeFile = Pattern.compile( "^\\s*(\\d+)\\s*(\\d+)\\s*(.*)$" );
	private static final Pattern rx_getCreateFile = Pattern.compile( "^\\s*create\\s*mode\\s*\\d+\\s*(.*)$" );
	private static final Pattern rx_getDeleteFile = Pattern.compile( "^\\s*delete\\s*mode\\s*\\d+\\s*(.*)$" );
	
	public class LoadImpl extends Load {
		
		public LoadImpl() {
			super();
		}

		public boolean perform() {
			logger.debug( "GIT: perform load" );
			
			
			String cmd = "git show -m --numstat --summary --pretty=format:\"%H%n%P%n%aN <%ae>%n%cN <%ce>%n%at%n%ct%n%s%nLISTINGCHANGES\" " + GitCommit.this.key;
			List<String> result = CommandLine.run( cmd, branch.getPath() ).stdoutList;
			
			if( result.size() < 8 ) {
				
			} else {
				GitCommit.this.parentKey = result.get( 1 );
				GitCommit.this.author = result.get( 2 );
				GitCommit.this.committer = result.get( 3 );
				GitCommit.this.authorDate = new Date( (long)Integer.parseInt( result.get( 4 ) ) * 1000 );
				GitCommit.this.committerDate = new Date( (long)Integer.parseInt( result.get( 5 ) ) * 1000 );

				GitCommit.this.title = result.get( 6 );
				
				/* Fetch change set */				
				for(int i = 8 ; i < result.size() ; i++) {
					Matcher m = rx_getChangeFile.matcher( result.get( i ) );
					if( m.find() ) {
						GitCommit.this.changeSet.put( m.group(3), new ChangeSetElement( new File( m.group(3) ), Status.CHANGED ) );
						continue;
					}
					
					Matcher m2 = rx_getCreateFile.matcher( result.get( i ) );
					if( m2.find() ) {
						GitCommit.this.changeSet.put( m2.group(1), new ChangeSetElement( new File( m2.group(1) ), Status.CREATED ) );
						continue;
					}
					
					Matcher m3 = rx_getDeleteFile.matcher( result.get( i ) );
					if( m3.find() ) {
						GitCommit.this.changeSet.put( m3.group(1), new ChangeSetElement( new File( m3.group(1) ), Status.DELETED ) );
						continue;
					}
				}
			}
			
			return true;
		}
	}
}
