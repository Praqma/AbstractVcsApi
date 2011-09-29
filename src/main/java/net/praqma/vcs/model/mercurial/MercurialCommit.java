package net.praqma.vcs.model.mercurial;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.ChangeSetElement;
import net.praqma.vcs.model.ChangeSetElement.Status;
import net.praqma.vcs.util.CommandLine;

public class MercurialCommit extends AbstractCommit {

	private Logger logger = Logger.getLogger();
	
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
	
	public void load() {
		LoadImpl load = new LoadImpl();
		doLoad( load );
	}
	
	private static final Pattern rx_getChangeFile = Pattern.compile( "^\\s*(\\d+)\\s*(\\d+)\\s*(.*)$" );
	private static final Pattern rx_renameFile = Pattern.compile( "^(.*?)\\{(.*?)\\}(.*?)$" );
	private static final Pattern rx_getCreateFile = Pattern.compile( "^\\s*create\\s*mode\\s*\\d+\\s*(.*)$" );
	private static final Pattern rx_getDeleteFile = Pattern.compile( "^\\s*delete\\s*mode\\s*\\d+\\s*(.*)$" );
	private static final Pattern rx_getRenameFile = Pattern.compile( "^\\s*rename\\s*mode\\s*\\d+\\s*(.*)$" );
	
	public class LoadImpl extends Load {
		
		public LoadImpl() {
			super();
		}

		public boolean perform() {
			logger.debug( "Mercurial: perform load" );
			
			
			String cmd = "hg log --rev " + MercurialCommit.this.key +  " --template '{parents}\\n{author}\\n{date|isodate}\\n{files}\\n{file_adds}\\n{file_dels}\\n{file_copies}\\n{desc}' ";
			List<String> result = CommandLine.run( cmd, branch.getPath() ).stdoutList;
			
			if( result.size() < 8 ) {
				
			} else {
				MercurialCommit.this.parentKey = result.get( 0 );
				MercurialCommit.this.author = result.get( 1 );
				MercurialCommit.this.committer = result.get( 1 );
				Date date = null;
				try {
					logger.debug( "Trying to parse " + result.get( 2 ) );
					//date = DateFormat.getInstance().parse( result.get( 2 ) );
					date = isodate.parse( result.get( 2 ) );
					logger.debug( "Success" );
				} catch (ParseException e1) {
					logger.debug( "Failure" );
					logger.warning( "Could not parse date. Defaulting to now: " + e1.getMessage() );
					date = new Date();
				}
				MercurialCommit.this.authorDate = date;
				MercurialCommit.this.committerDate = date;

				MercurialCommit.this.title = result.get( 7 );
				
				/* Get total changeset */
				String[] files = result.get( 3 ).split( "\\s+" );
				
				/* Get added */
				String[] a = result.get( 4 ).split( "\\s+" );
				Set<String> added = new HashSet<String>( Arrays.asList( a ) );
				
				/* Get deletes */
				String[] d = result.get( 5 ).split( "\\s+" );
				Set<String> deletes = new HashSet<String>( Arrays.asList( d ) );
				
				/* Get moves */
				/* To, From */
				Pattern p = Pattern.compile( "(.*?)\\((.*?)\\)" );
				Matcher m1 = p.matcher( result.get( 6 ) );
				Set<String> movesFrom = new HashSet<String>();
				Set<String> movesTo = new HashSet<String>();
				Map<String, String> moves = new HashMap<String, String>();
				while( m1.find() ) {
					moves.put( m1.group(1).trim(), m1.group(2).trim() );
					movesTo.add( m1.group(1).trim() );
					movesFrom.add( m1.group(2).trim() );			
				}
				
				for( String file : files ) {
					
					/* The file has been added */
					if( added.contains( file ) ) {
						/* Detecting a move to */
						if( movesTo.contains( file ) ) {
							logger.debug( "Moving " + moves.get( file ) + " to " + file );
							
							ChangeSetElement cse = new ChangeSetElement( new File( file ), Status.RENAMED );
							cse.setRenameFromFile( new File( moves.get( file ) ) );
							MercurialCommit.this.changeSet.put( file, cse );
						/* Just a plain add */
						} else {
							logger.debug( "Adding " + file );
							MercurialCommit.this.changeSet.put( file, new ChangeSetElement( new File( file ), Status.CREATED ) );
						}
						
						continue;
					}
					
					/* The file has been deleted */
					if( deletes.contains( file )) {
						/* A move from. This is essentially indifferent, it is handled elsewhere */
						if( movesFrom.contains( file ) ) {
							logger.debug( file + " has been deleted" );
							/* no op */
						/* A regular delete */
						} else {
							MercurialCommit.this.changeSet.put( file, new ChangeSetElement( new File( file ), Status.DELETED ) );
						}
						
						continue;
					}
					
					/* Nothing else applies, this is an ordinary change */
					MercurialCommit.this.changeSet.put( file, new ChangeSetElement( new File( file ), Status.CHANGED ) );
				}
								
				result.clear();
			}
			
			return true;
		}
	}


}
