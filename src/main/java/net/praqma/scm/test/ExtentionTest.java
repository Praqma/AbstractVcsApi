package net.praqma.scm.test;

import net.praqma.scm.OpenSCM;
import net.praqma.scm.model.extensions.PullListener;
import net.praqma.scm.model.extensions.PullListenerImpl;

public class ExtentionTest {

	/**
	 * @param args
	 */
	public static void main( String[] args ) {
		new OpenSCM();
		OpenSCM.getInstance().registerExtension( "Test", PullListenerImpl.class );
		
		PullListener.pullListener();
	}
	
}
