package net.praqma.vcs.model.clearcase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.utils.BaselineList;
import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.AbstractReplay;
import net.praqma.vcs.model.ChangeSetElement;
import net.praqma.vcs.model.ChangeSetElement.Status;
import net.praqma.vcs.model.clearcase.listeners.ClearcaseReplayListener;
import net.praqma.vcs.model.exceptions.UnableToReplayException;
import net.praqma.vcs.model.exceptions.UnsupportedBranchException;

public class ClearcaseReplay extends AbstractReplay {
	
	protected ClearcaseBranch ccBranch;
	
	private Logger logger = Logger.getLogger();

	public ClearcaseReplay( ClearcaseBranch branch ) {
		super( branch );
		
		this.ccBranch = branch;
	}
	
	public ClearcaseReplay( AbstractBranch branch ) throws UnsupportedBranchException {
		super( branch );
		if( branch instanceof ClearcaseBranch ) {
			this.ccBranch = (ClearcaseBranch)branch;
		} else {
			throw new UnsupportedBranchException( "ClearCase replays only supports ClearCase branches" );
		}
	}
	
	public void setBranch( ClearcaseBranch branch ) {
		super.setBranch( branch );
		this.ccBranch = branch;
	}

	@Override
	public void replay( AbstractCommit commit ) throws UnableToReplayException {
		ClearcaseReplayListener.runReplay( this, commit );
		doReplay( new ClearCaseReplayImpl( commit ) );
	}
	
	public class ClearCaseReplayImpl extends Replay{
		public ClearCaseReplayImpl( AbstractCommit commit ) {
			super( commit );
		}

		public boolean setup() {
			/* Update? Yes, but we must rebase first! */
			try{
				Stream parent = ccBranch.getInputStream().getDefaultTarget();
				logger.debug( "Trying to rebase against " + parent );
				if( parent != null ) {
					BaselineList baselines = parent.getLatestBaselines();
					if( baselines != null && baselines.size() > 0 ) {
						ccBranch.getInputStream().rebase( ccBranch.getSnapshotView(), baselines.get( 0 ), true );
					} else {
						logger.warning( "Unable to rebase to latest baseline!!!" );
					}
				}
			} catch( Exception e ) {
				logger.warning( "I tried to rebase, but got the error: " + e.getMessage() );
			}
			ccBranch.update();
			
			try {
				Activity.create( null, ccBranch.getPVob(), true, "CCReplay: " + commit.getKey(), ccBranch.getSnapshotView().getViewRoot() );
			} catch (UCMException e1) {
				logger.error( "ClearCase Activity could not be created: " + e1.getMessage() );
				return false;
			}

			
			/* Checkout component */
			/* Is this needed? */
			try {
				Version.checkOut( ccBranch.getDevelopmentPath(), ccBranch.getInputPath() );
			} catch (UCMException e1) {
				logger.error( "ClearCase could not checkout path " + ccBranch.getDevelopmentPath() + ": " + e1.getMessage() );
				return false;
			}
			
			return true;
		}
		
		protected File getChangeSetFile( ChangeSetElement cse ) {
			return new File( ccBranch.getDevelopmentPath(), cse.getFile().toString() );
		}
		
		public boolean replay() {
			List<ChangeSetElement> cs = commit.getChangeSet().asList();
			
			boolean success = true;
			
			for( ChangeSetElement cse : cs ) {
				//File file = new File( ccBranch.getDevelopmentPath(), cse.getFile().toString() );
				File file = getChangeSetFile( cse );
				//File file = new File( ccBranch.getInputPath(), cse.getFile().toString() );
				logger.debug( "File: " + file.isFile() );
				logger.debug( "CSE : " + cse.getFile().isFile() );
				logger.debug( "File(" + cse.getStatus() + "): " + file );
				
				Version version = null;
				
				switch( cse.getStatus() ) {
				case DELETED:
					try {
						version = getFile( file, file.isDirectory() );
						version.removeName();
						//version.removeVersion();
						removeEmptyDirectories( file.getParentFile() );
					} catch (UCMException e1) {
						logger.error( "ClearCase could not remove name: " + e1.getMessage() );
						success = false;
						continue;
					}
					break;
					
				case CREATED:
					try {
						logger.info( "Creating file: " + file );
						version = getFile( file, file.isDirectory() );
						version.getFile().createNewFile();
					} catch (IOException e1) {
						logger.warning( "Could not create file: " + e1.getMessage() );
						/* Continue anyway */
					}
				case CHANGED:
					if( cse.getStatus().equals( Status.CHANGED ) ) {
						version = getFile( file, file.isDirectory() );
					}
					InputStream in = null;
					OutputStream out = null;
					try {
						in = new FileInputStream( new File( commit.getBranch().getPath(), cse.getFile().toString() ));
						out = new FileOutputStream(version.getFile());
						
					    byte[] buf = new byte[1024];
					    int len;
					    while ((len = in.read(buf)) > 0) {
					        out.write(buf, 0, len);
					    }
						
					} catch (FileNotFoundException e) {
						success = false;
						logger.error( "Could not write to file(" + version.getFile().getAbsolutePath() + "): " + e );
					} catch (IOException e) {
						success = false;
						logger.error( "Could not write to file(" + version.getFile().getAbsolutePath() + "): " + e );
					} finally {
						try {
							in.close();
							out.close();
						} catch (Exception e) {
							logger.warning( "Could not close files: " + e.getMessage() );
						}
						
					}

					break;
					
				case RENAMED:
					File oldfile = new File( ccBranch.getPathIn(), cse.getRenameFromFile().toString() );
					version = getFile( oldfile, false );
					
					/* Write before rename? */
					write( new File( commit.getBranch().getPath(), cse.getFile().toString() ), oldfile );
					
					/* Add to source control */
					getFile( file.getParentFile(), true );
					
					try {
						logger.debug( "MOVING: " + version.getVersion() );
						version.moveFile( file );
					} catch( UCMException e ) {
						logger.warning( "Could not rename file" );
					}
					
					/* Clear out empty directories */
					removeEmptyDirectories( oldfile.getParentFile() );
					
					break;
				}
				
			}
			
			return success;
		}
		
		protected void removeEmptyDirectories( File d ) {			
			logger.debug( d + " has " + d.list().length + " elements" );
			while( d.list().length == 0 ) {
				try {
					Version.removeName( d, ccBranch.getSnapshotView().getViewRoot() );
					//Version.removeVersion( d, ccBranch.getSnapshotView().GetViewRoot() );
				} catch (UCMException e) {
					logger.warning( "Could not remove version " + d );
				}
				
				d = d.getParentFile();
			}
		}
		
		protected void write( File src, File dst ) {
			InputStream in = null;
			OutputStream out = null;
			
			try {
				in = new FileInputStream( src );
				out = new FileOutputStream( dst );
				
				logger.debug( "Writing..." );
				
			    byte[] buf = new byte[1024];
			    int len;
			    while ((len = in.read(buf)) > 0) {
			        out.write(buf, 0, len);
			    }
			    
			    logger.debug( "... Done" );

			} catch (FileNotFoundException e) {
				logger.error( "Could not write to file(" + dst + "): " + e );
			} catch (IOException e) {
				logger.error( "Could not write to file(" + dst + "): " + e );
			} finally {
				try {
					in.close();
					out.close();
				} catch (IOException e) {
					logger.warning( "Could not close files: " + e.getMessage() );
				}
				
			}
		}
		
		protected Version getFile( File file, boolean mkdir ) {
			logger.debug( "GETFILE: " + file );
			Version version = null;
			/* TODO Determine whether the file exists or not */
			
			if( !file.exists() || !Version.isUnderSourceControl( file, ccBranch.getSnapshotView().getViewRoot() ) ) {
				try {
					version = Version.create( file, mkdir, ccBranch.getSnapshotView() );
				} catch (UCMException e1) {
					logger.error( "ClearCase could not create version: " + e1.getMessage() );
				}
			} else {
				try {
					version = Version.getUnextendedVersion( file, ccBranch.getPathIn() );
					version.setView( ccBranch.getSnapshotView() );
					version.checkOut();
				} catch (UCMException e1) {
					logger.error( "ClearCase could not get version: " + e1.getMessage() );
				}
			}
			
			return version;
		}
		
		public boolean cleanup( boolean status ) {
			logger.debug( "Cleaning up Clearcase" );
			
			boolean success = true;
			
			try {
				List<File> files = Version.getUncheckedIn( ccBranch.getPathIn() );
				for( File f : files ) {
					Version.checkIn( f, false, ccBranch.getPathIn() );
				}
			} catch (UCMException e) {
				logger.error( e.getMessage() );
				success = false;
			}
			
			String baselineName = ClearcaseReplayListener.runSelectBaselineName( commit );
			
			try {
				//Baseline.create( baselineName, ccBranch.getComponent(), ccBranch.getDevelopmentPath(), true, true );
				Baseline.create( baselineName, ccBranch.getComponent(), ccBranch.getPathIn(), true, false );
			} catch (UCMException e1) {
				logger.error( "ClearCase could not create baseline: " + e1.getMessage() );
				success = false;
			}
			
			return success;
		}
	}
	
}
