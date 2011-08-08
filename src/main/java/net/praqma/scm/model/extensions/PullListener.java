package net.praqma.scm.model.extensions;

import java.util.List;

import net.praqma.scm.Extension;
import net.praqma.scm.OpenSCM;

public abstract class PullListener extends Extension {
	
	public abstract void doPullThingy();

	public static void pullListener() {
		System.out.println("BUYAR");
		for (PullListener l : all()) {
			System.out.println("1");
			l.doPullThingy();
		}
	}
	
	public static List<PullListener> all() {
		return OpenSCM.getInstance().getExtensions(PullListener.class);
	}
}
