package net.praqma.scm.git;

import java.io.File;

import net.praqma.scm.AbstractBranch;

public class GitBranch extends AbstractBranch{

	public GitBranch(  File localRepositoryPath, String name ) {
		super( localRepositoryPath, name );
	}

}
