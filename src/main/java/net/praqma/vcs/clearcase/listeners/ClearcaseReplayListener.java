package net.praqma.vcs.clearcase.listeners;

import java.util.List;

import net.praqma.vcs.Extension;
import net.praqma.vcs.OpenVCS;
import net.praqma.vcs.model.AbstractCommit;

public abstract class ClearcaseReplayListener implements Extension {

	public abstract String onSelectBaselineName( AbstractCommit commit );

	public static String runSelectBaselineName( AbstractCommit commit ) {
		for (ClearcaseReplayListener l : all()) {
			l.onSelectBaselineName( commit );
		}
		
		return "AVA_" + commit.getKey();
	}
	
	public static List<ClearcaseReplayListener> all() {
		return OpenVCS.getInstance().getExtensions(ClearcaseReplayListener.class);
	}
	
}
