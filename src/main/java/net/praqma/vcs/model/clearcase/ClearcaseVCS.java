package net.praqma.vcs.model.clearcase;

import java.io.File;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.view.DynamicView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractVCS;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException.FailureType;

/**
 * This is a Clearcase implementation of the abstract VCS.
 * Initializing this kind of VCS will result in one {@link Vob} and one {@link Component} per repository.
 * @author wolfgang
 *
 */
public class ClearcaseVCS extends AbstractVCS {
	private static Logger logger = Logger.getLogger();

	private static String dynView = "AVA_baseview";
	private static DynamicView baseView;
	
	/**
	 * This is the default path to dynamic views
	 */
	private static File viewPath = new File( "m:/" );
	
	/**
	 * This is the default PVob name
	 */
	private static String pvobName = "\\AVA_PVOB";

	private String bootView = "AVA_bootstrapview";

	private String baseName;
	private String vobName;
	private String projectName;
	private String streamName;
	private String baselineName;
	
	/**
	 * The policies for the repository
	 */
	private int policies = 0;
	private Stream integrationStream;
	private Baseline initialBaseline;

	private PVob pvob;
	
	private Vob lastCreatedVob;

	public ClearcaseVCS( File location ) {
		super( location );
	}

	/**
	 * Create and initialize an instance of {@link ClearcaseVCS}
	 * @param location 
	 * @param baseName The basename of the repository, which will serve as the name for the {@link Component} and the {@link Vob}.
	 * @param policies The policies of the project
	 * @param pvob The {@link PVob}
	 * @return {@link ClearcaseVCS}
	 * @throws ElementNotCreatedException
	 */
	public static ClearcaseVCS create( File location, String baseName, int policies, PVob pvob ) throws ElementNotCreatedException {
		ClearcaseVCS cc = new ClearcaseVCS( location );

		cc.baseName = baseName;
		cc.vobName = "\\" + baseName;
		cc.streamName = baseName + "_Mainline_int";
		cc.projectName = baseName + "_Mainline";
		cc.baselineName = baseName + "_Structure_1_0";
		
		cc.policies = policies;

		cc.pvob = pvob;
		
		cc.initialize();
		
		return cc;
	}
	
	public static ClearcaseVCS create( File location, String vobName, String componentName, int policies, PVob pvob ) throws ElementNotCreatedException {
		ClearcaseVCS cc = new ClearcaseVCS( location );

		cc.baseName = componentName;
		cc.vobName = "\\" + vobName;
		cc.streamName = componentName + "_Mainline_int";
		cc.projectName = componentName + "_Mainline";
		cc.baselineName = componentName + "_Structure_1_0";
		
		cc.policies = policies;

		cc.pvob = pvob;
		
		cc.initialize();
		
		return cc;
	}
	
	public static ClearcaseVCS create( File location, String vobName, String componentName, String projectName, String streamName, int policies, PVob pvob ) throws ElementNotCreatedException {
		ClearcaseVCS cc = new ClearcaseVCS( location );

		cc.baseName = componentName;
		cc.vobName = "\\" + vobName;
		cc.streamName = streamName;
		cc.projectName = projectName;
		cc.baselineName = componentName + "_Structure_1_0";
		
		cc.policies = policies;

		cc.pvob = pvob;
		
		if( cc.initialize() ) {
			return cc;	
		} else {
			throw new ElementNotCreatedException( "Could not create repository", FailureType.INITIALIZATON );
		}
		
		
	}

	@Override
	public boolean get() throws ElementNotCreatedException {
		/* TODO Actually check if Stream and view exists */
		if( initialize( true ) ) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Initializes an instance of a {@link ClearcaseVCS}
	 */
	@Override
	public boolean initialize() throws ElementNotCreatedException {
		return initialize( false );
	}
	
	@Override
	public boolean initialize( boolean get ) throws ElementNotCreatedException {
		logger.info( "Initializing Clearcase Repository" );
		InitializeImpl init = new InitializeImpl(get);
		boolean result = doInitialize( init );
		
		lastCreatedVob = init.getVob();
		initialBaseline = init.getBaseline();
		
		return result;
	}

	public class InitializeImpl extends Initialize {

		private Baseline baseline;
		private Vob vob;

		public InitializeImpl( boolean get ) {
			super(get);
		}

		public boolean setup() {
			
			/*
			Vob v = Vob.get( ClearcaseVCS.this.vobName );
			if( v != null ) {
				logger.info( "Removing vob" );
				try {
					v.remove();
				} catch (UCMException e) {
					logger.error( "Error while removing vob: " + e.getMessage() );
					return false;
				}
			}
			*/

			return true;
		}
		
		public Baseline getBaseline() {
			return baseline;
		}
		
		public Vob getVob() {
			return vob;
		}

		public boolean initialize() {

			/* Create Vob */
			/* Test existence before creation */
			vob = Vob.get( ClearcaseVCS.this.vobName );
			if( vob == null ) {
				try {
					logger.info( "Creating Vob " + ClearcaseVCS.this.vobName );
					vob = Vob.create( ClearcaseVCS.this.vobName, null, ClearcaseVCS.this.vobName + " Vob" );
				} catch (UCMException e) {
					logger.error( "Error while creating Vob: " + e.getMessage() );
					return false;
				}
			} else {
				logger.info("Vob already exists");
			}
			
			logger.info( "Loading Vob " + vob );
			try {
				vob.load();
			} catch (UCMException e) {
				logger.error( "Error while creating PVob: " + e.getMessage() );
				return false;
			}
			try {
				vob.mount();
			} catch (UCMException e) {
				logger.error( "Error while mounting Vob: " + e.getMessage() );
				return false;
			}
			logger.info( "Mounted Vob " + vob );

			
			/* Create component */
			Component c = null;
			try {
				logger.info("Creating Component " + ClearcaseVCS.this.baseName);
				File basepath = new File( ClearcaseVCS.viewPath, ClearcaseVCS.dynView + "/" + vob.getName() );
				logger.debug( "Baseview path: " + basepath.getAbsolutePath() );
				c = Component.create( ClearcaseVCS.this.baseName, pvob, ClearcaseVCS.this.baseName, "Main component", basepath );
			} catch (UCMException e) {
				if( get ) {
					logger.warning("Could not create Component, trying to continue: " + e.getMessage());
					try {
						c = UCMEntity.getComponent( ClearcaseVCS.this.baseName, pvob, false );
					} catch (UCMException e1) {
						logger.error( "Component does not exist: " + e1.getMessage() );
						return false;
					}
				} else {
					logger.error( "Error while creating Component: " + e.getMessage() );
					return false;
				}
			}
			logger.debug( "Component=" + c );

			// logger.info("Creating " + ClearcaseVCS.this.baseName +
			// " Structure_1_0");
			logger.debug( "Getting " + ClearcaseVCS.this.baseName + "_INITIAL" );
			Baseline initial;
			try {
				initial = UCMEntity.getBaseline( ClearcaseVCS.this.baseName + "_INITIAL", pvob, true );
			} catch (UCMException e) {
				logger.error( "Error while creating Structure Baseline: " + e.getMessage() );
				return false;
			}

			logger.info( "Creating Mainline project" );
			Project mainlineproject;
			try {
				mainlineproject = Project.create( ClearcaseVCS.this.projectName, null, pvob, policies, "Mainline project", c );
			} catch (UCMException e) {
				if( get ) {
					try {
						logger.warning( "Could not create project: " + e.getMessage() );
						mainlineproject = UCMEntity.getProject( ClearcaseVCS.this.projectName, pvob, false );
					} catch (UCMException e1) {
						logger.error( "Project does not exist: " + e1.getMessage() );
						return false;
					}
				} else {
					logger.error( "Error while creating Mainline Project: " + e.getMessage() );
					return false;
				}
			}

			logger.info( "Creating Mainline integration stream" );
			try {
				integrationStream = Stream.createIntegration( ClearcaseVCS.this.streamName, mainlineproject, initial );
			} catch (UCMException e) {
				if( get ) {
					try {
						logger.warning( "Could not integration stream: " + e.getMessage() );
						integrationStream = UCMEntity.getStream( ClearcaseVCS.this.streamName, pvob, false );
					} catch (UCMException e1) {
						logger.error( "Stream does not exist: " + e1.getMessage() );
						return false;
					}
				} else {
					logger.error( "Error while creating Mainline Integration Stream: " + e.getMessage() );
					return false;
				}
			}
			
			logger.info( "Creating integration view" );
			DynamicView bview = null;
			if( UCMView.ViewExists( ClearcaseVCS.this.baseName + "_" + bootView )) {
				try {
					bview = DynamicView.create( null, ClearcaseVCS.this.baseName + "_" + bootView, integrationStream );
				} catch (UCMException e) {
					logger.error( "Error while creating Integration view: " + e.getMessage() );
					return false;
				}
			} else {
				try {
					logger.info( "View exists, trying to start view server" );
					new DynamicView( null, ClearcaseVCS.this.baseName + "_" + bootView ).startView();
				} catch (UCMException e) {
					logger.error( "Error while starting baseview: " + e.getMessage() );
					return false;
				}
			}

			logger.info( "Creating Structure_1_0" );
			try {
				//baseline = Baseline.create( ClearcaseVCS.this.baseName + "_Structure_1_0", c, new File( viewPath, ClearcaseVCS.this.baseName + "_" + bootView + "/" + ClearcaseVCS.this.baseName ), false, true );
				baseline = Baseline.create( ClearcaseVCS.this.baselineName, c, new File( viewPath, bview.GetViewtag() + "/" + vob.getName() ), false, true );
			} catch (UCMException e) {
				if( get ) {
					try {
						logger.warning( "Baseline exists: " + e.getMessage() );
						baseline = UCMEntity.getBaseline( ClearcaseVCS.this.baselineName, pvob, false );
					} catch (UCMException e1) {
						logger.error( "Baseline does not exist: " + e1.getMessage() );
						return false;
					}
				} else {
					logger.error( "Error while creating Structure Baseline: " + e.getMessage() );
					return false;
				}
			}

			return true;
		}

		public boolean cleanup( boolean status ) {
			if( !status ) {
				logger.error( "Error while initializing" );
			}

			return true;
		}
	}

	/**
	 * Returns the {@link PVob} of the repository
	 * @return {@link PVob}
	 */
	public PVob getPVob() {
		return pvob;
	}
	
	/**
	 * Returns the last created {@link Vob}
	 * @return {@link Vob}
	 */
	public Vob getLastCreatedVob() {
		return lastCreatedVob;
	}
	
	/**
	 * Get the integration {@link Stream} for the {@link Project}(repository)
	 * @return {@link Stream}
	 */
	public Stream getIntegrationStream() {
		return integrationStream;
	}
	
	/**
	 * Get the initial {@link Baseline} for the {@link Project}(repository)
	 * @return {@link Baseline}
	 */
	public Baseline getInitialBaseline() {
		return initialBaseline;
	}
	
	/**
	 * Boot strap a project/repository given default values
	 * @return The {@link PVob} of the project
	 * @throws ElementNotCreatedException
	 */
	public static PVob bootstrap() throws ElementNotCreatedException {
		return bootstrap( ClearcaseVCS.pvobName, ClearcaseVCS.viewPath );
	}
	
	/**
	 * Boot strap a project/repository, given a pvobname and path to dynamic views
	 * @param pvobName String
	 * @param viewPath {@link File}
	 * @return The {@link PVob} of the project
	 * @throws ElementNotCreatedException
	 */
	public static PVob bootstrap( String pvobName, File viewPath ) throws ElementNotCreatedException {
		logger.info( "Bootstrapping PVOB " + pvobName );

		ClearcaseVCS.viewPath = viewPath;

		PVob pvob = PVob.get( pvobName );
		if( pvob == null ) {
			logger.info( "Creating PVOB" );
			try {
				pvob = PVob.create( pvobName, null, "PVOB" );
			} catch (UCMException e) {
				logger.error( "Error while creating PVOB: " + e.getMessage() );
				throw new ElementNotCreatedException( "Could not create PVob: " + e.getMessage(), FailureType.INITIALIZATON );
			}
		}

		if( !UCMView.ViewExists( ClearcaseVCS.dynView ) ) {
			try {
				logger.info( "Creating baseview" );
				baseView = DynamicView.create( null, dynView, null );
			} catch (Exception e) {
				logger.error( "Error while creating baseview: " + e.getMessage() );
				throw new ElementNotCreatedException( "Could not create base view: " + e.getMessage(), FailureType.INITIALIZATON );
			}
		} else {
			try {
				new DynamicView( null, dynView).startView();
			} catch (UCMException e) {
				logger.error( "Error while starting baseview: " + e.getMessage() );
				throw new ElementNotCreatedException( "Could not starting base view: " + e.getMessage(), FailureType.INITIALIZATON );
			}
		}

		return pvob;
	}

}
