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
		cycle( branch, replay, interval, true );
	}
	
	public static void cycle( AbstractBranch branch, AbstractReplay replay, Integer interval, boolean checkoutCommit ) throws UnableToCheckoutCommitException, UnableToReplayException, IOException, InterruptedException {
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		Date now = AVA.getInstance().getLastCommitDate();
		logger.debug( "LAST IS " + now );
		Date before = null;
		
		while( true ) {
			if( now != null ) {
				logger.info( "Getting commits after " + now );
			}
			
			/* Just to make sure, that now is really now, because we need now in getCommits, which can be lengthy */
			before = new Date();
			List<AbstractCommit> commits = branch.getCommits(false, now);
			for( int i = 0 ; i < commits.size() ; ++i ) {
				System.out.print( "\rCommit " + ( i + 1 ) + "/" + commits.size() + ": " + commits.get( i ).getKey() );

				/* Load the commit */
				commits.get( i ).load();
				
				branch.checkoutCommit( commits.get( i ) );
				replay.replay( commits.get( i ) );
			}
			
			/* Make now before */
			now = before;
			
			AVA.getInstance().setLastCommitDate( now );
			
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
	
	public static class Update implements Runnable {

		private List<AbstractCommit> commits;
		private AbstractBranch branch;
		private Date now;
		
		public Update( AbstractBranch branch, Date now ) {
			this.branch = branch;
			this.now = now;
		}
		
		public void run() {
			commits = branch.getCommits( false, now );
			/* Update 'em */
			for( AbstractCommit c : commits ) {
				c.load();
			}
		}
		
		public List<AbstractCommit> getCommits() {
			return commits;
		}
		
	}
}
