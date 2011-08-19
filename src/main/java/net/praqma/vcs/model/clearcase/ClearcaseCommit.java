package net.praqma.vcs.model.clearcase;


import java.util.List;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;

public class ClearcaseCommit extends AbstractCommit {
	
	private Logger logger = Logger.getLogger();
	private Baseline baseline;
	private ClearcaseBranch ccbranch;

	public ClearcaseCommit( Baseline baseline, ClearcaseBranch branch, int number ) {
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

		public boolean perform() {
			logger.debug( "CC: perform load" );

			try {
				ClearcaseCommit.this.parentKey = null;
				ClearcaseCommit.this.author = baseline.getUser();
				ClearcaseCommit.this.committer = baseline.getUser();
				ClearcaseCommit.this.authorDate = baseline.getDate();
				ClearcaseCommit.this.committerDate = baseline.getDate();
	
				ClearcaseCommit.this.title = ( baseline.getComment() != null ? baseline.getComment() : baseline.getFullyQualifiedName() );
				List<Version> versions = baseline.beforeBaselineDifferences( null, ccbranch.getSnapshotView() );
				
				for( Version v : versions ) {
					System.out.println( "FILE: " + v.getVersion() );
				}
				
				/*
				BaselineDiff diffs = baseline.getDifferences( ((ClearcaseBranch)branch).getSnapshotView() );
	
				for(int i = 8 ; i < result.size() ; i++) {
					
					Matcher m = rx_getChangeFile.matcher( result.get( i ) );
					if( m.find() ) {
						logger.debug("Line(change): " + result.get( i ));
						GitCommit.this.changeSet.put( m.group(3), new ChangeSetElement( new File( m.group(3) ), Status.CHANGED ) );
						continue;
					}
					
					Matcher m2 = rx_getCreateFile.matcher( result.get( i ) );
					if( m2.find() ) {
						logger.debug("Line(create): " + result.get( i ));
						GitCommit.this.changeSet.put( m2.group(1), new ChangeSetElement( new File( m2.group(1) ), Status.CREATED ) );
						continue;
					}
					
					Matcher m3 = rx_getDeleteFile.matcher( result.get( i ) );
					if( m3.find() ) {
						logger.debug("Line(delete): " + result.get( i ));
						GitCommit.this.changeSet.put( m3.group(1), new ChangeSetElement( new File( m3.group(1) ), Status.DELETED ) );
						continue;
					}
				}
				*/
					
			} catch( UCMException e ) {
				logger.warning( "Could not get differences: " + e.getMessage() );
			}
			
			return true;
		}
	}

}
