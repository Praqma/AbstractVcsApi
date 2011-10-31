package net.praqma.vcs.model.clearcase;

import java.io.File;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.exceptions.ElementAlreadyExistsException;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementException.FailureType;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;

/**
 * An implementation of {@link AbstractBranch} for Clearcase, where {@link Baseline}'s are used as commit separator.
 * @author wolfgang
 *
 */
public class ClearcaseBranchPart {

	/**
	 * The out most view root, ideally containing the
	 * folders in and/ out/
	 */
	protected File viewroot;
	
	/**
	 * The name of the stream
	 */
	protected String name;
		
	/**
	 * The view tag of the view
	 */
	protected String viewtag;
	
	/**
	 * The {@link Stream} of the part
	 */
	protected Stream devStream;
	
	/**
	 * The {@link SnapshotView} of the part
	 */
	protected SnapshotView snapshot;
	
	/**
	 * The foundation {@link Baseline} of the branch
	 */
	protected Baseline baseline;
	
	/**
	 * The parent {@link Stream} of the branch
	 */
	protected Stream parent;
		
	/**
	 * The {@link Project}s {@link PVob}
	 */
	protected PVob pvob;
	
	/**
	 * The component
	 */
	protected Component component;
	
	protected static Logger logger = Logger.getLogger();
	
	/**
	 * If the Stream does not exist, it will be created as a child of the Stream parent.
	 * @param pvob
	 * @param parent
	 * @param baseline
	 * @param viewroot
	 * @param viewtag
	 * @param name
	 * @throws ElementNotCreatedException
	 */
	public ClearcaseBranchPart( PVob pvob, Stream parent, Baseline baseline, File viewroot, String viewtag, String name ) throws ElementNotCreatedException {
		this.name = name;
		this.viewroot = viewroot;
		this.viewtag = viewtag;
		this.baseline = baseline;
		
		this.parent = parent;
		
		this.pvob = pvob;
		
		try {
			this.component = baseline.getComponent();
		} catch (UCMException e) {
			logger.error( "Could not create Clearcase branch: " + e.getMessage() );
			throw new ElementNotCreatedException( "Could not create Clearcase branch: " + e.getMessage(), FailureType.DEPENDENCY );
		}
	}
	
	public ClearcaseBranchPart( PVob pvob, Stream parent, File viewroot, String name ) throws ElementNotCreatedException {
		this.name = name;
		this.viewroot = viewroot;		
		this.parent = parent;		
		this.pvob = pvob;
		
		try {
			this.baseline = parent.getFoundationBaseline();
		} catch (UCMException e) {
			logger.error( "Could not create Clearcase branch: " + e.getMessage() );
			throw new ElementNotCreatedException( "Could not create Clearcase branch: " + e.getMessage(), FailureType.DEPENDENCY );
		}	
		
		try {
			this.component = baseline.getComponent();
		} catch (UCMException e) {
			logger.error( "Could not create Clearcase branch: " + e.getMessage() );
			throw new ElementNotCreatedException( "Could not create Clearcase branch: " + e.getMessage(), FailureType.DEPENDENCY );
		}
	}
	
	/**
	 * Constructor used for branches where everything is created.
	 * @param pvob
	 * @param component
	 * @param viewroot
	 * @param viewtag
	 * @param name
	 * @throws ElementNotCreatedException
	 */
	public ClearcaseBranchPart( PVob pvob, Component component, File viewroot, String viewtag, String name ) throws ElementNotCreatedException {
		this.name = name;
		this.viewroot = viewroot;
		this.viewtag = viewtag;
		
		this.pvob = pvob;
		
		this.component = component;
	}


	public boolean initialize( boolean useIfExists ) throws ElementDoesNotExistException, ElementNotCreatedException, ElementAlreadyExistsException {

		/* Create or get input stream */
		if( useIfExists ) {
			try {
				devStream = UCMEntity.getStream( name, pvob, false );
			} catch ( UCMException e ) {
				logger.debug( name + " was not found."  );
				if( parent != null ) {
					logger.debug( "Trying to create the stream " + name );
					try {
						devStream = Stream.create( parent, name + "@" + pvob, false, baseline );
					} catch (UCMException e1) {
						logger.error( "Error while creating input stream: " + e1.getMessage() );
						throw new ElementNotCreatedException( "Error while creating development stream: " + e1.getMessage() );
					}
				} else {
					logger.debug( "Unable to create the stream " + name + ". Parent is null" );
					throw new ElementNotCreatedException( "Error while creating development stream, parent is null" );
				}
			}
		} else {
			if( parent != null ) {
				try {
					logger.info( "Creating development stream"  );
					devStream = Stream.create( parent, name + "@" + pvob, false, baseline );
				} catch (UCMException e) {
					logger.error( "Error while creating Development input Stream: " + e.getMessage() );
					throw new ElementNotCreatedException( "Error while creating development stream: " + e.getMessage() );
				}
			} else {
				logger.debug( "Unable to create the stream " + name + ". Parent is null" );
				throw new ElementNotCreatedException( "Error while creating development stream, parent is null" );
			}
		}
		
		/* We got this far, let's set the baseline if null */
		if( baseline == null ) {
			try {
				baseline = devStream.getFoundationBaseline();
			} catch( UCMException e ) {
				logger.warning( "Unable to get foundation baseline. I wonder why?! " + e.getMessage() );
			} catch( NullPointerException e ) {
				logger.debug( "Could not get foundation baseline, because input stream was null" );
			}
		}
		
		
		/* Creating input view */
		if( useIfExists ) {
			try {
				snapshot = UCMView.getSnapshotView( viewroot );
				logger.info( "Using existing view" );
			} catch (Exception e1) {
				if( viewtag != null ) {
					try {
						logger.info( "Creating development view, " + viewtag );
						if( !viewroot.exists() ) {
							viewroot.mkdirs();
						}
						snapshot = SnapshotView.create( devStream, viewroot, viewtag );
					} catch (UCMException e) {
						logger.error( "Error while initializing input view: " + e.getMessage() );
					}
				} else {
					logger.debug( "Unable to create the view " + viewroot + ". View tag is null" );
					throw new ElementNotCreatedException( "Error while creating view, view tag is null" );
				}
			}
		} else {
			if( viewtag != null ) {
				try {
					logger.info( "Creating development input view, " + viewtag );
					if( !viewroot.exists() ) {
						viewroot.mkdirs();
					}
					snapshot = SnapshotView.create( devStream, viewroot, viewtag );
				} catch (UCMException e) {
					throw new ElementAlreadyExistsException( "The input view " + viewtag + " already exists" );
				}
			} else {
				logger.debug( "Unable to create the view " + viewroot + ". View tag is null" );
				throw new ElementNotCreatedException( "Error while creating view, view tag is null" );
			}
		}
		
		
		return true;
	}
	
	public boolean exists() {
		boolean result = true;
		
		try {
			UCMEntity.getStream( name, pvob, false );
		} catch (UCMException e1) {
			logger.error( "Stream does not exist" );
			result = false;
		}

		/* Test input view */
		if( !UCMView.viewExists( viewtag ) ) {
			logger.debug( "Input view tag, " + viewtag + ", does not exist" );
			result = false;
		} else {
			try {
				UCMView.getSnapshotView( viewroot );
			} catch (Exception e1) {
				logger.debug( "View does not exist" );
				result = false;
			}
		}
		
		return result;
	}
	
	public void initializeView() throws ElementDoesNotExistException {
		try {
			snapshot = UCMView.getSnapshotView( viewroot );
		} catch (UCMException e) {
			logger.error( "Could not get view: " + e.getMessage() );
			throw new ElementDoesNotExistException( "Could not get input clearcase view" );
		}
	}
	
	
	
	public SnapshotView getSnapshotView() {
		return snapshot;
	}
	
	public PVob getPVob() {
		return pvob;
	}
	
	public Baseline getBaseline() {
		return baseline;
	}
	
	public void setPath( File path ) {
		this.viewroot = path;
	}
		
	public File getPath() {
		return this.viewroot;
	}
		
	public Component getComponent() {
		return this.component;
	}
	
	public Stream getStream() {
		return devStream;
	}
}
