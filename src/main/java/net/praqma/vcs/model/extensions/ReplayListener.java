package net.praqma.vcs.model.extensions;

import java.util.List;

import net.praqma.vcs.AVA;
import net.praqma.vcs.Extension;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.AbstractReplay;

public abstract class ReplayListener implements Extension {
	
	public abstract void onPostReplay( AbstractReplay replay, AbstractCommit commit, boolean status );
	
	public abstract void onCommitCreated( AbstractReplay replay, AbstractCommit commit );
	
	public static void runPostReplayListener( AbstractReplay replay, AbstractCommit commit, boolean status ) {
		for (ReplayListener l : all()) {
			l.onPostReplay( replay, commit, status );
		}
	}
	
	public static void runCommitCreatedListener( AbstractReplay replay, AbstractCommit commit ) {
		for (ReplayListener l : all()) {
			l.onCommitCreated( replay, commit );
		}
	}
	
	public static List<ReplayListener> all() {
		return AVA.getInstance(null).getExtensions(ReplayListener.class);
	}
}
