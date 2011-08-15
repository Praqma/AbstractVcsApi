package net.praqma.vcs.model.git;

import java.io.File;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractVCS;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.git.api.Git;
import net.praqma.vcs.model.git.exceptions.GitException;

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
	public boolean initialize() throws ElementNotCreatedException {
		return initialize(false);
	}
	
	public boolean initialize( boolean get ) throws ElementNotCreatedException {
		logger.info( "Initializing git repository " + location );
		return doInitialize( new InitializeImpl(get) );
	}
	
	public class InitializeImpl extends Initialize {
		public InitializeImpl( boolean get ) {
			super( get );
		}

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

	@Override
	public boolean get() throws ElementNotCreatedException {
		return initialize(true);
	}
	
}
