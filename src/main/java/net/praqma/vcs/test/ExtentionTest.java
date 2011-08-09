package net.praqma.vcs.test;

import net.praqma.vcs.OpenVCS;
import net.praqma.vcs.model.extensions.PullListener;
import net.praqma.vcs.model.extensions.PullListenerImpl;

public class ExtentionTest {

	/**
	 * @param args
	 */
	public static void main( String[] args ) {
		new OpenVCS();
		OpenVCS.getInstance().registerExtension( "Test", new PullListenerImpl() );
		
		PullListener.runPrePullListener();
	}
	
}
