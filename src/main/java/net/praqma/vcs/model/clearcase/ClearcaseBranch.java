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
	 */
	private File developmentPath_in;
	
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
	 * 
	 */
	private ClearcaseVCS ccVCS;
	
	
	private Component component;
	
	private static Logger logger = Logger.getLogger();
	
	public ClearcaseBranch( ClearcaseVCS ccVCS, Vob vob, Stream parent, Baseline baseline, File viewroot, String viewtag, String name ) throws ElementNotCreatedException {
		super(viewroot, name);
		this.name_in = name;
		this.name_out = name + "_out";
		this.viewroot = viewroot;
		this.viewroot_in = new File( viewroot, "in" );
		this.viewroot_out = new File( viewroot, "out" );
		this.viewtag_in = viewtag;
		this.viewtag_out = viewtag + "_out";
		this.baseline = baseline;
		
		this.parent = parent;
		
		this.vob = vob;
		this.ccVCS = ccVCS;
		
		try {
			this.component = baseline.getComponent();
		} catch (UCMException e) {
			logger.error( "Could not create Clearcase branch: " + e.getMessage() );
			throw new ElementNotCreatedException( "Could not create Clearcase branch: " + e.getMessage(), FailureType.DEPENDENCY );
		}
		
		this.developmentPath_in = new File( viewroot, vob + "/" + this.component.getShortname() );
		
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
		try {
			initialize(false);
		} catch (ElementDoesNotExistException e) {
			/* This shouldn't be possible */
			logger.fatal( "False shouldn't throw exist exceptions!!!" );
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

		public boolean initialize() throws ElementDoesNotExistException, ElementNotCreatedException {

			/* Create input stream */
			try {
				logger.info( "Creating development input stream"  );
				devStream_in = Stream.create( parent, name_in + "@" + ccVCS.getPVob(), false, baseline );
			} catch (UCMException e) {
				if( get ) {
					try {
						devStream_in = UCMEntity.getStream( name_in, ccVCS.getPVob(), false );
						logger.info( "Input stream already exists" );
					} catch (UCMException e1) {
						logger.error( "Could not find input stream: " + e.getMessage() );
						throw new ElementDoesNotExistException( "Could not find input stream: " + e.getMessage() );
					}
				} else {
					logger.error( "Error while creating Development input Stream: " + e.getMessage() );
					throw new ElementNotCreatedException( "Error while creating Development input Stream: " + e.getMessage() );
				}
			}
			
			/* Create output stream
			 * This is a read only stream, allowing rebasing. */
			try {
				logger.info( "Creating development output stream"  );
				devStream_out = Stream.create( parent, name_out + "@" + ccVCS.getPVob(), true, baseline );
			} catch (UCMException e) {
				if( get ) {
					try {
						devStream_out = UCMEntity.getStream( name_out, ccVCS.getPVob(), false );
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
			
			/* Creating inout view */
			try {
				logger.info( "Creating development input view" );
				viewroot_in.mkdirs();
				snapshot_in = SnapshotView.Create( devStream_in, viewroot_in, viewtag_in );
			} catch (UCMException e) {
				if( get ) {
					try {
						snapshot_in = UCMView.GetSnapshotView(viewroot_in);
						logger.info( "Input view already exists" );
					} catch (Exception e1) {
						logger.error( "Could not find input view: " + e.getMessage() );
						/* try to generate new input view */
						try {
							snapshot_in = SnapshotView.Create( devStream_in, viewroot_in, viewtag_in + System.currentTimeMillis() );
						} catch (UCMException e2) {
							logger.error( "Could not generate new input view: " + e2.getMessage() );
							throw new ElementNotCreatedException( "Could not generate new input view: " + e2.getMessage() );
						}
					}
				} else {
					logger.error("Error while creating input Snapshot View: " + e.getMessage());
					throw new ElementNotCreatedException( "Error while creating input Snapshot View: " + e.getMessage() );
				}
			}
			
			/* Creating output view */
			try {
				logger.info( "Creating development ouput view" );
				viewroot_out.mkdirs();
				snapshot_out = SnapshotView.Create( devStream_out, viewroot_out, viewtag_out );
			} catch (UCMException e) {
				if( get ) {
					try {
						snapshot_out = UCMView.GetSnapshotView(viewroot_out);
						logger.info( "Output view already exists" );
					} catch (Exception e1) {
						logger.error( "Could not find output view: " + e.getMessage() );
						/* try to generate new output view */
						try {
							snapshot_out = SnapshotView.Create( devStream_out, viewroot_out, viewtag_out + System.currentTimeMillis() );
						} catch (UCMException e2) {
							logger.error( "Could not generate new output view: " + e2.getMessage() );
							throw new ElementNotCreatedException( "Could not generate new output view: " + e2.getMessage() );
						}
					}
				} else {
					logger.error("Error while creating output Snapshot View: " + e.getMessage());
					throw new ElementNotCreatedException( "Error while creating output Snapshot View: " + e.getMessage() );
				}
			}
			
			return true;
		}
	}
	
	
	
	public boolean exists() {
		boolean result = true;
		
		/* Test input stream */
		try {
			UCMEntity.getStream( name_in, ccVCS.getPVob(), false );
		} catch (UCMException e1) {
			logger.error( "Input stream does not exist" );
		}
		
		/* Test output stream */
		try {
			UCMEntity.getStream( name_out, ccVCS.getPVob(), false );
		} catch (UCMException e1) {
			logger.error( "Output stream does not exist" );
		}

		/* Test input view */
		if( !UCMView.ViewExists( viewtag_in )) {
			logger.debug( "Input view tag does not exist" );
			result = false;
		} else {
			try {
				UCMView.GetSnapshotView(viewroot_in);
			} catch (Exception e1) {
				logger.debug( "Input view root does not exist" );
				result = false;
			}
		}
		
		/* Test output view */
		if( !UCMView.ViewExists( viewtag_out )) {
			logger.debug( "Output view tag does not exist" );
			result = false;
		} else {
			try {
				UCMView.GetSnapshotView(viewroot_out);
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
		try{
			this.devStream_in = UCMEntity.getStream( name_in, ccVCS.getPVob(), false );
		} catch( UCMException e ) {
			logger.debug( "Input stream did not exist" );
			exists = false;
		}
		
		try{
			this.devStream_out = UCMEntity.getStream( name_out, ccVCS.getPVob(), false );
		} catch( UCMException e ) {
			logger.debug( "Output stream did not exist" );
			exists = false;
		}
		
		if( !UCMView.ViewExists( viewtag_in ) ) {
			logger.debug( "Input View did not exist" );
			exists = false;
		/* The view exists, but is the path correct? */
		} else {
			if( !viewroot_in.exists() ) {
				exists = false;
			}
		}
		
		if( !UCMView.ViewExists( viewtag_out ) ) {
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
				snapshot_in = UCMView.GetSnapshotView(viewroot_in);
			} catch (UCMException e) {
				logger.error( "Could not get input view: " + e.getMessage() );
				throw new ElementDoesNotExistException( "Could not get input clearcase view" );
			}
			
			try {
				snapshot_out = UCMView.GetSnapshotView(viewroot_out);
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
	public void checkoutCommit( AbstractCommit commit ) {
		this.currentCommit = commit;
		/* TODO how to checkout a commit i CC? */
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
			List<Baseline> baselines = this.devStream_in.getBaselines( getComponent(), null );
			
			/* TODO Clear out baselines before offset */
			if( offset != null ) {
				
			}
			
			for( int i = 0 ; i < baselines.size() ; i++ ) {
				System.out.print( "\r" + Utils.getProgress( baselines.size(), i ) );
				
				ClearcaseCommit commit = new ClearcaseCommit( baselines.get( i ), ClearcaseBranch.this, i );
				
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
		return snapshot_in;
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
		return this.developmentPath_in;
	}
	
	public Component getComponent() {
		return this.component;
	}

	@Override
	public boolean cleanup() {
		return true;
	}

}
