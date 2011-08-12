package net.praqma.vcs.model.extensions;

import java.util.List;

import net.praqma.vcs.Extension;
import net.praqma.vcs.AVA;

public abstract class PullListener implements Extension {
	
	public abstract void onPrePull();
	
	public abstract void onPostPull();

	public static void runPreCheckoutListener() {
		for (PullListener l : all()) {
			l.onPrePull();
		}
	}
	
	public static void runPostCheckoutListener() {
		for (PullListener l : all()) {
			l.onPostPull();
		}
	}
	
	public static List<PullListener> all() {
		return AVA.getInstance().getExtensions(PullListener.class);
	}
}
