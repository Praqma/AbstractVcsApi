package net.praqma.vcs.model.mercurial;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.Repository;
import net.praqma.vcs.model.exceptions.ElementAlreadyExistsException;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.interfaces.Cleanable;
import net.praqma.vcs.model.mercurial.api.Mercurial;
import net.praqma.vcs.model.mercurial.exceptions.MercurialException;
import net.praqma.vcs.util.CommandLine;
import net.praqma.vcs.util.Utils;

public class MercurialBranch extends AbstractBranch{
	
	private String defaultMasterBranch = "master";

	public MercurialBranch( File localRepositoryPath, String name ) throws ElementNotCreatedException {
		super( localRepositoryPath, name );
	}
	
	public MercurialBranch( File localRepositoryPath, String name, Repository parent ) {
		super( localRepositoryPath, name, parent );
	}

	public static MercurialBranch create( File localRepository, String name, Repository parent ) throws ElementNotCreatedException, ElementAlreadyExistsException {
		MercurialBranch gb = new MercurialBranch( localRepository, name, parent );
		gb.initialize();
		return gb;
	}
	
	public void initialize() throws ElementNotCreatedException, ElementAlreadyExistsException {
		try {
			initialize(false);
		} catch (ElementDoesNotExistException e) {
			/* This shouldn't be possible */
			logger.fatal( "False shouldn't throw exist exceptions!!!" );
		}
	}
	
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

		public boolean initialize() throws ElementNotCreatedException, ElementAlreadyExistsException {

			/* Only do anything if a parent is given */
			if( parent != null ) {
				
				try {
					Mercurial.pull( parent.getLocation(), parent.getName(), localRepositoryPath );
				} catch( MercurialException e ) {
					logger.warning( "Could not initialize Git branch " + name + " from remote " + parent.getName() + ": " + e.getMessage() );
					throw new ElementNotCreatedException( "Could not initialize Git branch: " + e.getMessage() );
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

	public void update() {
		doUpdate( new UpdateImpl() );
	}
	
	public class UpdateImpl extends Update {


		public boolean update() {
			logger.debug( "GIT: perform checkout" );
			
			if( parent == null ) {
				logger.info( "No parent given, nothing to check out" );
				return false;
			}
			
			try {
				Mercurial.pull( parent.getLocation(), name, localRepositoryPath );
				//Git.checkoutRemoteBranch( name, parent.getName() + "/" + name, localRepositoryPath );
			} catch (MercurialException e) {
				System.err.println( "Could not pull git branch" );
				logger.warning( "Could not pull git branch" );
				return false;
			}

			return true;
		}
	}
	
	public void checkoutCommit( AbstractCommit commit ) {
		this.currentCommit = commit;
		try {
			logger.info( "Checking out " + commit.getTitle() );
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
		logger.info( "Getting git commits for branch " + name );
		
		String cmd = "";
		cmd = "git rev-list --no-merges --reverse --all";

		List<String> cs = null;
		try {
			cs = CommandLine.run( cmd, localRepositoryPath.getAbsoluteFile() ).stdoutList;
		} catch( Exception e ) {
			/* It is probably just empty */
			cs = new ArrayList<String>();
		}
		
		//Collections.reverse( cs );
		
		List<AbstractCommit> commits = new ArrayList<AbstractCommit>();
		
		//for(String c : cs) {
		for( int i = 0 ; i < cs.size() ; i++ ) {
			System.out.print( "\r" + Utils.getProgress( cs.size(), i ) );
			MercurialCommit commit = new MercurialCommit( cs.get( i ), MercurialBranch.this, i );
			if( load ) {
				commit.load();
				
				/* TODO For now we skip old commits by loading them and checks the author date */
				if( offset != null ) {
					if( commit.getAuthorDate().before( offset ) ) {
						continue;
					}
				}
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
}
