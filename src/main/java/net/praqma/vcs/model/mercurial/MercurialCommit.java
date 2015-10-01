package net.praqma.vcs.model.mercurial;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.ChangeSetElement;
import net.praqma.vcs.model.ChangeSetElement.Status;
import net.praqma.vcs.model.mercurial.api.Mercurial;
import net.praqma.vcs.model.mercurial.exceptions.MercurialException;
import net.praqma.vcs.util.CommandLine;

public class MercurialCommit extends AbstractCommit {

	private static final Logger logger = Logger.getLogger(MercurialCommit.class.getName());
	
	/* NOTE THAT THE HG MANUAL SAYS IT HAS SECONDS, BUT THE OUTPUT HASN'T */
	private static final SimpleDateFormat isodate  = new SimpleDateFormat( "yyyy-MM-dd HH:mm Z" );
	
	public MercurialCommit( String key, AbstractBranch branch ) {
		super(key, branch);
	}
	
	public MercurialCommit( String key, AbstractBranch branch, int number ) {
		super( key, branch, number );
	}

	public static MercurialCommit create() {
		return null;
	}
	
    @Override
	public void load() {
		LoadImpl load = new LoadImpl();
		doLoad( load );
	}

	public class LoadImpl extends Load {
		
		public LoadImpl() {
			super();
		}

        @Override
		public boolean perform() {
			logger.fine( "Mercurial: perform load" );
			
			
			String cmd = "hg log --rev " + MercurialCommit.this.key +  " --template=\"{parents}\\n{author}\\n{date|isodate}\\n{rev}\\n{desc}\"";
			List<String> result = CommandLine.run( cmd, branch.getPath() ).stdoutList;
			
			MercurialCommit.this.parentKey = result.get( 0 );
			MercurialCommit.this.author = result.get( 1 );
			MercurialCommit.this.committer = result.get( 1 );
			Date date = null;
			try {
				logger.fine( "Trying to parse " + result.get( 2 ) );
				date = isodate.parse( result.get( 2 ) );
				logger.fine( "Success" );
			} catch (ParseException e1) {
                logger.log(Level.WARNING, "Unable to parse date", e1);
				date = new Date();
			}
			MercurialCommit.this.authorDate = date;
			MercurialCommit.this.setCommitterDate(date);

			MercurialCommit.this.title = result.get( 4 );
			
			int rev = 0;
			try {
				rev = Integer.parseInt( result.get( 3 ) );
			} catch( Exception e ) {
				/* no op */
			}
			
			List<String> files1 = null;
			
			try {
				files1 = Mercurial.getChangeset( rev, branch.getPath() );
			} catch (MercurialException e) {
				logger.warning( e.getMessage() );
				
				return false;
			}
			
			logger.fine( "CS: " + files1 );
			
			List<String[]> filesmap = new ArrayList<>();
			Set<String> removes = new HashSet<>();
			Set<String> origins = new HashSet<>();
			
			for( String f : files1 ) {
				String[] s = new String[]{ f.substring( 0, 1 ), f.substring( 2 ).trim() };
				filesmap.add( s );
				if( s[0].equals( "R" ) ) {
					removes.add( s[1] );
				}
				
				if( s[0].equals( " " ) ) {
					origins.add( s[1] );
				}
			}
			
			/* Remove removes from moves */
			for( String o : origins ) {
				if( removes.contains( o ) ) {
					removes.remove( o );
				}
			}
			
			for( int i = 0 ; i < filesmap.size() ; ++i ) {
				String file = filesmap.get( i )[1];
				String mode = filesmap.get( i )[0];
				
				logger.fine( "FILE: " + file + "[" + mode + "]" );
				
				/* Added */
				if( mode.equals( "A" ) ) {
					/* A renamed element */
					if( i + 1 < filesmap.size() && filesmap.get( i + 1 )[0].equals( " " ) ) {
						String src = filesmap.get( i + 1 )[1];
						
						logger.fine( "Moving " + src + " to " + file );
						
						ChangeSetElement cse = new ChangeSetElement( new File( file ), Status.RENAMED );
						cse.setRenameFromFile( new File( src ) );
						MercurialCommit.this.changeSet.put( file, cse );
						
						i++;
						
					/* Just a plain add */
					} else {
						logger.fine( "Adding " + file );
						MercurialCommit.this.changeSet.put( file, new ChangeSetElement( new File( file ), Status.CREATED ) );
					}
					
					continue;
				}
				
				/* The file has been deleted */
				if( mode.equals( "R" ) ) {
					if( removes.contains( file ) ) {
						MercurialCommit.this.changeSet.put( file, new ChangeSetElement( new File( file ), Status.DELETED ) );
					}
					
					continue;
				}
				
				/* The file has been modified */
				if( mode.equals( "M" ) ) {
					MercurialCommit.this.changeSet.put( file, new ChangeSetElement( new File( file ), Status.CHANGED ) );
					
					continue;
				}
				
				/* Nothing else applies, let's log it */
				logger.warning( "No satisfying action to handle " + file + "[" + mode + "]" );
			}
			
			result.clear();
			
			return true;
		}
	}


}
