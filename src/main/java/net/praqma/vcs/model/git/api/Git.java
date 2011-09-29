package net.praqma.vcs.model.git.api;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.vcs.model.exceptions.ElementAlreadyExistsException;
import net.praqma.vcs.model.exceptions.VCSException.FailureType;
import net.praqma.vcs.model.git.exceptions.GitException;
import net.praqma.vcs.util.CommandLine;

public class Git {

	private static Logger logger = Logger.getLogger();
	private static final Pattern rx_remoteExists = Pattern.compile( "^.*?remote \\w+ already exists.*?$" );
	private static final SimpleDateFormat datetimeformat  = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	
	public static void add( File file, File viewContext ) throws GitException {
		try {
			CommandLine.run( "git add " + file, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new GitException( "Could add " + file + ": " + e.getMessage() );
		}
	}
	
	public static void addRemote( String name, String location, File viewContext ) throws GitException, ElementAlreadyExistsException {
		try {
			CommandLine.run( "git remote add " + name + " " + location, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			Matcher m = rx_remoteExists.matcher( e.getMessage() );
			if( m.find() ) {
				throw new ElementAlreadyExistsException( "Remote " + name + " already exists" );
			}
			throw new GitException( "Could add remote: " + e.getMessage() );
		}
	}
	
	public static boolean branchExists( String branchName, File viewContext ) throws GitException {
		return repositoryExists( viewContext ) && listBranches(viewContext).contains( branchName );
	}
	
	public static void changeBranch( String branchName, File viewContext ) throws GitException {
		try {
			CommandLine.run( "git checkout " + branchName, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new GitException( "Could not change to branch " + branchName + ": " + e.getMessage() );
		}	
	}
	
	private static final Pattern rx_branchExists = Pattern.compile( "^.*?branch \\w+ already exists.*?$" );
	
	public static void checkoutRemoteBranch( String branchName, String remoteBranchName, File viewContext ) throws GitException, ElementAlreadyExistsException {
		try {
			CommandLine.run( "git checkout -b " + branchName + " " + remoteBranchName, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			Matcher m = rx_branchExists.matcher( e.getMessage() );
			if( m.find() ) {
				throw new ElementAlreadyExistsException( "Branch " + branchName + " already exists" );
			}
			throw new GitException( "Could not checkout remote branch " + branchName + ": " + e.getMessage() );
		}	
	}
	
	/**
	 * Create a commit with a given message. If date is not null, the commit date and author date will be set to it.
	 * @param message Message string
	 * @param date author/commit date
	 * @param viewContext The path
	 * @throws GitException
	 */
	public static void createCommit( String message, Date date, File viewContext ) throws GitException {
		try {
			Map<String, String> vars = new HashMap<String, String>();
			if( date != null ) {
				vars.put( "GIT_AUTHOR_DATE", datetimeformat.format( date ) );
				vars.put( "GIT_COMMITTER_DATE", datetimeformat.format( date ) );
			}
			CommandLine.run( "git commit -a -m \"" + message + "\"", viewContext, true, false, vars );
		} catch( AbnormalProcessTerminationException e ) {
			throw new GitException( "Could not commit " + message + ": " + e.getMessage() );
		}	
	}
	
	public static void checkoutCommit( String key, File viewContext ) throws GitException {
		try {
			CommandLine.run( "git checkout " + key, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new GitException( "Could not checkout commit: " + e.getMessage() );
		}
	}
	
	public static void clone( String parentLocation, File viewContext ) throws GitException {
		try {
			CommandLine.run( "git clone " + parentLocation + " .", viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new GitException( "Could not clone " + parentLocation + ": " + e.getMessage() );
		}
	}
	
	public static void fetch( File viewContext ) throws GitException {
		try {
			CommandLine.run( "git fetch", viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new GitException( "Could not fetch: " + e.getMessage() );
		}		
	}
	
	public static List<String> getCommitHashes( Date from, Date to, File viewContext ) throws GitException {
		try {
			return CommandLine.run( "git log --reverse --format=\"%H\"" + ( from != null ? " --since=\"" + datetimeformat.format( from ) + "\"" : "" ) +
					                                            ( to   != null ? " --until=\"" + datetimeformat.format( to ) + "\"" : "" ), viewContext ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			if( e.getMessage().matches( "^fatal: bad default revision 'HEAD'$" ) ) {
				throw new GitException( "Could not get hashes: " + e.getMessage(), FailureType.NO_OUTPUT );
			}
			throw new GitException( "Could not get hashes: " + e.getMessage() );
		}
	}
	
	//private static final Pattern rx_repoExists = Pattern.compile( "^.*?remote \\w+ already exists.*?$" );
	
	public static void initialize( File viewContext ) throws GitException {
		try {
			CommandLine.run( "git init", viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new GitException( "Could not initialize repository: " + e.getMessage() );
		}
	}
	
	private static final Pattern rx_findBranches = Pattern.compile( "^.*?(\\s+)\\s*$" );
	
	public static List<String> listBranches( File viewContext ) throws GitException {
		List<String> branches = new ArrayList<String>();
		try {
			List<String> list = CommandLine.run( "git branch", viewContext ).stdoutList;
			
			for( String s : list ) {
				branches.add( s.substring( 2 ).trim() );
			}
		} catch( AbnormalProcessTerminationException e ) {
			throw new GitException( "Could not list Git branches: " + e.getMessage() );
		}
		
		return branches;
	}
	
	/**
	 * Move or rename a file from file to destination
	 * @param file
	 * @param destination
	 * @param viewContext
	 * @throws GitException
	 */
	public static void move( File file, File destination, File viewContext ) throws GitException {
		try {
			CommandLine.run( "git mv " + file + " " + destination, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new GitException( "Could not move " + file + " : " + e.getMessage() );
		}
	}
	
	/**
	 * Pull from a remote location, where location is either an address or a remote
	 * @param branch The remote branch name
	 * @param location The location
	 * @param viewContext
	 * @throws GitException 
	 */
	public static void pull( String location, String branch, File viewContext ) throws GitException {
		try {
			CommandLine.run( "git pull " + location + " " + branch, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new GitException( "Could not pull " + branch + " from " + location + " : " + e.getMessage() );
		}
	}
	
	public static void remove( File file, File viewContext ) throws GitException {
		try {
			CommandLine.run( "git rm " + file, viewContext ); // What about directories? -r
		} catch( AbnormalProcessTerminationException e ) {
			throw new GitException( "Could not remove " + file + " : " + e.getMessage() );
		}
	}
	
	public static boolean repositoryExists( File viewContext ) {
		return new File( viewContext, ".git" ).exists();
	}
}
