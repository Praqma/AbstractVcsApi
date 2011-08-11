package net.praqma.vcs.model.clearcase;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.exceptions.UnableToReplayException;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.AbstractReplay;
import net.praqma.vcs.model.ChangeSetElement;

public class ClearcaseReplay extends AbstractReplay {
	
	private ClearcaseBranch ccBranch;
	
	private Logger logger = Logger.getLogger();

	public ClearcaseReplay( ClearcaseBranch branch ) {
		super( branch );
		
		this.ccBranch = branch;
	}

	@Override
	public void replay( AbstractCommit commit ) throws UnableToReplayException {
		doReplay( new ReplayImpl( commit ) );
	}
	
	public class ReplayImpl extends Replay{
		public ReplayImpl( AbstractCommit commit ) {
			super( commit );
		}

		public boolean setup() {
			try {
				Activity activity = Activity.create( null, ccBranch.getPVob(), true, "CCReplay: " + commit.getKey(), ccBranch.getSnapshotView().GetViewRoot() );
			} catch (UCMException e1) {
				logger.error( "ClearCase Activity could not be created: " + e1.getMessage() );
				return false;
			}
			
			try {
				Version.checkOut( ccBranch.getDevelopmentPath(), ccBranch.getDevelopmentPath() );
			} catch (UCMException e1) {
				logger.error( "ClearCase could not checkout path: " + e1.getMessage() );
				return false;
			}
			
			return true;
		}
		
		public boolean replay() {
			List<ChangeSetElement> cs = commit.getChangeSet();
			
			boolean success = true;
			
			for( ChangeSetElement cse : cs ) {
				File file = new File( ccBranch.getDevelopmentPath(), cse.getFile().getPath() );
				logger.debug( "FILE: " + file );
				
				Version version = null;
				if( !file.exists() ) {
					try {
						version = Version.create( file, ccBranch.getSnapshotView() );
					} catch (UCMException e1) {
						logger.error( "ClearCasecould not create version: " + e1.getMessage() );
						success = false;						
						continue;
					}
				} else {
					try {
						version = Version.getUnextendedVersion( file, ccBranch.getDevelopmentPath() );
					} catch (UCMException e1) {
						logger.error( "ClearCase could not get version: " + e1.getMessage() );
						success = false;
						continue;
					}
				}
				
				logger.debug( "Writing" );
					
				PrintStream ps;
				try {
					ps = new PrintStream( new BufferedOutputStream(new FileOutputStream(version.getVersion(), true) ) );
					ps.println( commit.getKey() + " - " + commit.getAuthorDate() );
					ps.close();
				} catch (FileNotFoundException e) {
					success = false;
					logger.error( e );
				}
			}
			
			return success;
		}
		
		public boolean cleanup( boolean status ) {
			logger.info( "Checking in...." );
			
			boolean success = true;
			
			try {
				Version.checkIn( ccBranch.getDevelopmentPath(), ccBranch.getDevelopmentPath() );
			} catch (UCMException e1) {
				logger.error( "ClearCase could not checkin path: " + e1.getMessage() );
				success = false;
			}
			
			try {
				Baseline.create( "OpenSCM_baseline_" + commit.getKey(), ccBranch.getComponent(), ccBranch.getDevelopmentPath(), true, true );
			} catch (UCMException e1) {
				logger.error( "ClearCase could not create baseline: " + e1.getMessage() );
				success = false;
			}
			
			return success;
		}
	}
	
}