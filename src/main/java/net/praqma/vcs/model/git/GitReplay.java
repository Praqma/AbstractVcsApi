package net.praqma.vcs.model.git;

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
import net.praqma.vcs.model.git.api.Git;
import net.praqma.vcs.model.git.exceptions.GitException;
import net.praqma.vcs.util.IO;

public class GitReplay extends AbstractReplay {

    
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
						Git.add( targetfile, branch.getPath() );
					} catch (IOException e) {
						logger.warning( "Could not create file: " + e.getMessage() );
						/* Continue anyway */
					} catch (GitException e) {
						logger.log(Level.SEVERE, "Could not add " + targetfile + " to git", e);
						success = false;
						continue;
					}
					
				case CHANGED:
					logger.fine( "Change element" );
                    if(!targetfile.exists()) {
                        logger.warning("Changed file...the file is not there??");
                        try {
                            targetfile.getParentFile().mkdirs();
                            targetfile.createNewFile();                            
                            IO.write( sourcefile, targetfile );
                            Git.add( targetfile, branch.getPath() );
                        } catch (IOException | GitException e) {
                           logger.log(Level.SEVERE, "The file "+ targetfile + "is not present, even though it has changes. And it cannot be creted", e);
                        }
                    } else {
                        IO.write( sourcefile, targetfile );
                    }
					
					break;
					
				case DELETED:
					logger.fine( "Delete element" );
					try {
						Git.remove( targetfile, branch.getPath() );
					} catch (GitException e) {
						logger.warning( e.getMessage() );
						success = false;
					}
					break;
					
				case RENAMED:
					logger.fine( "Rename element" );
					File oldfile = new File( branch.getPath(), cse.getRenameFromFile().toString() );
					
					/* Write before rename */
					IO.write( sourcefile, oldfile );
					
					/* Make sure the target directory exists */
					if( !targetfile.getParentFile().exists() ) {
						logger.fine( "The directory " + targetfile.getParentFile() + " does not exist. Creating it." );
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

        @Override
		public boolean commit() {
			try {
                logger.fine("Creating commit: "+commit.getTitle());
				Git.createCommit( commit.getTitle(), commit.getAuthor(), commit.getAuthorDate(), branch.getPath() );
				return true;
			} catch (GitException e) {
				return false;
			}
		}
		
	}

}
