package net.praqma.vcs.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.AVA;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.AbstractReplay;
import net.praqma.vcs.model.exceptions.UnableToCheckoutCommitException;
import net.praqma.vcs.model.exceptions.UnableToReplayException;

public class Cycle {
	
	private static Logger logger = Logger.getLogger();
	
	public static void cycle( AbstractBranch branch, AbstractReplay replay, Integer interval ) throws UnableToCheckoutCommitException, UnableToReplayException, IOException, InterruptedException {
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		Date now = AVA.getInstance().getLastCommitDate();
		Date before = null;
		
		while( true ) {
			if( now != null ) {
				logger.info( "Getting commits after " + now );
			}
			
			/* Just to make sure, that now is really now, because we need now in getCommits, which can be lengthy */
			before = new Date();
			
			logger.info( "Getting ClearCase commits" );
			List<AbstractCommit> cccommits = branch.getCommits(true, now);
			for( AbstractCommit commit : cccommits ) {
				branch.checkoutCommit( commit );
				replay.replay( commit );
			}
			
			/* Make now before */
			now = before;
			
			if( interval != null ) {
				/* Interactive mode */
				if( interval <= 0 ) {
					logger.info( "Press any key to continue" );
					stdin.readLine();
				} else {
					Thread.sleep( interval * 1000 );
				}
			} else {
				/* Only one pass is needed */
				return;
			}
		}
	}
}
