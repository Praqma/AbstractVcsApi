package net.praqma.vcs.model.extensions;

import java.util.List;

import net.praqma.vcs.AVA;
import net.praqma.vcs.Extension;
import net.praqma.vcs.model.AbstractCommit;

public abstract class CommitLoadListener implements Extension {
	
	public abstract void onPreLoad( AbstractCommit commit );
	
	public abstract void onPostLoad( AbstractCommit commit, boolean status );

	public static void runPreCommitLoadListener( AbstractCommit commit ) {
		for (CommitLoadListener l : all()) {
			l.onPreLoad( commit );
		}
	}
	
	public static void runPostCommitLoadListener( AbstractCommit commit, boolean status ) {
		for (CommitLoadListener l : all()) {
			l.onPostLoad( commit, status );
		}
	}
	
	public static List<CommitLoadListener> all() {
		return AVA.getInstance(null).getExtensions(CommitLoadListener.class);
	}
}
