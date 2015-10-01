package net.praqma.vcs.model.clearcase;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.praqma.clearcase.api.DiffBl;

import net.praqma.clearcase.changeset.ChangeSet2;
import net.praqma.clearcase.changeset.ChangeSetElement2;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.exceptions.UnknownEntityException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.ChangeSetElement;
import net.praqma.vcs.model.ChangeSetElement.Status;

public class ClearCaseCommit extends AbstractCommit {
	
	private Logger logger = Logger.getLogger();
	protected Baseline baseline;
	protected ClearCaseBranch ccbranch;
    private static final Pattern rx_versionName = Pattern.compile( "^(\\S+)\\s+([\\S\\s.^@]+@@.*)$" );

	public ClearCaseCommit( Baseline baseline, ClearCaseBranch branch, int number ) {
		super( baseline.getFullyQualifiedName(), branch, number );
		
		this.ccbranch = branch;
		this.baseline = baseline;
	}
    
    @Override
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

        @Override
		public boolean perform() {
			logger.debug( "CC: perform load" );

			try {
				ClearCaseCommit.this.parentKey = null;
				ClearCaseCommit.this.author = baseline.getUser();
				ClearCaseCommit.this.committer = baseline.getUser();
				ClearCaseCommit.this.authorDate = baseline.getDate();
				ClearCaseCommit.this.setCommitterDate(baseline.getDate());
	
				ClearCaseCommit.this.title = baseline.getFullyQualifiedName();
                
                //This one has been replaced with diffbl
				//ChangeSet2 changeset = ChangeSet2.getChangeSet( baseline, null, ccbranch.getOuputSnapshotView().getViewRoot() );
                
                DiffBl diffBl = new DiffBl(baseline, null).setViewRoot(ccbranch.getOuputSnapshotView().getViewRoot()).setVersions(true).setNmerge(true);                
                List<String> s = diffBl.execute();
                
                ChangeSet2 changeset = new ChangeSet2( ccbranch.getOuputSnapshotView().getViewRoot());
            
                for(String line : s) {
                    Matcher m = rx_versionName.matcher(line);
                    if(m.find()) {
                        String f = m.group( 2 ).trim();
                        Version version = (Version) UCMEntity.getEntity( m.group( 2 ).trim()).load();
                        changeset.addVersion(version);
                    }
                }
                
                

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
						
						logger.debug(element.getFile() + " " + cse.getStatus() );
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
						logger.debug(element.getFile() + " " + cse.getStatus() );
						ClearCaseCommit.this.changeSet.put( element.getFile().toString(), cse );
						continue;
					}
					
					/* Deleted */
					if( element.getStatus().equals( net.praqma.clearcase.ucm.entities.Version.Status.DELETED ) ) {
						ChangeSetElement cse = new ChangeSetElement( new File( element.getFile().getAbsoluteFile().toString().substring( length ) ), Status.DELETED );
						logger.debug(element.getFile() + " " + cse.getStatus() );
						ClearCaseCommit.this.changeSet.put( element.getFile().toString(), cse );
					}				
				}
					
			} catch( CleartoolException | UnableToInitializeEntityException | UnknownEntityException | UnableToLoadEntityException | UCMEntityNotFoundException e ) {
				logger.warning( "Could not get differences: " + e.getMessage() );
			}
			
			return true;
		}
	}
	
	public Baseline getBaseline() {
		return baseline;
	}

}
