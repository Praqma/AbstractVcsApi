package net.praqma.vcs.model.git.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.vcs.model.git.exceptions.GitException;
import net.praqma.vcs.util.CommandLine;

public class Git {
	
	public static void addRemote( String name, String location, File viewContext ) throws GitException {
		try {
			CommandLine.run( "git remote add " + name + " " + location, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new GitException( "Could add remote: " + e.getMessage() );
		}
	}
	
	public static boolean branchExists( String branchName, File viewContext ) throws GitException {
		return repositoryExists( viewContext ) && listBranches(viewContext).contains( branchName );
	}
	
	public static void checkoutCommit( String key, File viewContext ) throws GitException {
		try {
			CommandLine.run( "git checkout " + key, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new GitException( "Could not checkout commit: " + e.getMessage() );
		}
	}
	
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
				list.add( s.substring( 2 ).trim() );
			}
		} catch( Exception e ) {
			throw new GitException( "Could not list Git branches: " + e.getMessage() );
		}
		
		return branches;
	}
	
	/**
	 * Pull from a remote location, where location is either an address or a remote
	 * @param branch The remote branch name
	 * @param location The location
	 * @param viewContext
	 * @throws GitException 
	 */
	public static void pull( String branch, String location, File viewContext ) throws GitException {
		try {
			CommandLine.run( "git pull " + location + " " + branch, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new GitException( "Could pull " + branch + " from " + location + " : " + e.getMessage() );
		}
	}
	
	public static boolean repositoryExists( File viewContext ) {
		return new File( viewContext, ".git" ).exists();
	}
}
