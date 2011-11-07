package net.praqma.vcs.model.clearcase.listeners;

import java.util.List;

import net.praqma.vcs.Extension;
import net.praqma.vcs.AVA;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.AbstractReplay;
import net.praqma.vcs.model.clearcase.ClearCaseReplay;

public abstract class ClearcaseReplayListener implements Extension {

	/**
	 * Before initializing {@link AbstractReplay} object
	 * @param replay
	 */
	public abstract void onReplay( ClearCaseReplay replay, AbstractCommit commit );
	
	/**
	 * When baselining a commit
	 * @param commit {@link AbstractCommit}
	 * @return
	 */
	public abstract String onSelectBaselineName( AbstractCommit commit );

	
	public static void runReplay( ClearCaseReplay replay, AbstractCommit commit ) {
		for (ClearcaseReplayListener l : all()) {
			l.onReplay( replay, commit );
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
