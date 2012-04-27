package net.praqma.ava.model.clearcase;

import java.io.File;
import java.util.List;

import net.praqma.clearcase.changeset.ChangeSet2;
import net.praqma.clearcase.changeset.ChangeSetElement2;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.util.debug.Logger;
import net.praqma.ava.model.AbstractBranch;
import net.praqma.ava.model.AbstractCommit;
import net.praqma.ava.model.ChangeSetElement;
import net.praqma.ava.model.ChangeSetElement.Status;

public class ClearCaseCommit extends AbstractCommit {

	private Logger logger = Logger.getLogger();
	protected Baseline baseline;
	protected ClearCaseBranch ccbranch;

	public ClearCaseCommit( Baseline baseline, ClearCaseBranch branch, int number ) {
		super( baseline.getFullyQualifiedName(), branch, number );

		this.ccbranch = branch;
		this.baseline = baseline;
	}

	public void instantiate( Baseline baseline, AbstractBranch branch, int number ) {

	}

	public void load() {
		LoadImpl load = new LoadImpl();
		doLoad( load );
	}

	public class LoadImpl extends Load {

		public LoadImpl() {
			super();
		}

		protected int getLength() {
			return ccbranch.getPath().toString().length();
		}

		protected void debug() {
			logger.debug( "PATH: " + ccbranch.getPath().toString() );
		}

		public boolean perform() {
			logger.debug( "CC: perform load" );

			try {
				ClearCaseCommit.this.parentKey = null;
				ClearCaseCommit.this.author = baseline.getUser();
				ClearCaseCommit.this.committer = baseline.getUser();
				ClearCaseCommit.this.authorDate = baseline.getDate();
				ClearCaseCommit.this.committerDate = baseline.getDate();

				ClearCaseCommit.this.title = ( baseline.getComment() != null ? baseline.getComment() : baseline.getFullyQualifiedName() );
				ChangeSet2 changeset = ChangeSet2.getChangeSet( baseline, null, ccbranch.getOuputSnapshotView().getViewRoot() );

				logger.debug( "Changeset for " + ClearCaseCommit.this.baseline.getShortname() );

				List<ChangeSetElement2> elements = changeset.getElementsAsList();

				//int length = ccbranch.getDevelopmentPath().toString().length();
				int length = getLength();

				debug();

				for( ChangeSetElement2 element : elements ) {

					logger.debug( "FILE: " + element.getFile() + "(" + element.getFile().getAbsoluteFile().toString().substring( length ) + "(" + length + "))" );

					/* Plain change */
					if( element.getStatus().equals( net.praqma.clearcase.ucm.entities.Version.Status.CHANGED ) ) {
						ChangeSetElement cse = new ChangeSetElement( new File( element.getFile().getAbsoluteFile().toString().substring( length ) ), Status.CHANGED );
						if( element.getOldFile() != null ) {
							cse.setRenameFromFile( new File( element.getOldFile().getAbsoluteFile().toString().substring( length ) ) );
							logger.debug( "I WAS RENAMED FROM " + element.getOldFile() );
							cse.setStatus( Status.RENAMED );
						}

						logger.debug( cse );

						logger.debug( element.getFile() + " " + cse.getStatus() );
						ClearCaseCommit.this.changeSet.put( element.getFile().toString(), cse );
						continue;
					}

					/* Added */
					if( element.getStatus().equals( net.praqma.clearcase.ucm.entities.Version.Status.ADDED ) ) {
						ChangeSetElement cse = new ChangeSetElement( new File( element.getFile().getAbsoluteFile().toString().substring( length ) ), Status.CREATED );
						if( element.getOldFile() != null ) {
							cse.setRenameFromFile( element.getOldFile() );
							cse.setStatus( Status.RENAMED );
						}
						logger.debug( element.getFile() + " " + cse.getStatus() );
						ClearCaseCommit.this.changeSet.put( element.getFile().toString(), cse );
						continue;
					}

					/* Deleted */
					if( element.getStatus().equals( net.praqma.clearcase.ucm.entities.Version.Status.DELETED ) ) {
						ChangeSetElement cse = new ChangeSetElement( new File( element.getFile().getAbsoluteFile().toString().substring( length ) ), Status.DELETED );
						logger.debug( element.getFile() + " " + cse.getStatus() );
						ClearCaseCommit.this.changeSet.put( element.getFile().toString(), cse );
						continue;
					}

				}

			} catch( ClearCaseException e ) {
				logger.warning( "Could not get differences: " + e.getMessage() );
			}

			return true;
		}
	}

	public Baseline getBaseline() {
		return baseline;
	}

}
