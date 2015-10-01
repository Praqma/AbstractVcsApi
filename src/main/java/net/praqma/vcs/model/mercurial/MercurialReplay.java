package net.praqma.vcs.model.mercurial;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

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

public class MercurialReplay extends AbstractReplay {

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
		
        @Override
		public boolean setup() {
			branch.update();
			return true;
		}
        
		@Override
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
					logger.fine( "Create element" );
					try {
						targetfile.getParentFile().mkdirs();
						targetfile.createNewFile();
						Mercurial.add( targetfile, branch.getPath() );
					} catch (IOException e) {
						logger.warning( "Could not create file: " + e.getMessage() );
						/* Continue anyway? */
					} catch (MercurialException e) {
						logger.log(Level.WARNING, "Could not add " + targetfile + " to Mercurial", e);
						success = false;
						continue;
					}
					
				case CHANGED:
					logger.fine( "Change element" );
					IO.write( sourcefile, targetfile );
					break;
					
				case DELETED:
					logger.fine( "Delete element" );
					try {
						Mercurial.remove( targetfile, branch.getPath() );
					} catch (MercurialException e) {
						logger.warning( e.getMessage() );
						success = false;
					}
					break;
					
				case RENAMED:
					logger.fine( "Rename element : " + cse );
					File oldfile = new File( branch.getPath(), cse.getRenameFromFile().toString() );
					/* Write before rename */
					IO.write( sourcefile, oldfile );
					
					/* Make sure the target directory exists */
					if( !targetfile.getParentFile().exists() ) {
						logger.fine( "The directory " + targetfile.getParentFile() + " does not exist. Creating it." );
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
				
				try {
					logger.fine( "STATUS: " + Mercurial.status( branch.getPath() ) );
				} catch (MercurialException e) {
					logger.log(Level.SEVERE, "Unable to run hg -status", e);
				}
			}
			
			return success;
		}
        
		@Override
		public boolean commit() {
			try {
				Mercurial.createCommit( commit.getTitle(), commit.getAuthor(), commit.getAuthorDate(), branch.getPath() );
				logger.info( "New Mercurial commit created" );
				return true;
			} catch( MercurialException e ) {
				if( e.getType().equals( FailureType.NOTHING_CHANGED ) ) {
					logger.info( "No Mercurial commit created, nothing changed" );
				} else {
					logger.log(Level.SEVERE,  "No Mercurial commit created", e.getMessage());
				}
				return false;
			}
		}
		
	}

}
