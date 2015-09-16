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
import net.praqma.util.debug.Logger;
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
	
	public class ClearCaseReplayImpl extends Replay{
		public ClearCaseReplayImpl( AbstractCommit commit ) {
			super( commit );
		}

        @Override
		public boolean setup() {
			/* Update? Yes, but we must rebase first! NO! */
			/*
			try {
				Stream parent = ccBranch.getInputStream().getDefaultTarget();
				if( parent != null ) {
					logger.debug( "Trying to rebase against " + parent );
					List<Baseline> baselines = parent.getLatestBaselines();
					if( baselines != null && baselines.size() > 0 ) {
						ccBranch.getInputStream().rebase( ccBranch.getInputSnapshotView(), baselines.get( 0 ), true );
					} else {
						logger.warning( "Unable to rebase to latest baseline!!!" );
					}
				} else {
					logger.debug( "Could not rebase stream, no parent stream given" );
				}
			} catch( Exception e ) {
				logger.warning( "I tried to rebase, but got the error: " + e.getMessage() );
			}
			*/
			ccBranch.update();
			
			try {
				//Activity.create( null, ccBranch.getPVob(), true, "CCReplay: " + commit.getKey(), ccBranch.getInputSnapshotView().getViewRoot() );
                //TODO: 2.0 What is the 'in' stream value here?
                Activity.create(null, null, ccBranch.getPVob(), true, "CCReplay: " + commit.getKey(), null, ccBranch.getInputSnapshotView().getViewRoot());
			} catch (UnableToCreateEntityException | UCMEntityNotFoundException | UnableToGetEntityException | UnableToInitializeEntityException e1) {
				logger.error( "ClearCase Activity could not be created: " + e1.getMessage() );
				return false;
			}

			
			/* Checkout component */
			/* TODO Is this needed? */
			/*
			try {
				Version.checkOut( ccBranch.getDevelopmentPath(), ccBranch.getInputPath() );
			} catch (UCMException e1) {
				logger.error( "ClearCase could not checkout path " + ccBranch.getDevelopmentPath() + ": " + e1.getMessage() );
				return false;
			}
			*/
			
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
				logger.debug( "File(" + cse.getStatus() + "): " + file );
				
				Version version = null;
				
				switch( cse.getStatus() ) {
                    
                    case DELETED:
                        try {
                            version = getFile( file, file.isDirectory() );
                            version.removeName();
                            //version.removeVersion();
                            removeEmptyDirectories( file.getParentFile() );
                        } catch (Exception e1) {
                            logger.error( "ClearCase could not remove name: " + e1.getMessage() );
                            success = false;
                        }
                        break;

                    case CREATED:
                        try {
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
                        File oldfile = new File( ccBranch.getInputPath(), cse.getRenameFromFile().toString() );
                        version = getFile( oldfile, false );

                        /* Write before rename? */
                        write( new File( commit.getBranch().getPath(), cse.getFile().toString() ), oldfile );

                        /* Add to source control */
                        getFile( file.getParentFile(), true );

                        try {
                            logger.debug( "MOVING: " + version.getVersion() );
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
			logger.debug( d + " has " + d.list().length + " elements" );
			while( d.list().length == 0 ) {
				try {
					Version.removeName( d, ccBranch.getInputSnapshotView().getViewRoot() );
					//Version.removeVersion( d, ccBranch.getSnapshotView().GetViewRoot() );
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
		
		protected Version getFile( File file, boolean mkdir )  {
			logger.debug( "GETFILE: " + file );
			Version version = null;
			/* TODO Determine whether the file exists or not */
            boolean underSourceControl = false;
            
            try {
                underSourceControl = Version.isUnderSourceControl( file, ccBranch.getInputSnapshotView().getViewRoot() );
            } catch (CleartoolException ex) {
                logger.error( String.format( "Unable to determine if file %s is under source control", file.getAbsolutePath() ) );
            }
			
			if( !file.exists() || !underSourceControl ) {
				try {
					version = Version.create( file, mkdir, ccBranch.getInputSnapshotView() );
				} catch (CleartoolException | IOException | UnableToCreateEntityException | UCMEntityNotFoundException | UnableToGetEntityException | UnableToLoadEntityException | UnableToInitializeEntityException e1) {
					logger.error( "ClearCase could not create version: " + e1.getMessage() );
				}
			} else {
				try {
					version = Version.getUnextendedVersion( file, ccBranch.getInputPath() );
					version.setView( ccBranch.getInputSnapshotView() );
					version.checkOut();
				} catch (IOException | CleartoolException | UnableToLoadEntityException | UCMEntityNotFoundException | UnableToInitializeEntityException e1) {
					logger.error( "ClearCase could not get version: " + e1.getMessage() );
				}
			}
			
			return version;
		}
		
        @Override
		public boolean commit() {
			logger.debug( "Committing ClearCase baseline" );

			try {
				List<File> files = Version.getUncheckedIn( ccBranch.getInputPath() );
				for( File f : files ) {
					logger.debug( "Checking in " + f );
					try {
						Version.checkIn( f, false, ccBranch.getInputPath() );
					} catch( Exception e ) {
						logger.debug( "Unable to checkin " + f );
						/* No op */
					}
				}
			} catch( Exception e ) {
				logger.error( e.getMessage() );
				
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
                logger.error( "ClearCase could not create baseline: " + e1.getMessage() );
                return false;
				
			}
		}
	}	
}
