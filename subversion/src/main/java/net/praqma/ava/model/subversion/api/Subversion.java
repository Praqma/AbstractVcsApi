package net.praqma.ava.model.subversion.api;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.ava.model.subversion.exceptions.SubversionException;
import net.praqma.ava.util.CommandLine;

public class Subversion {
	private static Logger logger = Logger.getLogger();
	
	private static final SimpleDateFormat datetimeformat  = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

	public static void checkout( String parentLocation, File viewContext ) throws SubversionException {
		try {
			CommandLine.run( "svn checkout " + parentLocation + " .", viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new SubversionException( "Could not clone " + parentLocation, e );
		}
	}
	
	public static void changeBranch( String branceshUrl, String branchName, File viewContext ) throws SubversionException {
		try {
			CommandLine.run( "svn switch " + branceshUrl + "/" + branchName, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			throw new SubversionException( "Could not change to branch", e );
		}	
	}
	
	public static List<Integer> getRevisions( Date from, Date to, File viewContext ) throws SubversionException {
		String out = null;
		try {
			String cmd = "svn log";
			if( from != null || to != null ) {
				cmd += " -r '";
				if( from != null ) {
					cmd += "{" + datetimeformat.format( from ) + "}";
				}
				cmd += ":";
				if( to != null ) {
					cmd += "{" + datetimeformat.format( to ) + "}";
				}
				cmd += "'";
			}
			out = CommandLine.run( cmd, viewContext ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			throw new SubversionException( "Getting log", e );
		}
		
		Pattern p = Pattern.compile( "^r(\\d+)\\s\\|", Pattern.MULTILINE );
		Matcher m = p.matcher( out );
		
		List<Integer> list = new ArrayList<Integer>();
		
		while( m.find() ) {
			list.add( new Integer( m.group( 1 ) ) );
		}
		
		return list;
	}
	
	public static List<String> getRevision( int revision, File viewContext ) throws SubversionException {
		try {
			return CommandLine.run( "svn log -v -r " + revision, viewContext ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			throw new SubversionException( "Could not get revision " + revision, e );
		}
		
		/* Line 1 is details */
		
	}
}
