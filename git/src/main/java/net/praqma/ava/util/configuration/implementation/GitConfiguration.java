package net.praqma.ava.util.configuration.implementation;

import java.io.File;

import net.praqma.ava.model.AbstractBranch;
import net.praqma.ava.model.AbstractReplay;
import net.praqma.ava.model.exceptions.ElementDoesNotExistException;
import net.praqma.ava.model.exceptions.ElementNotCreatedException;
import net.praqma.ava.model.exceptions.UnsupportedBranchException;
import net.praqma.ava.model.git.GitBranch;
import net.praqma.ava.model.git.GitReplay;
import net.praqma.ava.util.configuration.AbstractConfiguration;

public class GitConfiguration extends AbstractConfiguration {
	
	private static final long serialVersionUID = 4113405067586223732L;
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
