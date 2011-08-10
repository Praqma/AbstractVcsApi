package net.praqma.vcs.model.git;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.AbstractVCS;
import net.praqma.vcs.model.Repository;
import net.praqma.vcs.util.CommandLine;

public class GitVCS extends AbstractVCS {
	
	private Logger logger = Logger.getLogger();

	public GitVCS( File location ) {
		super( location );
	}
	
	public static GitVCS create( File location) {
		GitVCS git = new GitVCS( location );
		git.initialize( location );
		return git;
	}
	
	
	@Override
	public void initialize( File location ) {
		doInitialize( new InitializeImpl( location ) );
	}
	
	public class InitializeImpl extends Initialize {
		public InitializeImpl( File location ) {
			super( location );
		}

		public boolean initialize() {
			String cmd = "git init";
			CommandLine.run( cmd, location.getAbsoluteFile() );
			return true;
		}
	}
	
	/*
	public void changeBranch( AbstractBranch branch ) {
		super.changeBranch( branch );
	}
	*/
	
	


}
