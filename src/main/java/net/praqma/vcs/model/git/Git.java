package net.praqma.vcs.model.git;

import java.io.File;

import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.vcs.util.CommandLine;

public class Git {
	
	public static void addRemote( String name, String location, File viewContext ) throws GitException {
		try {
			CommandLine.run( "git remote add " + name + " " + location, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new GitException( "Could add remote: " + e.getMessage() );
		}
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
}
