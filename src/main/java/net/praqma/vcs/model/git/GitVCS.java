package net.praqma.vcs.model.git;

import java.io.File;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractVCS;
import net.praqma.vcs.util.CommandLine;

public class GitVCS extends AbstractVCS {
	
	private Logger logger = Logger.getLogger();

	public GitVCS( File location ) {
		super( location );
	}
	
	public static GitVCS create( File location) {
		GitVCS git = new GitVCS( location );
		git.initialize();
		return git;
	}
	
	
	@Override
	public void initialize() {
		logger.info( "Initializing git repository add " + location );
		doInitialize( new InitializeImpl() );
	}
	
	public class InitializeImpl extends Initialize {
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
