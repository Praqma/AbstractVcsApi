package net.praqma.vcs.model.extensions;

import java.util.List;

import net.praqma.vcs.AVA;
import net.praqma.vcs.Extension;
import net.praqma.vcs.model.AbstractCommit;

public abstract class ReplayListener implements Extension {
	
	public abstract void onPostReplay( AbstractCommit commit, boolean status );
	
	public static void runPostCommitLoadListener( AbstractCommit commit, boolean status ) {
		for (ReplayListener l : all()) {
			l.onPostReplay( commit, status );
		}
	}
	
	public static List<ReplayListener> all() {
		return AVA.getInstance().getExtensions(ReplayListener.class);
	}
}
