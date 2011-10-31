package net.praqma.vcs.model.clearcase;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.praqma.clearcase.PVob;
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

/**
 * An implementation of {@link AbstractBranch} for Clearcase, where {@link Baseline}'s are used as commit separator.
 * @author wolfgang
 *
 */
public class ClearcaseBranch extends AbstractBranch{
	
	protected ClearcaseBranchPart input;
	protected ClearcaseBranchPart output;
	
	protected PVob pvob;
	protected Component component;
	protected Baseline baseline;
	
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
	public ClearcaseBranch( PVob pvob, Stream parent, Baseline baseline, File viewroot, String viewtag, String name ) throws ElementNotCreatedException {
		super(viewroot, name);
		
		input = new ClearcaseBranchPart( pvob, parent, baseline, new File( viewroot, "_in" ), viewtag, name );
		output = new ClearcaseBranchPart( pvob, parent, baseline, new File( viewroot, "_out" ), viewtag + "_out", name + "_out" );
		
		this.pvob = input.getPVob();
		this.component = input.getComponent();
		this.baseline = input.getBaseline();

		/* Set this to the view root of the out view */
		this.localRepositoryPath = output.getPath();
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
	public ClearcaseBranch( PVob pvob, Component component, File viewroot, String viewtag, String name ) throws ElementNotCreatedException {
		super(viewroot, name);
		
		input = new ClearcaseBranchPart( pvob, component, new File( viewroot, "_in" ), viewtag, name );
		output = new ClearcaseBranchPart( pvob, component, new File( viewroot, "_out" ), viewtag + "_out", name + "_out" );
		
		this.pvob = input.getPVob();
		this.component = input.getComponent();
		this.baseline = input.getBaseline();
		
		/* Set this to the view root of the out view */
		this.localRepositoryPath = output.getPath();
	}
	
	public ClearcaseBranch( ClearcaseBranchPart input, ClearcaseBranchPart output ) throws ElementNotCreatedException {
		super(input.getPath(), input.getStream().getShortname());
		
		this.input = input;
		this.output = output;
		
		this.pvob = input.getPVob();
		this.component = input.getComponent();
		this.baseline = input.getBaseline();
		
		/* Set this to the view root of the out view */
		this.localRepositoryPath = output.getPath();
	}
	
	public ClearcaseBranch( ClearcaseBranchPart input, boolean isInput ) throws ElementNotCreatedException {
		super(input.getPath(), input.getStream().getShortname());
		
		if( isInput ) {
			this.input = input;
		} else {
			this.output = input;
		}
		
		this.pvob = input.getPVob();
		this.component = input.getComponent();
		this.baseline = input.getBaseline();
		
		/* Set this to the view root of the out view */
		this.localRepositoryPath = input.getPath();
	}

	
	
	@Override
	public void initialize() throws ElementNotCreatedException, ElementAlreadyExistsException {
		try {
			initialize( false );
		} catch( ElementDoesNotExistException e ) {
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
	
	protected class InitializeImpl extends Initialize {
		public InitializeImpl( boolean get ) {
			super( get );
		}

		public boolean initialize() throws ElementDoesNotExistException, ElementNotCreatedException, ElementAlreadyExistsException {

			
			return true;
		}
	}
	
	
	
	public boolean exists() {
		boolean result = true;
		
		if( input != null ) {
			if( !input.exists() ) {
				result = false;
			}
		}
		
		if( output != null ) {
			if( !output.exists() ) {
				result = false;
			}
		}
		
		return result;
	}
	
	public void get() throws ElementDoesNotExistException {
		try {
			get( false );
		} catch( ElementNotCreatedException e ) {
			/* This should not happen */
			/* TODO Should we throw DoesNotExist? */
			logger.fatal( "This shouldn't be possible..." );
		}
	}
	
	public void get( boolean initialize ) throws ElementDoesNotExistException, ElementNotCreatedException {
		
		boolean exists = exists();
		
		if( !exists && initialize ) {
			logger.debug( "Must initialize" );
			try {
				initialize( true );
			} catch (ElementAlreadyExistsException e) {
				/* This should not happen */
				/* TODO Should we throw ElementAlreadyExistsException? */
				logger.fatal( "This shouldn't be possible..." );
			}
		} else {
			logger.debug( "DONT initialize" );
			if( input != null ) {
				input.initializeView();
			}
			
			if( output != null ) {
				output.initializeView();
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
			//if( snapshot_in == null || devStream_in == null ) {
			if( input.getSnapshotView() == null || input.getStream() == null ) {
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
				input.getSnapshotView().Update( true, true, true, false, COMP.ALL, null );
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
		
		if( commit instanceof ClearcaseCommit ) {
			ClearcaseCommit cccommit = (ClearcaseCommit)commit;
			//this.devStream_out.rebase( this.snapshot_out, cccommit.getBaseline(), true );
			this.output.getStream().rebase( this.output.getSnapshotView(), cccommit.getBaseline(), true );
			try {
				//this.snapshot_out.Update(true, true, true, false, COMP.ALL, null);
				this.output.getSnapshotView().Update(true, true, true, false, COMP.ALL, null);
			} catch (UCMException e) {
				throw new UnableToCheckoutCommitException( "Could not checkout " + cccommit.getBaseline() );
			}
		} else {
			logger.warning( "I don't know how to do this!!!" );
			throw new UnableToCheckoutCommitException( "This is not a ClearCase commit!" );
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
			//List<Baseline> baselines = this.devStream_in.getBaselines( getComponent(), null, offset );
			List<Baseline> baselines = this.input.getStream().getBaselines( getComponent(), null, offset );
			
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
		//return snapshot_in;
		return input.getSnapshotView();
	}
	
	public PVob getPVob() {
		return pvob;
	}
	
	public Baseline getBaseline() {
		return baseline;
	}
	
	public void setInputPath( File path ) {
		//this.viewroot_in = path;
		this.input.setPath( path );
	}
	
	public File getInputPath() {
		//return this.viewroot_in;
		return this.input.getPath();
	}
	
	public void setOutputPath( File path ) {
		//this.viewroot_out = path;
		this.output.setPath( path );
	}
	
	public File getOutputPath() {
		//return this.viewroot_out;
		return this.output.getPath();
	}
	
	@Override
	public File getPath() {
		//return this.developmentPath_out;
		//return this.viewroot_out;
		return this.output.getPath();
	}
	
	public File getPathIn() {
		//return this.developmentPath_out;
		//return this.viewroot_in;
		return this.input.getPath();
	}
		
	public Component getComponent() {
		return this.component;
	}
	
	public Stream getInputStream() {
		//return devStream_in;
		return this.input.getStream();
	}
	
	public Stream getOutputStream() {
		//return devStream_out;
		return this.input.getStream();
	}

	@Override
	public boolean cleanup() {
		return true;
	}

}
