package net.praqma.vcs.test;

import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.vcs.AVA;
import net.praqma.vcs.model.extensions.PullListener;
import net.praqma.vcs.model.extensions.PullListenerImpl;

public class ExtentionTest {

	static Logger logger = Logger.getLogger();
	
	public static void main( String[] args ) {
		new AVA(null);
		AVA.getInstance().registerExtension( "Test", new PullListenerImpl() );
		
		logger.log( "hej", LogLevel.DEBUG );
		logger.log( "hej" );
		PullListener.runPreCheckoutListener();
	}
	
}
