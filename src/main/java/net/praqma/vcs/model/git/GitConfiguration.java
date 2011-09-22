package net.praqma.vcs.model.git;

import java.io.File;

import net.praqma.vcs.configuration.AbstractConfiguration;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractReplay;
import net.praqma.vcs.model.clearcase.ClearcaseReplay;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.UnsupportedBranchException;

public class GitConfiguration extends AbstractConfiguration {
	
	private String branchName;
	
	public GitConfiguration( File path, String branchName ) {
		super( path );
		
		this.branchName = branchName;
	}
	
	public GitConfiguration( File path, String branchName, String parentLocation, String parentName ) {
		super( path, parentLocation, parentName );
		
		this.branchName = branchName;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranch( String branch ) {
		this.branchName = branch;
	}

	@Override
	public AbstractBranch getBranch() throws ElementNotCreatedException, ElementDoesNotExistException {
		if( branch == null ) {
			branch = new GitBranch( path, branchName, parent );
			branch.get( true );
		}
		return branch;
	}

	@Override
	public AbstractReplay getReplay() throws UnsupportedBranchException, ElementNotCreatedException, ElementDoesNotExistException {
		return new GitReplay( getBranch() );
	}

}
