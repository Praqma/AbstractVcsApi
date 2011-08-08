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
import net.praqma.scm.clearcase.ClearcaseReplay;
import net.praqma.scm.git.GitBranch;
import net.praqma.scm.git.GitSCM;

public class GitTest1 {

	public static void main( String[] args ) throws UnableToPerformException, URISyntaxException, OperationNotSupportedException, MalformedURLException, OperationNotImplementedException {
		File path = new File( "c:\\temp\\git_tests\\repo3" );
		
		//Repository parent = new Repository( "git@github.com:Praqma/MonKit.git", "origin" );
		Repository parent = new Repository( "file://C:/projects/monkit/branches", "origin" );
		//GitSCM.create( new File( "c:\\temp\\git_tests\\repo1" ) );
		
		//GitSCM git = new GitSCM( parent, path, new GitBranch( "master" ) );
		GitBranch branch = new GitBranch( path, "master" );
		GitSCM git = GitSCM.create( branch );
		git.pull(parent);
		
		List<AbstractCommit> commits = git.getCommits();
		
		System.out.println( commits.size() + " commits on branch " + branch );
		
		commits.get( 90 ).load();
		commits.get( 91 ).load();
		commits.get( 92 ).load();

		System.out.println( "Commit #1: " + commits.get( 90 ) );
		
		ClearcaseReplay cr = new ClearcaseReplay( new File( "C:/temp/cc_test/test1" ) );
		cr.replay( commits.get( 90 ) );
		cr.replay( commits.get( 91 ) );
		cr.replay( commits.get( 92 ) );
	}

}
