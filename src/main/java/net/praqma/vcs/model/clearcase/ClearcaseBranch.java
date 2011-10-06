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
import net.praqma.vcs.model.exceptions.UnableToCheckoutCommitException;
import net.praqma.vcs.util.Utils;

/**
 * An implementation of {@link AbstractBranch} for Clearcase, where {@link Baseline}'s are used as commit separator.
 * @author wolfgang
 *
 */
public class ClearcaseBranch extends AbstractBranch{

	/**
	 * The out most view root, ideally containing the
	 * folders in and/ out/
	 */
	private File viewroot;
	
	/**
	 * The name of the input stream
	 */
	private String name_in;
	
	/**
	 * The name of the output stream
	 */
	private String name_out;
	
	/**
	 * The view root of the input view
	 */
	private File viewroot_in;
	
	/**
	 * The view root of the output view
	 */
	private File viewroot_out;
	
	/**
	 * The root of the development view,
	 * basically the folder of the component
	 * @deprecated
	 */
	private File developmentPath_in;
	
	/**
	 * @deprecated
	 */
	private File developmentPath_out;
	
	/**
	 * The view tag of the input view
	 */
	private String viewtag_in;
	
	/**
	 * The view tag of the output view
	 */
	private String viewtag_out;
	
	/**
	 * The {@link Stream} of the input
	 */
	private Stream devStream_in;
	
	/**
	 * The {@link Stream} of the output
	 */
	private Stream devStream_out;
	
	/**
	 * The {@link SnapshotView} of the input
	 */
	private SnapshotView snapshot_in;
	
	/**
	 * The {@link SnapshotView} of the output
	 */
	private SnapshotView snapshot_out;
	
	/**
	 * The foundation {@link Baseline} of the branch
	 */
	private Baseline baseline;
	
	/**
	 * The parent {@link Stream} of the branch
	 */
	private Stream parent;
	
	/**
	 * The {@link Project}s {@link Vob}
	 */
	private Vob vob;
	
	/**
	 * The {@link Project}s {@link PVob}
	 */
	private PVob pvob;
	
	
	private Component component;
	
	private static Logger logger = Logger.getLogger();
	
	/**
	 * If the Stream does not exist, it will be created as a child of the Stream parent.
	 * @param pvob
	 * @param vob
	 * @param parent
	 * @param baseline
	 * @param viewroot
	 * @param viewtag
	 * @param name
	 * @throws ElementNotCreatedException
	 */
	public ClearcaseBranch( PVob pvob, Vob vob, Stream parent, Baseline baseline, File viewroot, String viewtag, String name ) throws ElementNotCreatedException {
		super(viewroot, name);
		this.name_in = name;
		this.name_out = name + "_out";
		this.viewroot = viewroot;
		this.viewroot_in = new File( viewroot, "in" );
		this.viewroot_out = new File( viewroot, "out" );
		this.viewtag_in = viewtag;
		this.viewtag_out = viewtag + "_out";
		this.baseline = baseline;
		
		logger.debug( "VIEWTAG=" + this.viewtag_in );
		logger.debug( "VIEWTAG=" + this.viewtag_out );
		
		this.parent = parent;
		
		this.vob = vob;
		this.pvob = pvob;
		//this.ccVCS = ccVCS;
		
		
		try {
			this.component = baseline.getComponent();
		} catch (UCMException e) {
			logger.error( "Could not create Clearcase branch: " + e.getMessage() );
			throw new ElementNotCreatedException( "Could not create Clearcase branch: " + e.getMessage(), FailureType.DEPENDENCY );
		}
		
		this.developmentPath_in = new File( viewroot_in, vob + "/" + this.component.getShortname() );
		this.developmentPath_out = new File( viewroot_out, vob + "/" + this.component.getShortname() );
		
		/* Set this to the view root of the out view */
		this.localRepositoryPath = viewroot_out;
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
	public static ClearcaseBranch create( PVob pvob, Vob vob, Stream parent, Baseline baseline, File viewroot, String viewtag, String name ) throws ElementNotCreatedException, ElementAlreadyExistsException {
		ClearcaseBranch branch = new ClearcaseBranch( pvob, vob, parent, baseline, viewroot, viewtag, name );
		branch.initialize();
		//branch.get();
		return branch;
	}
	
	
	
	@Override
	public void initialize() throws ElementNotCreatedException, ElementAlreadyExistsException {
		try {
			initialize(false);
		} catch (ElementDoesNotExistException e) {
			/* This shouldn't be possible */
			logger.fatal( "FAIL!! Shouldn't throw exist exceptions!!!" );
		}
	}
	
	public void initialize( boolean get ) throws ElementNotCreatedException, ElementAlreadyExistsException, ElementDoesNotExistException {
		logger.info( "Creating Clearcase branch/stream " + name );
		if( !doInitialize( new InitializeImpl( get ) ) ) {
			throw new ElementNotCreatedException( "Could not create Clearcase branch" );
		}
	}
	
	private class InitializeImpl extends Initialize {
		public InitializeImpl( boolean get ) {
			super( get );
		}

		public boolean initialize() throws ElementDoesNotExistException, ElementNotCreatedException, ElementAlreadyExistsException {

			/* Create input stream */
			if( get ) {
				try {
					devStream_in = UCMEntity.getStream( name_in, pvob, false );
				} catch ( UCMException e ) {
					logger.debug( name_in + " was not found."  );
					if( parent != null ) {
						logger.debug( "Trying to create the stream " + name_in );
						try {
							devStream_in = Stream.create( parent, name_in + "@" + pvob, false, baseline );
						} catch (UCMException e1) {
							logger.error( "Error while creating input stream: " + e.getMessage() );
						}
					} else {
						logger.debug( "Unable to create the stream " + name_in + ". Parent is null" );
					}
				} finally {
					if( devStream_in == null ) {
						throw new ElementDoesNotExistException( "Could not find input stream" );
					}
				}
			} else {
				try {
					logger.info( "Creating development input stream"  );
					devStream_in = Stream.create( parent, name_in + "@" + pvob, false, baseline );
				} catch (UCMException e) {
					if( get ) {
						try {
							devStream_in = UCMEntity.getStream( name_in, pvob, false );
							logger.info( "Input stream already exists" );
						} catch (UCMException e1) {
							logger.error( "Error while initializing input stream: " + e.getMessage() );
							throw new ElementDoesNotExistException( "Could not find input stream: " + e.getMessage() );
						}
					} else {
						logger.error( "Error while creating Development input Stream: " + e.getMessage() );
						throw new ElementNotCreatedException( "Error while creating Development input Stream: " + e.getMessage() );
					}
				}
			}
			
			/* Create output stream
			 * This is a read only stream, allowing rebasing. */
			try {
				logger.info( "Creating development output stream"  );
				devStream_out = Stream.create( devStream_in, name_out + "@" + pvob, true, baseline );
			} catch (UCMException e) {
				if( get ) {
					try {
						devStream_out = UCMEntity.getStream( name_out, pvob, false );
						logger.info( "Output stream already exists" );
					} catch (UCMException e1) {
						logger.error( "Could not find output stream: " + e.getMessage() );
						throw new ElementDoesNotExistException( "Could not find input stream: " + e.getMessage() );
					}
				} else {
					logger.error("Error while creating Development output Stream: " + e.getMessage());
					throw new ElementNotCreatedException( "Error while creating Development input Stream: " + e.getMessage() );
				}
			}
			
			/* Creating input view */
			if( get ) {
				try {
					snapshot_in = UCMView.getSnapshotView(viewroot_in);
					logger.info( "Using existing input view" );
				} catch (Exception e1) {
					try {
						logger.info( "Creating development input view, " + viewtag_in );
						if( !viewroot_in.exists() ) {
							viewroot_in.mkdirs();
						}
						snapshot_in = SnapshotView.create( devStream_in, viewroot_in, viewtag_in );
					} catch (UCMException e) {
						logger.error( "Error while initializing input view: " + e.getMessage() );
					}
				}
			} else {
				try {
					logger.info( "Creating development input view, " + viewtag_in );
					if( !viewroot_in.exists() ) {
						viewroot_in.mkdirs();
					}
					snapshot_in = SnapshotView.create( devStream_in, viewroot_in, viewtag_in );
				} catch (UCMException e) {
					throw new ElementAlreadyExistsException( "The input view " + viewtag_in + " already exists" );
				}
			}
			
			/* Creating output view */
			if( get ) {
				try {
					snapshot_out = UCMView.getSnapshotView(viewroot_out);
					logger.info( "Using existing output view" );
				} catch (Exception e1) {
					try {
						logger.info( "Creating development output view, " + viewtag_out );
						if( !viewroot_out.exists() ) {
							viewroot_out.mkdirs();
						}
						snapshot_out = SnapshotView.create( devStream_out, viewroot_out, viewtag_out );
					} catch (UCMException e) {
						logger.error( "Error while initializing output view: " + e.getMessage() );
					}
				}
			} else {
				try {
					logger.info( "Creating development output view, " + viewtag_out );
					if( !viewroot_out.exists() ) {
						viewroot_out.mkdirs();
					}
					snapshot_out = SnapshotView.create( devStream_out, viewroot_out, viewtag_out );
				} catch (UCMException e) {
					throw new ElementAlreadyExistsException( "The output view " + viewtag_out + " already exists" );
				}
			}
			
			return true;
		}
	}
	
	
	
	public boolean exists() {
		boolean result = true;
		
		/* Test input stream */
		try {
			UCMEntity.getStream( name_in, pvob, false );
		} catch (UCMException e1) {
			logger.error( "Input stream does not exist" );
		}
		
		/* Test output stream */
		try {
			UCMEntity.getStream( name_out, pvob, false );
		} catch (UCMException e1) {
			logger.error( "Output stream does not exist" );
		}

		/* Test input view */
		if( !UCMView.viewExists( viewtag_in )) {
			logger.debug( "Input view tag, " + viewtag_in + ", does not exist" );
			result = false;
		} else {
			try {
				UCMView.getSnapshotView(viewroot_in);
			} catch (Exception e1) {
				logger.debug( "Input view root does not exist" );
				result = false;
			}
		}
		
		/* Test output view */
		if( !UCMView.viewExists( viewtag_out )) {
			logger.debug( "Output view tag, " + viewtag_out + ", does not exist" );
			result = false;
		} else {
			try {
				UCMView.getSnapshotView(viewroot_out);
			} catch (Exception e1) {
				logger.debug( "Output view root does not exist" );
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
		boolean outputExists = false;
		try{
			this.devStream_in = UCMEntity.getStream( name_in, pvob, false );
		} catch( UCMException e ) {
			logger.debug( "Input stream did not exist" );
			exists = false;
		}
		
		try{
			this.devStream_out = UCMEntity.getStream( name_out, pvob, false );
			outputExists = true;
		} catch( UCMException e ) {
			logger.debug( "Output stream did not exist" );
			exists = false;
		}
		
		if( !UCMView.viewExists( viewtag_in ) ) {
			logger.debug( "Input View did not exist" );
			exists = false;
		/* The view exists, but is the path correct? */
		} else {
			if( !viewroot_in.exists() ) {
				exists = false;
			}
		}
		
		if( !UCMView.viewExists( viewtag_out ) ) {
			logger.debug( "Output view did not exist" );
			exists = false;
		/* The view exists, but is the path correct? */
		} else {
			if( !viewroot_out.exists() ) {
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
				snapshot_in = UCMView.getSnapshotView(viewroot_in);
			} catch (UCMException e) {
				logger.error( "Could not get input view: " + e.getMessage() );
				throw new ElementDoesNotExistException( "Could not get input clearcase view" );
			}
			
			try {
				snapshot_out = UCMView.getSnapshotView(viewroot_out);
			} catch (UCMException e) {
				logger.error( "Could not get output view: " + e.getMessage() );
				throw new ElementDoesNotExistException( "Could not get output clearcase view" );
			}
		}
	}
	
	
	public void update() {
		doUpdate( new UpdateImpl() );
	}

	/**
	 * This Update implementation only updates the input view, because the output view 
	 * does not need to be updated, only when demanded.
	 * @author wolfgang
	 *
	 */
	public class UpdateImpl extends Update {

		public boolean setup() {
			if( snapshot_in == null || devStream_in == null ) {
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
				snapshot_in.Update( true, true, true, false, COMP.MODIFIABLE, null );
			} catch (UCMException e) {
	        	logger.error("Error while updating view: " + e.getMessage());
	        	return false;
			}
			
			return true;
		}
	}
	
	@Override
	public void checkoutCommit( AbstractCommit commit ) throws UnableToCheckoutCommitException {
		this.currentCommit = commit;
		/* TODO how to checkout a commit i CC? */
		if( commit instanceof ClearcaseCommit ) {
			ClearcaseCommit cccommit = (ClearcaseCommit)commit;
			this.devStream_out.rebase( this.snapshot_out, cccommit.getBaseline(), true );
			try {
				this.snapshot_out.Update(true, true, true, false, COMP.ALL, null);
			} catch (UCMException e) {
				throw new UnableToCheckoutCommitException( "Could not checkout " + cccommit.getBaseline() );
			}
		} else {
			logger.warning( "I don't know how to do this!!!" );
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
			List<Baseline> baselines = this.devStream_in.getBaselines( getComponent(), null, offset );
			
			/* TODO Clear out baselines before offset */
			if( offset != null ) {
				
			}
			
			for( int i = 0 ; i < baselines.size() ; i++ ) {				
				ClearcaseCommit commit = new ClearcaseCommit( baselines.get( i ), ClearcaseBranch.this, i );
				
				if( load ) {
					commit.load();
				}
				commits.add( commit );
			}
		} catch (UCMException e) {
			logger.error( "Could not list baselines: " + e.getMessage() );
		}
		
		return commits;
	}
	
	
	public SnapshotView getSnapshotView() {
		return snapshot_in;
	}
	
	public PVob getPVob() {
		return pvob;
	}
	
	public Vob getVob() {
		return vob;
	}
	
	public Baseline getBaseline() {
		return baseline;
	}
	
	@Deprecated
	public File getDevelopmentPath() {
		return this.developmentPath_in;
	}
	
	public void setInputPath( File path ) {
		this.viewroot_in = path;
	}
	
	public File getInputPath() {
		return this.viewroot_in;
	}
	
	public void setOutputPath( File path ) {
		this.viewroot_out = path;
	}
	
	public File getOutputPath() {
		return this.viewroot_out;
	}
	
	@Override
	public File getPath() {
		return this.viewroot_out;
	}
	
	public Component getComponent() {
		return this.component;
	}

	@Override
	public boolean cleanup() {
		return true;
	}

}
