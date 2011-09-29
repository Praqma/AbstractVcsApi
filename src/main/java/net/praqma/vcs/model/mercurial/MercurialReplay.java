package net.praqma.vcs.model.mercurial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.AbstractReplay;
import net.praqma.vcs.model.ChangeSetElement;
import net.praqma.vcs.model.exceptions.UnableToReplayException;
import net.praqma.vcs.model.exceptions.UnsupportedBranchException;
import net.praqma.vcs.model.mercurial.api.Mercurial;
import net.praqma.vcs.model.mercurial.exceptions.MercurialException;

public class MercurialReplay extends AbstractReplay{

	public MercurialReplay( MercurialBranch branch ) {
		super( branch );
	}
	
	public MercurialReplay( AbstractBranch branch ) throws UnsupportedBranchException {
		super( branch );
		if( branch instanceof MercurialBranch ) {
		} else {
			throw new UnsupportedBranchException( "Git replays only supports Mercurial branches" );
		}
	}

	@Override
	public void replay( AbstractCommit commit ) throws UnableToReplayException {
		doReplay( new ReplayImpl( commit ));
	}
	
	public class ReplayImpl extends Replay {

		public ReplayImpl( AbstractCommit commit ) {
			super( commit );
		}
		
		public boolean replay() {
			List<ChangeSetElement> cs = commit.getChangeSet().asList();
			
			boolean success = true;
			
			for( ChangeSetElement cse : cs ) {
				
				File targetfile = new File( branch.getPath(), cse.getFile().toString() );
				File sourcefile = new File( commit.getBranch().getPath(), cse.getFile().toString() );

				/* Filter out directories */
				if( sourcefile.isDirectory() ) {
					continue;
				}
				
				switch( cse.getStatus() ) {
				case CREATED:
					logger.debug( "Create element" );
					try {
						targetfile.getParentFile().mkdirs();
						targetfile.createNewFile();
						Mercurial.add( targetfile, branch.getPath() );
					} catch (IOException e) {
						logger.warning( "Could not create file: " + e.getMessage() );
						/* Continue anyway */
					} catch (MercurialException e) {
						logger.error( "Could not add " + targetfile + " to git" );
						success = false;
						continue;
					}
					
				case CHANGED:
					logger.debug( "Change element" );
					InputStream in = null;
					OutputStream out = null;
					try {
						in = new FileInputStream( sourcefile );
						out = new FileOutputStream( targetfile );
						
					    byte[] buf = new byte[1024];
					    int len;
					    while ((len = in.read(buf)) > 0) {
					        out.write(buf, 0, len);
					    }
						
					} catch (FileNotFoundException e) {
						success = false;
						logger.error( "Could not write to file(" + sourcefile + "): " + e );
					} catch (IOException e) {
						success = false;
						logger.error( "Could not write to file(" + sourcefile + "): " + e );
					} finally {
						try {
							in.close();
							out.close();
						} catch (Exception e) {
							logger.warning( "Could not close files: " + e.getMessage() );
						}
						
					}
					
					break;
					
				case DELETED:
					logger.debug( "Delete element" );
					try {
						Mercurial.remove( targetfile, branch.getPath() );
					} catch (MercurialException e) {
						logger.warning( e.getMessage() );
						success = false;
					}
					break;
					
				case RENAMED:
					logger.debug( "Rename element" );
					File oldfile = new File( branch.getPath(), cse.getRenameFromFile().toString() );
					
					/* Write before rename */
					write( sourcefile, oldfile );
					
					/* Make sure the target directory exists */
					if( !targetfile.getParentFile().exists() ) {
						logger.debug( "The directory " + targetfile.getParentFile() + " does not exist. Creating it." );
						targetfile.getParentFile().mkdirs();
					}
					
					try {
						Mercurial.move( oldfile, targetfile, branch.getPath() );
					} catch (MercurialException e) {
						logger.warning( e.getMessage() );
						success = false;
					}
					break;
				}
			}
			
			return success;
		}
		
		private boolean write( File source, File target ) {
			InputStream in = null;
			OutputStream out = null;
			boolean success = true;
			
			try {
				in = new FileInputStream( source );
				out = new FileOutputStream( target );
				
			    byte[] buf = new byte[1024];
			    int len;
			    while ((len = in.read(buf)) > 0) {
			        out.write(buf, 0, len);
			    }
				
			} catch (FileNotFoundException e) {
				success = false;
				logger.error( "Could not write to file(" + source + "): " + e );
			} catch (IOException e) {
				success = false;
				logger.error( "Could not write to file(" + source + "): " + e );
			} finally {
				try {
					in.close();
					out.close();
				} catch (Exception e) {
					logger.warning( "Could not close files: " + e.getMessage() );
				}
				
			}
			
			return success;
		}
		
		public boolean cleanup( boolean status ) {
			if( status ) {
				try {
					Mercurial.createCommit( commit.getTitle(), branch.getPath() );
					return true;
				} catch (MercurialException e) {
					return false;
				}
			} else {
				return false;
			}
		}
		
	}

}
