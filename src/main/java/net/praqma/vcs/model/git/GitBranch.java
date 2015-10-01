package net.praqma.vcs.model.git;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import net.praqma.vcs.VersionControlSystems;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.Repository;
import net.praqma.vcs.model.exceptions.ElementAlreadyExistsException;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.git.api.Git;
import net.praqma.vcs.model.git.exceptions.GitException;

public class GitBranch extends AbstractBranch {
	
	protected String defaultBranch = "master";
	
	public GitBranch( File localRepositoryPath, String name ) throws ElementNotCreatedException {
		super( localRepositoryPath, name );
	}
	
	public GitBranch( File localRepositoryPath, String name, Repository parent ) {
		super( localRepositoryPath, name, parent );
	}

	public static GitBranch create( File localRepository, String name, Repository parent ) throws ElementNotCreatedException, ElementAlreadyExistsException {
		GitBranch gb = new GitBranch( localRepository, name, parent );
		gb.initialize();
		return gb;
	}
	
    @Override
	public void initialize() throws ElementNotCreatedException, ElementAlreadyExistsException {
		try {
			initialize(false);
		} catch (ElementDoesNotExistException e) {
			/* This shouldn't be possible */
			logger.fine( "False shouldn't throw exist exceptions!!!" );
		}
	}
	
    @Override
	public void initialize( boolean get ) throws ElementNotCreatedException, ElementAlreadyExistsException, ElementDoesNotExistException {
		InitializeImpl init = new InitializeImpl( get );
		doInitialize( init );
	}
	
    @Override
	public void get() throws ElementDoesNotExistException {
		try {
			get(false);
		} catch (ElementNotCreatedException e) {
			/* This should not happen */
			/* TODO Should we throw DoesNotExist? */
		}
	}
	
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
			return Git.branchExists( this.name, localRepositoryPath );
		} catch (GitException e) {
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
				logger.fine( String.format("Switching to branch %s",GitBranch.this.name));
                
                if(!Git.branchExists(name, localRepositoryPath)) {
                    Git.changeBranchAndCreate(name, localRepositoryPath);
                } else {
                    Git.changeBranch( GitBranch.this.name, localRepositoryPath );
                }				
			} catch( GitException e ) {
				logger.log(Level.WARNING, String.format("The branch %s does not seem to exist and could not be created", GitBranch.this.name), e);                        
				throw new ElementDoesNotExistException( "The branch " + GitBranch.this.name + " does not seem to exist" );
			}
			
			/* Only do anything if a parent is given */
			if( parent != null ) {
				
				
				try { /* to add remote */
                    
					Git.addRemote( parent.getName(), parent.getLocation(), localRepositoryPath );
				} catch (ElementAlreadyExistsException e1) {
					if( get ) {
						throw e1;
					} else {
						logger.warning(e1.getMessage() );
					}
				} catch (GitException e) {
					throw new ElementNotCreatedException( "Could not initialize Git branch" );
				}
				
				try {
					Git.fetch( localRepositoryPath );
					Git.checkoutRemoteBranch( name, parent.getName() + "/" + name, localRepositoryPath );
				} catch( GitException e ) {
					logger.log(Level.WARNING, String.format("Could not initialize Git branch %s from remote %s ",name, parent.getName()), e);
					throw new ElementNotCreatedException( "Could not initialize Git branch: " + e.getMessage() );
				} catch (ElementAlreadyExistsException e) {
					if( get ) {
						throw e;
					} else {
						logger.warning( e.getMessage() );
					}
				}
				
			} else {
				/*
				CommandLine.run( "git symbolic-ref HEAD refs/heads/" + name );
				File index = new File( ".git/index" );
				index.delete();
				CommandLine.run( "git clean -fdx" );
				*/
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
			logger.fine( "Git perform checkout" );
			
			if( parent == null ) {
				logger.info( "No parent given, nothing to check out" );
				return false;
			}
			
			try {
				Git.pull( parent.getLocation(), name, localRepositoryPath );				
			} catch (GitException e) {
				System.err.println( "Could not pull git branch" );
				logger.warning( "Could not pull git branch" );
				return false;
			}

			return true;
		}
	}
    
	@Override
	public void checkoutCommit( AbstractCommit commit ) {
		this.currentCommit = commit;
		try {
			logger.fine(String.format("Checking out %s", commit.getTitle()));
			Git.checkoutCommit( commit.getKey(), localRepositoryPath );
		} catch (GitException e) {
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
		logger.info( "Getting git commits for branch " + name );
		
		List<String> cs = new ArrayList<>();
		try {
			cs = Git.getCommitHashes( offset, null, localRepositoryPath.getAbsoluteFile() );
		} catch (GitException e) {
			logger.log(Level.SEVERE, "Could not get hashes" );
		}
		
		List<AbstractCommit> c = new ArrayList<>();
        int csSize = cs.size();
		
		for( int i = 0 ; i < csSize; i++ ) {
			GitCommit commit = new GitCommit( cs.get( i ), GitBranch.this, i );
			if( load ) {
				commit.load();
			}
			
			c.add( commit );
		}
	
		return c;
	}

	@Override
	public boolean cleanup() {
		return true;
	}
	
	@Override
	public VersionControlSystems getVersionControlSystem() {
		return VersionControlSystems.Git;
	}
}
