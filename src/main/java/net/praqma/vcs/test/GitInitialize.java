package net.praqma.vcs.test;

import java.io.File;

import net.praqma.vcs.model.Repository;
import net.praqma.vcs.model.exceptions.ElementException;
import net.praqma.vcs.model.git.GitBranch;
import net.praqma.vcs.model.git.GitVCS;

public class GitInitialize {

	public static void main( String[] args ) throws ElementException {
		File rpath = new File( "c:/temp/git_tests/VCS-test2" );
		GitVCS git = new GitVCS( rpath );
		git.initialize();
		
		Repository parent = new Repository( "git://github.com/Praqma/MonKit.git", "origin" );
		GitBranch branch = new GitBranch( rpath, "master", parent );
		branch.initialize( true );

	}

}
