package net.praqma.ava.util.configuration.implementation;

import java.io.File;

import net.praqma.ava.model.mercurial.MercurialBranch;
import net.praqma.ava.model.mercurial.MercurialReplay;
import net.praqma.ava.model.mercurial.api.Mercurial;
import net.praqma.ava.model.mercurial.exceptions.MercurialException;
import net.praqma.ava.model.AbstractBranch;
import net.praqma.ava.model.AbstractReplay;
import net.praqma.ava.model.exceptions.ElementDoesNotExistException;
import net.praqma.ava.model.exceptions.ElementNotCreatedException;
import net.praqma.ava.model.exceptions.UnsupportedBranchException;
import net.praqma.ava.util.configuration.AbstractConfiguration;
import net.praqma.ava.util.configuration.exception.ConfigurationException;

public class MercurialConfiguration extends AbstractConfiguration {
	
	private static final long serialVersionUID = 3461227245140962704L;
	private String branchName;
	
	public MercurialConfiguration( File path, String branchName ) {
		super( path );
		
		this.branchName = branchName;
	}
	
	public MercurialConfiguration( String pathName, String branchName ) {
		super( pathName );
		
		this.branchName = branchName;
	}
	
	public MercurialConfiguration( File path, String branchName, String parentLocation, String parentName ) {
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
			branch = new MercurialBranch( path, branchName, parent );
			branch.get( true );
		}
		return branch;
	}
	
	@Override
	public void generate() throws ConfigurationException {
		super.generate();
		
		if( branchName == null || branchName.length() == 0 ) {
			try {
				branchName = Mercurial.getCurrentBranch( getPath() );
			} catch( MercurialException e ) {
				branchName = "default";
			}
		}
	}

	@Override
	public AbstractReplay getReplay() throws UnsupportedBranchException, ElementNotCreatedException, ElementDoesNotExistException {
		return new MercurialReplay( getBranch() );
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append( "Mercurial configuration:\n-------------------\n" );
		sb.append( "Path       : " + path + "\n" );
		sb.append( "Branch name: " + branchName + "\n" );

		return sb.toString();
	}

}
