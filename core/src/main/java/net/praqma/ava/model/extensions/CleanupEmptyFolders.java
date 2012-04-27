package net.praqma.ava.model.extensions;

import net.praqma.util.debug.Logger;
import net.praqma.ava.model.AbstractCommit;
import net.praqma.ava.model.AbstractReplay;
import net.praqma.ava.util.IO;

public class CleanupEmptyFolders extends ReplayListener {

	private Logger logger = Logger.getLogger();
	
	@Override
	public void onPostReplay( AbstractReplay replay, AbstractCommit commit, boolean status ) {
		/* Only do this if the replay went well */
		if( status ) {
			IO.removeEmptyFolders( replay.getBranch().getPath() );
		}
	}

	@Override
	public void onCommitCreated( AbstractReplay replay, AbstractCommit commit ) {
	}

}
