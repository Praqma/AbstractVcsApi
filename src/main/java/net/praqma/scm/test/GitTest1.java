package net.praqma.scm.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import net.praqma.exceptions.OperationNotImplementedException;
import net.praqma.exceptions.OperationNotSupportedException;
import net.praqma.exceptions.UnableToPerformException;
import net.praqma.scm.AbstractCommit;
import net.praqma.scm.Repository;
import net.praqma.scm.git.GitSCM;

public class GitTest1 {

	public static void main( String[] args ) throws UnableToPerformException, URISyntaxException, OperationNotSupportedException, MalformedURLException, OperationNotImplementedException {
		File path = new File( "c:\\temp\\git_tests\\repo2" );
		
		//Repository parent = new Repository( "git@github.com:Praqma/MonKit.git", "origin" );
		Repository parent = new Repository( "file://C:/projects/monkit/branches", "origin" );
		//GitSCM.create( new File( "c:\\temp\\git_tests\\repo1" ) );
		
		//GitSCM git = new GitSCM( parent, path, new GitBranch( "master" ) );
		GitSCM git = GitSCM.create( path );
		git.pull(parent);
		
		List<AbstractCommit> commits = git.getCommits();

	}

}
