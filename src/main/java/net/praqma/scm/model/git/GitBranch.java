package net.praqma.scm.model.git;

import java.io.File;

import net.praqma.scm.model.AbstractBranch;

public class GitBranch extends AbstractBranch{

	public GitBranch(  File localRepositoryPath, String name ) {
		super( localRepositoryPath, name );
	}

}
