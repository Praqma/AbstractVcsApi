package net.praqma.vcs.model.clearcase;

import java.io.File;

import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.vcs.model.AbstractBranch;

public class ClearcaseBranch extends AbstractBranch{

	private File viewroot;
	private Baseline baseline;
	private String viewtag;
	private Stream parent;
	
	private SnapshotView snapshot;
	
	public ClearcaseBranch( Vob vob, Stream parent, Baseline baseline, File viewroot, String viewtag, String name ) {
		this.viewroot = viewroot;
		this.viewtag = viewtag;
		this.baseline = baseline;
		this.name = name;
		this.parent = parent;
		
		File view = new File( viewroot, vob.toString() );
		this.localRepositoryPath = view;
	}
	
	public static ClearcaseBranch create( Vob vob, Stream parent, Baseline baseline, File viewroot, String viewtag, String name ) {
		ClearcaseBranch branch = new ClearcaseBranch( vob, parent, baseline, viewroot, viewtag, name );
		branch.initialize();
		return branch;
	}
	
	@Override
	public boolean initialize() {
		logger.info( "Creating Clearcase branch/stream" );
		return doInitialize( new InitializeImpl() );
	}
	
	private class InitializeImpl extends Initialize {
		public boolean initialize() {
			Stream devStream = null;
			try {
				logger.info( "Creating development stream" );
				devStream = Stream.create( parent, name, false, baseline );
			} catch (UCMException e) {
				logger.error("Error while creating Development Stream: " + e.getMessage());
				return false;
			}
			
			try {
				logger.info( "Creating development view" );
				snapshot = SnapshotView.Create( devStream, viewroot, viewtag );
			} catch (UCMException e) {
				logger.error("Error while creating Snapshot View: " + e.getMessage());
				return false;
			}
			
			return true;
		}
		
		public boolean cleanup( boolean status ) {
			return true;
		}
		
	}

}
