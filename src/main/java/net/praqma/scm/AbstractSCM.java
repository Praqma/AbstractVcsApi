package net.praqma.scm;

import java.io.File;
import java.util.List;

import net.praqma.exceptions.OperationNotImplementedException;
import net.praqma.exceptions.OperationNotSupportedException;

public abstract class AbstractSCM {

	protected File localRepositoryPath;
	protected AbstractBranch branch;
	protected Repository parent;
	
	public AbstractSCM( Repository parent, File localRepositoryPath, AbstractBranch branch ) {
		this.localRepositoryPath = localRepositoryPath;
		this.branch = branch;
		this.parent = parent;
		
		/* Create path */
		localRepositoryPath.mkdirs();
	}
	
	public void initialize() throws OperationNotSupportedException {
		throw new OperationNotSupportedException( "Cannot initialize this kind of repository" );
	}
	
	public void pull() throws OperationNotSupportedException {
		throw new OperationNotSupportedException( "Cannot pull from this kind of repository" );
	}
	
	public void pull( Repository parent ) throws OperationNotSupportedException {
		throw new OperationNotSupportedException( "Cannot pull from this kind of repository" );
	}
	
	protected void doPull( Pull pull ) {

		boolean status = pull.prePull();
		
		/* Only perform if pre step went good */
		if( status ) {
			status = pull.perform();
		}
		
		pull.postPull( status );
	}
	
	protected abstract class Pull{
		protected Repository parent;
		public Pull( Repository parent ) {
			this.parent = parent;
		}
		
		public boolean prePull() {
			System.out.println( "Abstract: pre pull" );
			return true;
		}
		
		public boolean perform() {
			System.out.println( "Abstract: perform pull" );
			return true;
		}
		
		public boolean postPull( boolean status ) {
			System.out.println( "Abstract: post pull " + status );
			return true;
		}
	}
	
	public void changeBranch( File localRepositoryPath, AbstractBranch branch ) throws OperationNotSupportedException {
		throw new OperationNotSupportedException( "Cannot change branch" );
	}
	
	
	public List<AbstractCommit> getCommits() throws OperationNotImplementedException {
		throw new OperationNotImplementedException( "getCommits" );
	}

}
