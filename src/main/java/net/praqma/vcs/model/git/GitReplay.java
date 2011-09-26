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
					try {
						Git.remove( targetfile, branch.getPath() );
					} catch (GitException e) {
						logger.warning( e.getMessage() );
						success = false;
					}
					break;
					
				case RENAMED:
					try {
						Git.move( sourcefile, targetfile, branch.getPath() );
					} catch (GitException e) {
						logger.warning( e.getMessage() );
						success = false;
					}
					break;
				}
			}
			
			return success;
		}
		
		public boolean cleanup( boolean status ) {
			if( status ) {
				try {
					Git.createCommit( commit.getTitle(), branch.getPath() );
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
