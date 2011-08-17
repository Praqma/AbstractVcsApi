package net.praqma.vcs.test;

import java.io.File;
import java.util.List;

import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.Repository;
import net.praqma.vcs.model.exceptions.ElementException;
import net.praqma.vcs.model.git.GitBranch;
import net.praqma.vcs.model.git.GitVCS;

public class GitCommitTest2 {

	public static void main( String[] args ) throws ElementException {

		GitBranch branch = new GitBranch( new File( "c:/projects/other/ccbridgetest" ), "master" );
		branch.get();
		List<AbstractCommit> commits = branch.getCommits(true);
		for( AbstractCommit commit : commits ) {
			System.out.println( commit );
		}
	}

}
