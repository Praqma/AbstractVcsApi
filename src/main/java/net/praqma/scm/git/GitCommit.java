package net.praqma.scm.git;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.scm.AbstractBranch;
import net.praqma.scm.AbstractCommit;
import net.praqma.scm.ChangeSetElement;
import net.praqma.scm.git.GitSCM.PullImpl;
import net.praqma.scm.util.CommandLine;

public class GitCommit extends AbstractCommit {
	
	public GitCommit( String key, AbstractBranch branch ) {
		super(key, branch);
	}

	public static GitCommit create() {
		return null;
	}
	
	public void load() {
		LoadImpl load = new LoadImpl();
		doLoad( load );
	}
	
	private static final Pattern rx_getChangeFile = Pattern.compile( "^\\s*(\\d+)\\s*(\\d+)\\s*(.*)$" );
	
	public class LoadImpl extends Load {
		
		public LoadImpl() {
			super();
		}

		public boolean perform() {
			System.out.println( "GIT: perform load" );
			
			
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
						GitCommit.this.changeSet.add( new ChangeSetElement( new File( m.group(3)) ) );
					}
				}
			}
			
			return true;
		}
	}
}
