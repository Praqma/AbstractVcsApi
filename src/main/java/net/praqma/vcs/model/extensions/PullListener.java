package net.praqma.vcs.model.extensions;

import java.util.List;

import net.praqma.vcs.Extension;
import net.praqma.vcs.OpenVCS;

public abstract class PullListener implements Extension {
	
	public abstract void onPrePull();
	
	public abstract void onPostPull();

	public static void runPrePullListener() {
		for (PullListener l : all()) {
			l.onPrePull();
		}
	}
	
	public static void runPostPullListener() {
		for (PullListener l : all()) {
			l.onPostPull();
		}
	}
	
	public static List<PullListener> all() {
		return OpenVCS.getInstance().getExtensions(PullListener.class);
	}
}
