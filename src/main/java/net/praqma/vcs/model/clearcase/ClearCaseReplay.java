package net.praqma.vcs.model.clearcase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.NothingNewException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;

import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Version;

import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.AbstractReplay;
import net.praqma.vcs.model.ChangeSetElement;
import net.praqma.vcs.model.ChangeSetElement.Status;
import net.praqma.vcs.model.clearcase.listeners.ClearcaseReplayListener;
import net.praqma.vcs.model.exceptions.UnableToReplayException;
import net.praqma.vcs.model.exceptions.UnsupportedBranchException;

public class ClearCaseReplay extends AbstractReplay {
	
	protected ClearCaseBranch ccBranch;

	public ClearCaseReplay( ClearCaseBranch branch ) {
		super( branch );
		
		this.ccBranch = branch;
	}
	
	public ClearCaseReplay( AbstractBranch branch ) throws UnsupportedBranchException {
		super( branch );
		if( branch instanceof ClearCaseBranch ) {
			this.ccBranch = (ClearCaseBranch)branch;
		} else {
			throw new UnsupportedBranchException( "ClearCase replays only supports ClearCase branches" );
		}
	}
	
	public void setBranch( ClearCaseBranch branch ) {
		super.setBranch( branch );
		this.ccBranch = branch;
	}

	@Override
	public void replay( AbstractCommit commit ) throws UnableToReplayException {
		ClearcaseReplayListener.runReplay( this, commit );
		doReplay( new ClearCaseReplayImpl( commit ) );
	}
	
	public class ClearCaseReplayImpl extends Replay {
		public ClearCaseReplayImpl( AbstractCommit commit ) {
			super( commit );
		}

        @Override
		public boolean setup() {
            ccBranch.update();
			
			try {                
                String activityName = "CCReplay: " + commit.getKey();
                logger.fine("Creating activity: "+activityName); 
				Activity.create(
                        "id_"+commit.getKey(), 
                        ccBranch.getInputStream(),
                        ccBranch.getPVob(),
                        true,
                        activityName,
                        activityName,
                        ccBranch.getInputSnapshotView().getViewRoot() );
			} catch (UnableToCreateEntityException | UCMEntityNotFoundException | UnableToGetEntityException | UnableToInitializeEntityException e1) {
				logger.log(Level.SEVERE, "ClearCase Activity could not be created", e1);
				return false;
			}
			
			return true;
		}
		
		protected File getChangeSetFile( ChangeSetElement cse ) {
			return new File( ccBranch.getInputPath(), cse.getFile().toString() );
		}
		
        @Override
		public boolean replay() {
			List<ChangeSetElement> cs = commit.getChangeSet().asList();
			
			boolean success = true;
			
			for( ChangeSetElement cse : cs ) {
				File file = getChangeSetFile( cse );
				logger.fine( "File(" + cse.getStatus() + "): " + file );
				
				Version version = null;
				
				switch( cse.getStatus() ) {

                    case CREATED:
                        try {
                            version = getFile( file, file.isDirectory() );
                            version.getFile().createNewFile();
                        } catch (IOException e1) {
                            logger.warning( "Could not create file: " + e1.getMessage() );

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
                            logger.log(Level.SEVERE, "File not found: "+ version.getFile().getAbsolutePath(), e );
                        } catch (IOException e) {
                            success = false;
                            logger.log(Level.SEVERE, "Could not write to file(" + version.getFile().getAbsolutePath() + ")", e);
                        } finally {
                            try {
                                if(in != null) in.close();
                                if(out != null) out.close();
                            } catch (Exception e) {
                                logger.fine("Unable to close in and output streams");
                            }

                        }

                        break;

                    case RENAMED:
                        File oldfile = new File( ccBranch.getInputPath(), cse.getRenameFromFile().toString() );
                        version = getFile( oldfile, false );

                        /* Write before rename? */
                        write( new File( commit.getBranch().getPath(), cse.getFile().toString() ), oldfile );

                        /* Add to source control */
                        getFile( file.getParentFile(), true );

                        try {
                            logger.fine( "MOVING: " + version.getVersion() );
                            version.moveFile( file );
                        } catch( UnableToLoadEntityException | CleartoolException e ) {
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
			logger.fine( d + " has " + d.list().length + " elements" );
			while( d.list().length == 0 ) {
				try {
					Version.removeName( d, ccBranch.getInputSnapshotView().getViewRoot() );
				} catch (Exception e) {
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
				
				logger.fine( "Writing..." );
				
			    byte[] buf = new byte[1024];
			    int len;
			    while ((len = in.read(buf)) > 0) {
			        out.write(buf, 0, len);
			    }
			    
			    logger.fine( "... Done" );

			} catch (IOException e) {
				logger.log(Level.SEVERE, "Could not write to file(" + dst + ")", e);
			} finally {
				try {
					if(in != null) in.close();
					if(out != null) out.close();
				} catch (IOException e) {
					logger.warning( "Could not close files: " + e.getMessage() );
				}
				
			}
		}
		
		protected Version getFile( File file, boolean mkdir )  {			
			Version version = null;
			/* TODO Determine whether the file exists or not */
            boolean underSourceControl = false;
            
            try {
                underSourceControl = Version.isUnderSourceControl( file, ccBranch.getInputSnapshotView().getViewRoot() );
            } catch (CleartoolException ex) {
                logger.fine( String.format( "Unable to determine if file %s is under source control", file.getAbsolutePath() ) );
            }
			
			if( !file.exists() || !underSourceControl ) {
				try {
					version = Version.create( file, mkdir, ccBranch.getInputSnapshotView() );
				} catch (CleartoolException | IOException | UnableToCreateEntityException | UCMEntityNotFoundException | UnableToGetEntityException | UnableToLoadEntityException | UnableToInitializeEntityException e1) {
					logger.log(Level.WARNING, "ClearCase could not create version", e1 );
				}
			} else {
				try {
					version = Version.getUnextendedVersion( file, ccBranch.getInputPath() );
					version.setView( ccBranch.getInputSnapshotView() );
					version.checkOut();
				} catch (IOException | CleartoolException | UnableToLoadEntityException | UCMEntityNotFoundException | UnableToInitializeEntityException e1) {
					logger.log(Level.WARNING, "ClearCase could not get version ", e1 );
				}
			}
			
			return version;
		}
		
        @Override
		public boolean commit() {
			logger.fine( "Committing ClearCase baseline" );

			try {
				List<File> files = Version.getUncheckedIn( ccBranch.getInputPath() );
				for( File f : files ) {
					logger.fine( "Checking in " + f );
					try {
						Version.checkIn( f, false, ccBranch.getInputPath() );
					} catch( Exception e ) {
						logger.fine( "Unable to checkin " + f );
						/* No op */
					}
				}
			} catch( Exception e ) {
				logger.log(Level.SEVERE, "Unable to commit clearcase baseline", e);
				
			}
			String baselineName = ClearcaseReplayListener.runSelectBaselineName( commit );

			try {
				Baseline.create( baselineName, ccBranch.getComponent(), ccBranch.getInputPath(), Baseline.LabelBehaviour.INCREMENTAL, false );
				logger.info( "New baseline created" );
				return true;
            } catch (NothingNewException nnew) {
                logger.info( "No new baseline created, nothing changed" );
                return false;                
			} catch( UnableToInitializeEntityException | UnableToCreateEntityException e1 ) {
                logger.log(Level.SEVERE, "ClearCase could not create baseline", e1);
                return false;
				
			}
		}
	}	
}
