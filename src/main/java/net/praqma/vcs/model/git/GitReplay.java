package net.praqma.vcs.model.git;

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
import net.praqma.vcs.model.clearcase.ClearcaseBranch;
import net.praqma.vcs.model.exceptions.UnableToReplayException;
import net.praqma.vcs.model.exceptions.UnsupportedBranchException;
import net.praqma.vcs.model.git.api.Git;
import net.praqma.vcs.model.git.exceptions.GitException;
import net.praqma.vcs.util.IO;

public class GitReplay extends AbstractReplay{

	public GitReplay( GitBranch branch ) {
		super( branch );
	}
	
	public GitReplay( AbstractBranch branch ) throws UnsupportedBranchException {
		super( branch );
		if( branch instanceof GitBranch ) {
		} else {
			throw new UnsupportedBranchException( "Git replays only supports Git branches" );
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
						Git.add( targetfile, branch.getPath() );
					} catch (IOException e) {
						logger.warning( "Could not create file: " + e.getMessage() );
						/* Continue anyway */
					} catch (GitException e) {
						logger.error( "Could not add " + targetfile + " to git" );
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
						Git.remove( targetfile, branch.getPath() );
					} catch (GitException e) {
						logger.warning( e.getMessage() );
						success = false;
					}
					break;
					
				case RENAMED:
					logger.debug( "Rename element" );
					File oldfile = new File( branch.getPath(), cse.getRenameFromFile().toString() );
					
					/* Write before rename */
					IO.write( sourcefile, oldfile );
					
					/* Make sure the target directory exists */
					if( !targetfile.getParentFile().exists() ) {
						logger.debug( "The directory " + targetfile.getParentFile() + " does not exist. Creating it." );
						targetfile.getParentFile().mkdirs();
					}
					
					try {
						Git.move( oldfile, targetfile, branch.getPath() );
					} catch (GitException e) {
						logger.warning( e.getMessage() );
						success = false;
					}
					
					cleanRename( oldfile );
					
					break;
				}
			}
			
			return success;
		}
				
		public boolean cleanup( boolean status ) {
			if( status ) {
				try {
					Git.createCommit( commit.getTitle(), commit.getAuthor(), commit.getAuthorDate(), branch.getPath() );
					return true;
				} catch (GitException e) {
					return false;
				}
			} else {
				return false;
			}
		}
		
	}

}
