package net.praqma.vcs.model.git;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.praqma.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.Repository;
import net.praqma.vcs.util.CommandLine;
import net.praqma.vcs.util.Utils;

public class GitBranch extends AbstractBranch{

	public GitBranch( File localRepositoryPath, String name ) throws ElementNotCreatedException {
		super( localRepositoryPath, name );
	}
	
	public GitBranch( File localRepositoryPath, String name, Repository parent ) {
		super( localRepositoryPath, name, parent );
	}

	public static GitBranch create( File localRepository, String name, Repository parent ) {
		GitBranch gb = new GitBranch( localRepository, name, parent );
		gb.initialize();
		return gb;
	}
	
	public static GitBranch get( File localRepository, String name, Repository parent ) {
		GitBranch gb = new GitBranch( localRepository, name, parent );
		gb.initialize();
		return gb;
	}
	
	public boolean initialize() {
		return doInitialize( new InitializeImpl() );
	}
	
	private class InitializeImpl extends Initialize {
		public boolean initialize() {
			/*
			String cmd = "git branch " + name + " .";
			
			try {
				CommandLine.run( cmd, localRepositoryPath.getAbsoluteFile() );
			} catch( Exception e ) {
				logger.warning( "Could not create the branch " + name );
			}
			*/
			
			/* Only do anything if a parent is given
			 * Clone parent */
			if( parent != null ) {
				CommandLine.run( "git clone " + parent.getLocation() + " ." );
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

	public void checkout() {
		doCheckout( new CheckoutImpl(null) );
	}
	
	public void checkout( AbstractCommit commit ) {
		doCheckout( new CheckoutImpl( commit ) );
	}
	
	public class CheckoutImpl extends Checkout {

		public CheckoutImpl( AbstractCommit commit ) {
			super( commit );
		}

		public boolean checkout() {
			logger.debug( "GIT: perform checkout" );
			
			if( parent == null ) {
				System.err.println( "Cannot checkout null branch" );
				logger.warning( "Cannot checkout null branch" );
				return false;
			}
			
			try {
				Git.pull( name, parent.getLocation(), localRepositoryPath );
			} catch (GitException e) {
				System.err.println( "Could not pull git branch" );
				logger.warning( "Could not pull git branch" );
				return false;
			}

			/* Checkout a specific commit */
			if( commit != null ) {
				try {
					logger.info( "Checking out " + commit.getTitle() );
					Git.checkoutCommit( commit.getKey(), localRepositoryPath );
				} catch (GitException e) {
					System.err.println( "Could not pull git branch" );
					logger.warning( "Could not pull git branch" );
					return false;
				}
			}
			
			return true;
		}
	}
	
	@Override
	public List<AbstractCommit> getCommits() {
		return getCommits(false);
	}
	
	@Override
	public List<AbstractCommit> getCommits( boolean load ) {
		logger.info( "Getting git commits for branch " + name );
		
		String cmd = "git rev-list --all";
		List<String> cs = CommandLine.run( cmd, localRepositoryPath.getAbsoluteFile() ).stdoutList;
		
		List<AbstractCommit> commits = new ArrayList<AbstractCommit>();
		
		//for(String c : cs) {
		for( int i = 0 ; i < cs.size() ; i++ ) {
			System.out.print( "\r" + Utils.getProgress( cs.size(), i ) );
			GitCommit commit = new GitCommit(cs.get( i ), GitBranch.this);
			if( load ) {
				commit.load();
			}
			
			commits.add( commit );
		}
		
		System.out.println( " Done" );
		
		Collections.reverse( commits );
		
		return commits;
	}
}
