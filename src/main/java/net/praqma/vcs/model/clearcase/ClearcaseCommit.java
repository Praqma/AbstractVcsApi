package net.praqma.vcs.model.clearcase;

import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;

public class ClearcaseCommit extends AbstractCommit {

	public ClearcaseCommit( String key, AbstractBranch branch ) {
		super( key, branch );
	}
	
	public ClearcaseCommit( String key, AbstractBranch branch, int number ) {
		super( key, branch, number );
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub
		
	}

}
