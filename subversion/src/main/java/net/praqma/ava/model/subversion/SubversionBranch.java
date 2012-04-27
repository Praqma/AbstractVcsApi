package net.praqma.ava.model.subversion;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.praqma.ava.VersionControlSystems;
import net.praqma.ava.model.AbstractBranch;
import net.praqma.ava.model.AbstractCommit;
import net.praqma.ava.model.Repository;
import net.praqma.ava.model.exceptions.ElementAlreadyExistsException;
import net.praqma.ava.model.exceptions.ElementDoesNotExistException;
import net.praqma.ava.model.exceptions.ElementNotCreatedException;
import net.praqma.ava.model.exceptions.UnableToCheckoutCommitException;
import net.praqma.ava.model.subversion.api.Subversion;
import net.praqma.ava.model.subversion.exceptions.SubversionException;

public class SubversionBranch extends AbstractBranch {

	public SubversionBranch( File localRepositoryPath, String name, Repository parent ) {
		super( localRepositoryPath, name, parent );
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean cleanup() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initialize() throws ElementNotCreatedException, ElementAlreadyExistsException {
		try {
			initialize( false );
		} catch( ElementDoesNotExistException e ) {
			/* This shouldn't be possible */
			logger.fatal( "False shouldn't throw exist exceptions!!!" );
		}
	}

	@Override
	public void initialize( boolean get ) throws ElementNotCreatedException, ElementAlreadyExistsException, ElementDoesNotExistException {
		InitializeImpl init = new InitializeImpl( get );
		doInitialize( init );
	}

	private class InitializeImpl extends Initialize {

		public InitializeImpl( boolean get ) {
			super( get );
		}

		public boolean initialize() throws ElementNotCreatedException, ElementAlreadyExistsException, ElementDoesNotExistException {

			/* Pull first */
			try {
				Subversion.checkout( parent.getLocation(), localRepositoryPath );
			} catch( SubversionException e ) {
				throw new ElementDoesNotExistException( parent.getLocation(), e );
			}

			/* Switch or not */

			return true;
		}

	}

	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void get() throws ElementDoesNotExistException {
		// TODO Auto-generated method stub

	}

	@Override
	public void get( boolean initialize ) throws ElementNotCreatedException, ElementDoesNotExistException {
		// TODO Auto-generated method stub

	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkoutCommit( AbstractCommit commit ) throws UnableToCheckoutCommitException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<AbstractCommit> getCommits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AbstractCommit> getCommits( boolean load ) {
		return getCommits( load, null );
	}

	@Override
	public List<AbstractCommit> getCommits( boolean load, Date offset ) {
		try {
			List<Integer> list = Subversion.getRevisions( offset, null, localRepositoryPath );
			List<AbstractCommit> commits = new ArrayList<AbstractCommit>();
			
			for( int i : list ) {
				SubversionCommit c = new SubversionCommit( i + "", this, i );
				c.load();
				commits.add( c );
			}
			
			return commits;
		} catch( SubversionException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public VersionControlSystems getVersionControlSystem() {
		// TODO Auto-generated method stub
		return null;
	}

}
