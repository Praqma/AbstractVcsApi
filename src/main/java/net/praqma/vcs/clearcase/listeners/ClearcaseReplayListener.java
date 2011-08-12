package net.praqma.vcs.clearcase.listeners;

import java.util.List;

import net.praqma.vcs.Extension;
import net.praqma.vcs.AVA;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.AbstractReplay;

public abstract class ClearcaseReplayListener implements Extension {

	/**
	 * Before initializing {@link ClearcaseReplay} object
	 * @param replay
	 */
	public abstract void onReplay( AbstractReplay replay );
	
	/**
	 * When baselining a commit
	 * @param commit {@link AbstractCommit}
	 * @return
	 */
	public abstract String onSelectBaselineName( AbstractCommit commit );

	
	public static void runReplay( AbstractReplay replay ) {
		for (ClearcaseReplayListener l : all()) {
			l.onReplay( replay );
		}
	}
	
	/**
	 * Returns the {@link Baseline} basename from the first none-null extension implementation.
	 * @param commit
	 * @return
	 */
	public static String runSelectBaselineName( AbstractCommit commit ) {
		for (ClearcaseReplayListener l : all()) {
			String name = l.onSelectBaselineName( commit );
			if( name != null ) {
				return name;
			}
		}
		
		return "AVA_" + commit.getKey();
	}
	
	public static List<ClearcaseReplayListener> all() {
		return AVA.getInstance().getExtensions(ClearcaseReplayListener.class);
	}
	
}
