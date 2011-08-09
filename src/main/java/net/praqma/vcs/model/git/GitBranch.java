package net.praqma.vcs.model.git;

import java.io.File;

import net.praqma.vcs.model.AbstractBranch;

public class GitBranch extends AbstractBranch{

	public GitBranch(  File localRepositoryPath, String name ) {
		super( localRepositoryPath, name );
	}

}
