package net.praqma.vcs.model.clearcase;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.ucm.view.SnapshotView.COMP;
import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.exceptions.ElementAlreadyExistsException;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementException.FailureType;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.util.Utils;

/**
 * An implementation of {@link AbstractBranch} for Clearcase, where {@link Baseline}'s are used as commit separator.
 * @author wolfgang
 *
 */
public class ClearcaseBranch extends AbstractBranch{

	private File viewroot;
	private File developmentPath;
	private Baseline baseline;
	private String viewtag;
	private Stream parent;
	
	private Vob vob;
	private ClearcaseVCS ccVCS;
	
	private Stream devStream;
	private SnapshotView snapshot;
	
	private Component component;
	
	private static Logger logger = Logger.getLogger();
	
	public ClearcaseBranch( ClearcaseVCS ccVCS, Vob vob, Stream parent, Baseline baseline, File viewroot, String viewtag, String name ) throws ElementNotCreatedException {
		this.viewroot = viewroot;
		this.viewtag = viewtag;
		this.baseline = baseline;
		this.name = name;
		this.parent = parent;
		
		this.vob = vob;
		this.ccVCS = ccVCS;
		
		try {
			this.component = baseline.getComponent();
		} catch (UCMException e) {
			logger.error( "Could not create Clearcase branch: " + e.getMessage() );
			throw new ElementNotCreatedException( "Could not create Clearcase branch: " + e.getMessage(), FailureType.DEPENDENCY );
		}
		
		this.developmentPath = new File( viewroot, vob + "/" + this.component.getShortname() );
		
		File view = new File( viewroot, vob.toString() );
		this.localRepositoryPath = view;
	}

	/**
	 * Create and initializes a Clearcase branch(stream) and a corresponding view(not updated).
	 * @param vob {@link Vob}
	 * @param pvob {@link PVob}
	 * @param parent The parent {@link Stream} 
	 * @param baseline The {@link Baseline} the branch is initialized from
	 * @param viewroot The view root as a {@link File}
	 * @param viewtag The view tag of the {@link SnapshotView}
	 * @param name The name of the {@link Stream} given as a basename.
	 * @return
	 * @throws ElementNotCreatedException
	 * @throws ElementAlreadyExistsException 
	 */
	public static ClearcaseBranch create( ClearcaseVCS ccVCS, Vob vob, Stream parent, Baseline baseline, File viewroot, String viewtag, String name ) throws ElementNotCreatedException, ElementAlreadyExistsException {
		ClearcaseBranch branch = new ClearcaseBranch( ccVCS, vob, parent, baseline, viewroot, viewtag, name );
		branch.initialize();
		//branch.get();
		return branch;
	}
	
	
	
	@Override
	public void initialize() throws ElementNotCreatedException, ElementAlreadyExistsException {
		initialize(false);
	}
	
	public void initialize( boolean get ) throws ElementNotCreatedException, ElementAlreadyExistsException {
		logger.info( "Creating Clearcase branch/stream " + name );
		if( !doInitialize( new InitializeImpl( get ) ) ) {
			throw new ElementNotCreatedException( "Could not create Clearcase branch" );
		}
	}
	
	private class InitializeImpl extends Initialize {
		public InitializeImpl( boolean get ) {
			super( get );
		}

		public boolean initialize() {

			try {
				logger.info( "Creating development stream"  );
				devStream = Stream.create( parent, name + "@" + ccVCS.getPVob(), false, baseline );
			} catch (UCMException e) {
				if( get ) {
					try {
						devStream = UCMEntity.getStream( name, ccVCS.getPVob(), false );
						logger.info( "Stream already exists" );
					} catch (UCMException e1) {
						logger.error( "Could not find stream: " + e.getMessage() );
						return false;
					}
				} else {
					logger.error("Error while creating Development Stream: " + e.getMessage());
					return false;
				}
			}
			
			try {
				logger.info( "Creating development view" );
				viewroot.mkdirs();
				snapshot = SnapshotView.Create( devStream, viewroot, viewtag );
			} catch (UCMException e) {
				if( get ) {
					try {
						snapshot = UCMView.GetSnapshotView(viewroot);
						logger.info( "View already exists" );
					} catch (Exception e1) {
						logger.error( "Could not find view: " + e.getMessage() );
						/* try to generate new */
						try {
							snapshot = SnapshotView.Create( devStream, viewroot, viewtag + System.currentTimeMillis() );
						} catch (UCMException e2) {
							logger.error( "Could not generate new view: " + e2.getMessage() );
							return false;
						}
						
					}
				} else {
					logger.error("Error while creating Snapshot View: " + e.getMessage());
					return false;
				}
			}
			
			return true;
		}
	}
	
	public boolean exists() {
		boolean result = true;
		
		try {
			UCMEntity.getStream( name, ccVCS.getPVob(), false );
		} catch (UCMException e1) {
			logger.error( "Stream does not exist" );
		}


		if( !UCMView.ViewExists( viewtag )) {
			logger.debug( "View tag does not exist" );
			result = false;
		} else {
			try {
				UCMView.GetSnapshotView(viewroot);
			} catch (Exception e1) {
				logger.debug( "View root does not exist" );
				result = false;			
			}
		}
		
		return result;
	}
	
	public void get() throws ElementDoesNotExistException {
		try {
			get(false);
		} catch (ElementNotCreatedException e) {
			/* This should not happen */
			/* TODO Should we throw DoesNotExist? */
			logger.fatal( "This shouldn't be possible..." );
		}
	}
	
	public void get( boolean initialize ) throws ElementDoesNotExistException, ElementNotCreatedException {
		
		boolean exists = true;
		try{
			this.devStream = UCMEntity.getStream( name, ccVCS.getPVob(), false );
		} catch( UCMException e ) {
			logger.debug( "Stream did not exist" );
			exists = false;
		}
		
		if( !UCMView.ViewExists( viewtag ) ) {
			logger.debug( "View did not exist" );
			exists = false;
		/* The view exists, but is the path correct? */
		} else {
			if( !viewroot.exists() ) {
				exists = false;
			}
		}
		
		if( !exists && initialize ) {
			logger.debug( "Must initialize" );
			try {
				initialize(true);
			} catch (ElementAlreadyExistsException e) {
				/* This should not happen */
				/* TODO Should we throw ElementAlreadyExistsException? */
				logger.fatal( "This shouldn't be possible..." );
			}
		} else {
			logger.debug( "DONT initialize" );
			try {
				snapshot = UCMView.GetSnapshotView(viewroot);
			} catch (UCMException e) {
				logger.error( "Could not get view: " + e.getMessage() );
				throw new ElementDoesNotExistException( "Could not get clearcase view" );
			}
		}
	}
	
	
	public void update() {
		doUpdate( new UpdateImpl(null) );
	}
	
	@Override
	public void update( AbstractCommit commit ) {
		doUpdate( new UpdateImpl(commit) );
	}
	
	public class UpdateImpl extends Update {
		public UpdateImpl( AbstractCommit commit ) {
			super( commit );
		}

		public boolean setup() {
			if( snapshot == null || devStream == null ) {
				try {
					get();
				} catch (ElementDoesNotExistException e) {
		        	logger.error("Error while getting stream: " + e.getMessage());
		        	return false;
				}
			}
			
			return true;
		}
		
		public boolean update() {
			try {
				snapshot.Update( true, true, true, false, COMP.MODIFIABLE, null );
			} catch (UCMException e) {
	        	logger.error("Error while updating view: " + e.getMessage());
	        	return false;
			}
			
			return true;
		}
	}
	
	

	@Override
	public List<AbstractCommit> getCommits() {
		return getCommits( false, null );
	}
	
	@Override
	public List<AbstractCommit> getCommits( boolean load ) {
		return getCommits( load, null );
	}

	@Override
	public List<AbstractCommit> getCommits( boolean load, Date offset ) {
		
		List<AbstractCommit> commits = new ArrayList<AbstractCommit>();
		
		try {
			List<Baseline> baselines = this.devStream.getBaselines( getComponent(), null );
			
			/* TODO Clear out baselines before offset */
			if( offset != null ) {
				
			}
			
			for( int i = 0 ; i < baselines.size() ; i++ ) {
				System.out.print( "\r" + Utils.getProgress( baselines.size(), i ) );
				ClearcaseCommit commit = new ClearcaseCommit( baselines.get( i ).getFullyQualifiedName(), ClearcaseBranch.this, i );
				if( load ) {
					commit.load();
				}
				commits.add( commit );
			}
		} catch (UCMException e) {
			logger.error( "Could not list baselines: " + e.getMessage() );
		}
		
		System.out.println( " Done" );
		
		return commits;
	}
	
	
	public SnapshotView getSnapshotView() {
		return snapshot;
	}
	
	public PVob getPVob() {
		return ccVCS.getPVob();
	}
	
	public Vob getVob() {
		return vob;
	}
	
	public Baseline getBaseline() {
		return baseline;
	}
	
	public File getDevelopmentPath() {
		return this.developmentPath;
	}
	
	public Component getComponent() {
		return this.component;
	}

	@Override
	public boolean cleanup() {
		return true;
	}

}
