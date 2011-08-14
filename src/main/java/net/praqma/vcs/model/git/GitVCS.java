package net.praqma.vcs.model.git;

import java.io.File;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractVCS;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.git.api.Git;
import net.praqma.vcs.model.git.exceptions.GitException;
import net.praqma.vcs.util.CommandLine;

public class GitVCS extends AbstractVCS {
	
	private Logger logger = Logger.getLogger();

	public GitVCS( File location ) {
		super( location );
	}
	
	public static GitVCS create( File location) throws ElementNotCreatedException {
		GitVCS git = new GitVCS( location );
		git.initialize();
		return git;
	}
	
	
	@Override
	public void initialize() throws ElementNotCreatedException {
		logger.info( "Initializing git repository " + location );
		doInitialize( new InitializeImpl() );
	}
	
	public class InitializeImpl extends Initialize {
		public boolean initialize() {
			location.mkdirs();
			try {
				Git.initialize( location.getAbsoluteFile() );
			} catch( GitException e ) {
				logger.warning( "Could not initialize repository at " + location.getAbsolutePath() );
				return false;
			}
			return true;
		}
	}
	
	/*
	public void changeBranch( AbstractBranch branch ) {
		super.changeBranch( branch );
	}
	*/
	
	


}
