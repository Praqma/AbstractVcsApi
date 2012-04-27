package net.praqma.ava.model.git;

import java.io.File;

import net.praqma.util.debug.Logger;
import net.praqma.ava.model.AbstractVCS;
import net.praqma.ava.model.exceptions.ElementDoesNotExistException;
import net.praqma.ava.model.exceptions.ElementException;
import net.praqma.ava.model.exceptions.ElementException.FailureType;
import net.praqma.ava.model.exceptions.ElementNotCreatedException;
import net.praqma.ava.model.git.api.Git;
import net.praqma.ava.model.git.exceptions.GitException;

public class GitVCS extends AbstractVCS {
	
	private Logger logger = Logger.getLogger();

	public GitVCS( File location ) {
		super( location );
	}
	
	public static GitVCS create( File location) throws ElementNotCreatedException, ElementDoesNotExistException {
		GitVCS git = new GitVCS( location );
		git.initialize();
		return git;
	}
	
	public boolean exists() {
		return Git.repositoryExists( location );
	}
	
	
	@Override
	public void initialize() throws ElementNotCreatedException, ElementDoesNotExistException {
		initialize(false);
	}
	
	public void initialize( boolean get ) throws ElementNotCreatedException, ElementDoesNotExistException {
		logger.info( "Initializing git repository " + location );
		InitializeImpl init = new InitializeImpl(get);
		doInitialize( init );
	}
	
	public class InitializeImpl extends Initialize {
		public InitializeImpl( boolean get ) {
			super( get );
		}

		public boolean initialize() throws ElementNotCreatedException {
			location.mkdirs();
			try {
				Git.initialize( location.getAbsoluteFile() );
			} catch( GitException e ) {
				logger.warning( "Could not initialize repository at " + location.getAbsolutePath() );
				throw new ElementNotCreatedException( "Could not initialize Git repository", FailureType.INITIALIZATON, e );
			}
			return true;
		}
	}
	
	public void get() throws ElementDoesNotExistException {
		try {
			get(false);
		} catch( ElementNotCreatedException e ) {
			/* This should not happen */
			/* TODO Should we throw DoesNotExist? */
		}
	}

	@Override
	public void get( boolean initialize ) throws ElementNotCreatedException, ElementDoesNotExistException {
		if( initialize ) {
			initialize(true);
		} else {
			if( !exists() ) {
				throw new ElementDoesNotExistException( "Git repository at " + location + " does not exist" );
			}
		}
	}

	@Override
	public boolean cleanup() {
		return true;
	}
	
}
