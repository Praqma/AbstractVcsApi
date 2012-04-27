package net.praqma.ava.model.subversion.test;

import java.io.File;
import java.util.List;

import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;
import net.praqma.ava.model.AbstractCommit;
import net.praqma.ava.model.Repository;
import net.praqma.ava.model.exceptions.ElementAlreadyExistsException;
import net.praqma.ava.model.exceptions.ElementNotCreatedException;
import net.praqma.ava.model.subversion.SubversionBranch;

public class Test {

	private static Logger logger = Logger.getLogger();
	public static Appender app = new ConsoleAppender();
	
	public static void main( String[] args ) throws ElementNotCreatedException, ElementAlreadyExistsException {
		app.setMinimumLevel( LogLevel.DEBUG );
		Logger.addAppender( app );
		
		SubversionBranch b = new SubversionBranch( new File( "c:/temp/ava/test" ), "trunk", new Repository( "http://wolfgang-PC:8181/svn/code/dev/ALM/trunk", "Blaha" ) );
		b.initialize();
		List<AbstractCommit> commits = b.getCommits( true );
		logger.debug( "COMMITS: " + commits );
	}
}
