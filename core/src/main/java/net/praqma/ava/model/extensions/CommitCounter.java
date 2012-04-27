package net.praqma.vcs.model.extensions;

import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.AbstractReplay;

public class CommitCounter extends ReplayListener {

	private int commitCount = 0;
	
	@Override
	public void onPostReplay( AbstractReplay replay, AbstractCommit commit, boolean status ) {
	}

	@Override
	public void onCommitCreated( AbstractReplay replay, AbstractCommit commit ) {
		commitCount++;
	}

	public int getCommitCount() {
		return commitCount;
	}
	
	public void reset() {
		commitCount = 0;
	}
}
