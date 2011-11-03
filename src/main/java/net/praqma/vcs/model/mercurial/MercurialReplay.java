package net.praqma.vcs.model.mercurial;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.AbstractReplay;
import net.praqma.vcs.model.ChangeSetElement;
import net.praqma.vcs.model.exceptions.UnableToReplayException;
import net.praqma.vcs.model.exceptions.UnsupportedBranchException;
import net.praqma.vcs.model.exceptions.VCSException.FailureType;
import net.praqma.vcs.model.mercurial.api.Mercurial;
import net.praqma.vcs.model.mercurial.exceptions.MercurialException;
import net.praqma.vcs.util.IO;

public class MercurialReplay extends AbstractReplay{

	public MercurialReplay( MercurialBranch branch ) {
		super( branch );
	}
	
	public MercurialReplay( AbstractBranch branch ) throws UnsupportedBranchException {
		super( branch );
		if( branch instanceof MercurialBranch ) {
		} else {
			throw new UnsupportedBranchException( "Mercurial replays only supports Mercurial branches" );
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
		
		public boolean setup() {
			branch.update();
			return true;
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
						/* Continue anyway? */
					} catch (MercurialException e) {
						logger.error( "Could not add " + targetfile + " to Mercurial: " + e.getMessage() );
						success = false;
						continue;
					}
					
				case CHANGED:
					logger.debug( "Change element" );
					IO.write( sourcefile, targetfile );
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
					logger.debug( "Rename element : " + cse );
					File oldfile = new File( branch.getPath(), cse.getRenameFromFile().toString() );
					/* Write before rename */
					IO.write( sourcefile, oldfile );
					
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
					
					//cleanRename( oldfile );
					
					break;
				}
				
				try {
					logger.debug( "STATUS: " + Mercurial.status( branch.getPath() ) );
				} catch (MercurialException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			return success;
		}
		

		
		public boolean cleanup( boolean status ) {
			if( status ) {
				try {
					Mercurial.createCommit( commit.getTitle(), commit.getAuthor(), commit.getAuthorDate(), branch.getPath() );
					logger.info( "New Mercurial commit created" );
					return true;
				} catch( MercurialException e ) {
					if( e.getType().equals( FailureType.NOTHING_CHANGED ) ) {
						logger.info( "No Mercurial commit created, nothing changed" );
						return true;
					} else {
						logger.error( "No Mercurial commit created, " + e.getMessage() );
						return false;
					}
				}
			} else {
				return false;
			}
		}
		
	}

}
