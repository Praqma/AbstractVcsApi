package net.praqma.vcs.clearcase;

import java.io.File;

import net.praqma.vcs.model.AbstractBranch;

public class ClearcaseBranch extends AbstractBranch{

	public ClearcaseBranch(  File localRepositoryPath, String name ) {
		super( localRepositoryPath, name );
	}

}
