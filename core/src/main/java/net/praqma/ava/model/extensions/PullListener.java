package net.praqma.ava.model.extensions;

import java.util.List;

import net.praqma.ava.Extension;
import net.praqma.ava.AVA;

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
