package net.praqma.vcs.model.mercurial;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.praqma.vcs.VersionControlSystems;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.Repository;
import net.praqma.vcs.model.exceptions.ElementAlreadyExistsException;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.mercurial.api.Mercurial;
import net.praqma.vcs.model.mercurial.exceptions.MercurialException;

public class MercurialBranch extends AbstractBranch {
	
	protected String defaultBranch = "default";
	
	public MercurialBranch( File repositoryPath, String name ) throws ElementNotCreatedException {
		super( repositoryPath, name );
	}
	
	public MercurialBranch( File localRepositoryPath, String name, Repository parent ) {
		super( localRepositoryPath, name, parent );
	}

	public static MercurialBranch create( File localRepository, String name, Repository parent ) throws ElementNotCreatedException, ElementAlreadyExistsException {
		MercurialBranch hb = new MercurialBranch( localRepository, name, parent );
		hb.initialize();
		return hb;
	}
	
	public void initialize() throws ElementNotCreatedException, ElementAlreadyExistsException {
		try {
			initialize(false);
		} catch (ElementDoesNotExistException e) {
			/* This shouldn't be possible */
			logger.fatal( "False shouldn't throw exist exceptions!!!" );
		}
	}
	
    @Override
	public void initialize( boolean get ) throws ElementNotCreatedException, ElementAlreadyExistsException, ElementDoesNotExistException {
		InitializeImpl init = new InitializeImpl( get );
		doInitialize( init );
	}
	
	public void get() throws ElementDoesNotExistException {
		try {
			get(false);
		} catch (ElementNotCreatedException e) {
			/* This should not happen */
			/* TODO Should we throw DoesNotExist? */
		}
	}
	
    @Override
	public void get( boolean initialize ) throws ElementNotCreatedException, ElementDoesNotExistException {
		if( initialize ) {
			try{
				initialize(true);
			} catch( ElementAlreadyExistsException e ) {
				/* This should not happen */
				/* TODO Should we throw DoesNotExist? */
			}
		} else {
			if( !exists() ) {
				throw new ElementDoesNotExistException( name + " at " + localRepositoryPath + " does not exist" );
			}
		}
	}
    
	@Override
	public boolean exists() {
		try {
			return Mercurial.branchExists( this.name, localRepositoryPath );
		} catch (MercurialException e) {
			logger.warning( "Branch " + name + " at " + localRepositoryPath + " could not be queried: " + e.getMessage() );
			return false;
		}
	}
	
	private class InitializeImpl extends Initialize {
		public InitializeImpl( boolean get ) {
			super( get );
		}

        @Override
		public boolean initialize() throws ElementNotCreatedException, ElementAlreadyExistsException, ElementDoesNotExistException {

			/* Try to switch branch */
			try {
				logger.debug( "Switching to branch " + MercurialBranch.this.name );
				Mercurial.changeBranch( MercurialBranch.this.name, localRepositoryPath );
			} catch( MercurialException e ) {
				/* Try to create the branch */
				try {
					logger.debug( name + " does not exist, let's create it" );
					Mercurial.createBranch( name, localRepositoryPath );
				} catch( MercurialException e2 ) {
					logger.warning( "Could not create branch " + name );
					throw new ElementNotCreatedException( "Could not create branch " + name + ": " + e2.getMessage() );
				}
			}
			
			/* Only do anything if a parent is given */
			if( parent != null ) {
				
				try {
					Mercurial.pull( parent.getLocation(), parent.getName(), localRepositoryPath );
				} catch( MercurialException e ) {
					logger.warning( "Could not initialize Mercurial branch " + name + " from remote " + parent.getName() + ": " + e.getMessage() );
					throw new ElementNotCreatedException( "Could not initialize Mercurial branch: " + e.getMessage() );
				}
				
			}
			
			return true;
		}
		
	}

    @Override
	public void update() {
		doUpdate( new UpdateImpl() );
	}
	
	public class UpdateImpl extends Update {
        @Override
		public boolean update() {
			logger.debug( "Mercurial: perform checkout" );
			
			/* No need for updating if there's no parent */
			if( parent == null ) {
				logger.debug( "No parent given, nothing to check out" );
				return false;
			} else {
				try {
					Mercurial.pull( parent.getLocation(), name, localRepositoryPath );
				} catch (MercurialException e) {
					System.err.println( "Could not pull Mercurial branch" );
					logger.warning( "Could not pull Mercurial branch" );
					return false;
				}
	
				return true;
			}
		}
	}
    
	@Override
	public void checkoutCommit( AbstractCommit commit ) {
		this.currentCommit = commit;
		try {
			logger.debug( "Checking out " + commit.getTitle() );
			Mercurial.checkoutCommit( commit.getKey(), localRepositoryPath );
		} catch (MercurialException e) {
			System.err.println( "Could not checkout commit" );
			logger.warning( "Could not checkout commit: " + e.getMessage() );
		}
	}
	
	@Override
	public List<AbstractCommit> getCommits() {
		return getCommits(false);
	}
	
	@Override
	public List<AbstractCommit> getCommits( boolean load ) {
		return getCommits( load, null );
	}
	
	@Override
	public List<AbstractCommit> getCommits( boolean load, Date offset ) {
		logger.info( "Getting Mercurial commits for branch " + name );
		
		List<String> cs = null;
		try {
			cs = Mercurial.getCommitHashes( offset, null, localRepositoryPath.getAbsoluteFile() );
		} catch (MercurialException e) {
			logger.warning( "Could not get hashes" );
		}
		
		List<AbstractCommit> commits = new ArrayList<AbstractCommit>();
		
		for( int i = 0 ; i < cs.size() ; i++ ) {
			MercurialCommit commit = new MercurialCommit( cs.get( i ), MercurialBranch.this, i );
			if( load ) {
				commit.load();
			}
			
			commits.add( commit );
		}
		
		System.out.println( " Done" );
		
		return commits;
	}

	@Override
	public boolean cleanup() {
		return true;
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( "Mercurial branch\n" );
		sb.append( "Branch name: " +  name + "\n" );
		sb.append( "Path       : " +  localRepositoryPath + "\n" );
		return sb.toString();
	}
	
	@Override
	public VersionControlSystems getVersionControlSystem() {
		return VersionControlSystems.Mercurial;
	}
}
