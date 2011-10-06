package net.praqma.vcs.model.mercurial.api;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.vcs.model.Repository;
import net.praqma.vcs.model.exceptions.VCSException.FailureType;
import net.praqma.vcs.model.git.exceptions.GitException;
import net.praqma.vcs.model.mercurial.exceptions.MercurialException;
import net.praqma.vcs.util.CommandLine;

public class Mercurial {

	private static Logger logger = Logger.getLogger();
	private static final SimpleDateFormat datetimeformat  = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	
	public static void add( File file, File viewContext ) throws MercurialException {
		try {
			CommandLine.run( "hg add " + file, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new MercurialException( "Could add " + file + ": " + e.getMessage() );
		}
	}
	
	public static boolean branchExists( String branchName, File viewContext ) throws MercurialException {
		return repositoryExists( viewContext ) && listBranches(viewContext).contains( branchName );
	}
	
	public static void changeBranch( String branchName, File viewContext ) throws MercurialException {
		try {
			CommandLine.run( "hg update -C " + branchName, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new MercurialException( "Could not change to branch " + branchName + ": " + e.getMessage() );
		}	
	}
	
	private static final Pattern rx_branchExists = Pattern.compile( "^.*?branch \\w+ already exists.*?$" );
	
	public static void createCommit( String message, String author, Date date, File viewContext ) throws MercurialException {
		try {
			CommandLine.run( "hg commit -m \"" + message + "\"" + ( author != null ? " --user=\"" + author + "\"" : "" ) + ( date != null ? " --date=\"" + datetimeformat.format( date ) + "\"" : "" ), viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new MercurialException( "Could not commit " + message + ": " + e.getMessage() );
		}	
	}
		
	/**
	 * Resets the working copy's state to the given revision
	 * @param key
	 * @param viewContext
	 * @throws MercurialException
	 */
	public static void checkoutCommit( String key, File viewContext ) throws MercurialException {
		try {
			CommandLine.run( "hg update --rev " + key, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new MercurialException( "Could not checkout commit: " + e.getMessage() );
		}
	}
	
	public static void clone( String parentLocation, File viewContext ) throws MercurialException {
		try {
			CommandLine.run( "hg clone " + parentLocation + " .", viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new MercurialException( "Could not clone " + parentLocation + ": " + e.getMessage() );
		}
	}
	
	/**
	 * Fetch a remote repository given by the location. If location is null, this is equivalent to hg pull
	 * without any source
	 * @param location
	 * @param viewContext
	 * @throws MercurialException
	 */
	public static void fetch( String location, File viewContext ) throws MercurialException {
		try {
			CommandLine.run( "hg pull" + ( location != null ? " " + location : "" ), viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new MercurialException( "Could not fetch: " + e.getMessage() );
		}		
	}
	
	public static List<String> getChangeset( int revision, File viewContext ) throws MercurialException {
		try {
			if( revision == 0 ) {
				return CommandLine.run( "hg status --copies --rev null:0", viewContext ).stdoutList;
			} else {
				return CommandLine.run( "hg status --copies --rev 'p1(" + revision + "):" + revision + "'", viewContext ).stdoutList;
			}
		} catch( AbnormalProcessTerminationException e ) {
			throw new MercurialException( "Could not get changeset: " + e.getMessage() );
		}	
	}
	
	public static List<String> getCommitHashes( Date from, Date to, File viewContext ) throws MercurialException {
		try {
			String dateString = "";
			if( from != null ) {
				dateString = datetimeformat.format( from );
			}
			if( to != null ) {
				if( dateString.length() > 0 ) {
					dateString += " to " + datetimeformat.format( to );
				} else {
					dateString = datetimeformat.format( to );
				}
			}
			return CommandLine.run( "hg log --rev 0: --template '{node}\\n'" + ( dateString.length() > 0 ? " --date=\"" + dateString : "" ), viewContext ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			if( e.getMessage().matches( "^fatal: bad default revision 'HEAD'$" ) ) {
				throw new MercurialException( "Could not get hashes: " + e.getMessage(), FailureType.NO_OUTPUT );
			}
			throw new MercurialException( "Could not get hashes: " + e.getMessage() );
		}
	}
	

	public static void initialize( File viewContext ) throws MercurialException {
		try {
			CommandLine.run( "hg init", viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new MercurialException( "Could not initialize repository: " + e.getMessage() );
		}
	}
		
	public static List<String> listBranches( File viewContext ) throws MercurialException {
		List<String> branches = new ArrayList<String>();
		try {
			List<String> list = CommandLine.run( "hg branches -q", viewContext ).stdoutList;
			
			for( String s : list ) {
				branches.add( s.trim() );
			}
		} catch( AbnormalProcessTerminationException e ) {
			throw new MercurialException( "Could not list Mercurial branches: " + e.getMessage() );
		}
		
		return branches;
	}
	
	/**
	 * Move or rename a file from file to destination
	 * @param file
	 * @param destination
	 * @param viewContext
	 * @throws MercurialException
	 */
	public static void move( File file, File destination, File viewContext ) throws MercurialException {
		try {
			CommandLine.run( "hg rename \"" + file + "\" \"" + destination + "\"", viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new MercurialException( "Could not move " + file + " : " + e.getMessage() );
		}
	}
	
	/**
	 * Pull from a remote location, where location is either an address or a remote
	 * @param branch The remote branch name
	 * @param location The location
	 * @param viewContext
	 * @throws MercurialException 
	 */
	public static void pull( String location, String branch, File viewContext ) throws MercurialException {
		try {
			CommandLine.run( "hg pull --branch " + branch + " " + location, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new MercurialException( "Could not pull " + branch + " from " + location + " : " + e.getMessage() );
		}
	}
	
	/**
	 * 
	 * @param repository
	 * @param newBranch
	 * @param viewContext
	 * @throws MercurialException
	 */
	public static void push( Repository repository, boolean newBranch, File viewContext ) throws MercurialException {
		try {
			CommandLine.run( "hg push " + ( newBranch ? "--new-branch" : "" ) + repository.getLocation(), viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new MercurialException( "Could not push to " + repository.getLocation() + " : " + e.getMessage() );
		}
	}
	
	public static void remove( File file, File viewContext ) throws MercurialException {
		try {
			CommandLine.run( "hg remove " + file, viewContext ); // What about directories? -r
		} catch( AbnormalProcessTerminationException e ) {
			throw new MercurialException( "Could not remove " + file + " : " + e.getMessage() );
		}
	}
	
	public static boolean repositoryExists( File viewContext ) {
		return new File( viewContext, ".hg" ).exists();
	}
}
