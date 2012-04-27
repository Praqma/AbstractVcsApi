package net.praqma.ava.model.mercurial;

import java.io.File;

import net.praqma.ava.model.mercurial.api.Mercurial;
import net.praqma.ava.model.mercurial.exceptions.MercurialException;
import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractVCS;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;

public class MercurialVCS extends AbstractVCS {
	
	private Logger logger = Logger.getLogger();

	public MercurialVCS( File location ) {
		super( location );
	}
	
	public static MercurialVCS create( File location) throws ElementNotCreatedException, ElementDoesNotExistException {
		MercurialVCS hg = new MercurialVCS( location );
		hg.initialize();
		return hg;
	}
	
	public boolean exists() {
		return Mercurial.repositoryExists( location );
	}
	
	
	@Override
	public void initialize() throws ElementNotCreatedException, ElementDoesNotExistException {
		initialize(false);
	}
	
	public void initialize( boolean get ) throws ElementNotCreatedException, ElementDoesNotExistException {
		logger.info( "Initializing Mercurial repository " + location );
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
				Mercurial.initialize( location.getAbsoluteFile() );
			} catch( MercurialException e ) {
				logger.warning( "Could not initialize repository at " + location.getAbsolutePath() );
				throw new ElementNotCreatedException( "Could not initialize Mercurial repository: " + e.getMessage() );
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
				throw new ElementDoesNotExistException( "Mercurial repository at " + location + " does not exist" );
			}
		}
	}

	@Override
	public boolean cleanup() {
		return true;
	}
	
}
