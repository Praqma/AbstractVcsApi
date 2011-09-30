package net.praqma.vcs.model.extensions;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.AbstractReplay;
import net.praqma.vcs.util.IO;

public class CleanupEmptyFolders extends ReplayListener {

	private Logger logger = Logger.getLogger();
	
	@Override
	public void onPostReplay( AbstractReplay replay, AbstractCommit commit, boolean status ) {
		/* Only do this if the replay went well */
		if( status ) {
			IO.removeEmptyFolders( replay.getBranch().getPath() );
		}
	}

}
