package net.praqma.vcs.model.clearcase;

import java.io.File;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.view.DynamicView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractVCS;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementException.FailureType;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;

/**
 * This is a Clearcase implementation of the abstract VCS. Initializing this
 * kind of VCS will result in one {@link Vob} and one {@link Component} per
 * repository.
 * 
 * @author wolfgang
 * 
 */
public class ClearCaseVCS extends AbstractVCS {
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
	private static String pvobName = "\\AVA_PVOB2";

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
	private Project project;

	private PVob pvob;

	private Vob lastCreatedVob;

	public ClearCaseVCS( File location ) {
		super( location );
	}

	public static void setPVob( PVob pvob ) {

	}

	/**
	 * Create and initialize an instance of {@link ClearCaseVCS}
	 * 
	 * @param location
	 * @param baseName
	 *            The basename of the repository, which will serve as the name
	 *            for the {@link Component} and the {@link Vob}.
	 * @param policies
	 *            The policies of the project
	 * @param pvob
	 *            The {@link PVob}
	 * @return {@link ClearCaseVCS}
	 * @throws ElementNotCreatedException
	 * @throws ElementDoesNotExistException
	 */
	public static ClearCaseVCS create( File location, String baseName, int policies, PVob pvob ) throws ElementNotCreatedException, ElementDoesNotExistException {
		ClearCaseVCS cc = new ClearCaseVCS( location );

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

	public static ClearCaseVCS create( File location, String vobName, String componentName, int policies, PVob pvob ) throws ElementNotCreatedException, ElementDoesNotExistException {
		ClearCaseVCS cc = new ClearCaseVCS( location );

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

	public ClearCaseVCS( File location, String vobName, String componentName, String projectName, String streamName, int policies, PVob pvob ) {
		super( location );

		this.baseName = componentName;
		this.vobName = "\\" + vobName;
		this.streamName = streamName;
		this.projectName = projectName;
		this.baselineName = componentName + "_Structure_1_0";

		this.policies = policies;

		this.pvob = pvob;

	}

	public static ClearCaseVCS create( File location, String vobName, String componentName, String projectName, String streamName, int policies, PVob pvob ) throws ElementNotCreatedException, ElementDoesNotExistException {
		ClearCaseVCS cc = new ClearCaseVCS( location );

		cc.initialize();
		return cc;
	}

	public boolean exists() {

		boolean result = true;

		try {
			Component.get( ClearCaseVCS.this.baseName, pvob ).load();
		} catch( ClearCaseException e1 ) {
			logger.debug( "Component does not exist" );
			result = false;
		}

		try {
			Project.get( ClearCaseVCS.this.projectName, pvob ).load();
		} catch( ClearCaseException e1 ) {
			logger.debug( "Project does not exist" );
			result = false;
		}

		try {
			Stream.get( ClearCaseVCS.this.streamName, pvob ).load();
		} catch( ClearCaseException e1 ) {
			logger.debug( "Stream does not exist" );
			result = false;
		}

		try {
			Baseline.get( ClearCaseVCS.this.baselineName, pvob ).load();
		} catch( ClearCaseException e1 ) {
			logger.debug( "Baseline does not exist" );
			result = false;
		}

		return result;
	}

	public void get() throws ElementDoesNotExistException {
		try {
			get( false );
		} catch( ElementNotCreatedException e ) {
			/* This should not happen */
			/* TODO Should we throw DoesNotExist? */
		}
	}

	@Override
	public void get( boolean initialize ) throws ElementNotCreatedException, ElementDoesNotExistException {
		if( initialize ) {
			initialize( true );
		} else {
			try {
				this.project = Project.get( ClearCaseVCS.this.projectName, pvob ).load();
			} catch( ClearCaseException e1 ) {
				logger.error( "Project does not exist" );
				throw new ElementDoesNotExistException( "Project does not exist" );
			}

			try {
				this.integrationStream = Stream.get( ClearCaseVCS.this.streamName, pvob ).load();
			} catch( ClearCaseException e1 ) {
				logger.error( "Stream does not exist" );
				throw new ElementDoesNotExistException( "Stream does not exist" );
			}

			try {
				this.initialBaseline = Baseline.get( ClearCaseVCS.this.baselineName, pvob ).load();
			} catch( ClearCaseException e1 ) {
				logger.error( "Baseline does not exist" );
				throw new ElementDoesNotExistException( "Baseline does not exist" );
			}
		}
	}

	/**
	 * Initializes an instance of a {@link ClearCaseVCS}
	 * 
	 * @throws ElementNotCreatedException
	 * @throws ElementDoesNotExistException
	 */
	@Override
	public void initialize() throws ElementNotCreatedException, ElementDoesNotExistException {
		initialize( false );
	}

	@Override
	public void initialize( boolean get ) throws ElementNotCreatedException, ElementDoesNotExistException {
		logger.info( "Initializing Clearcase Repository" );
		InitializeImpl init = new InitializeImpl( get );

		doInitialize( init );

		lastCreatedVob = init.getVob();
		initialBaseline = init.getBaseline();
	}

	public class InitializeImpl extends Initialize {

		private Baseline baseline;
		private Vob vob;

		public InitializeImpl( boolean get ) {
			super( get );
		}

		public boolean setup() {
			return true;
		}

		public Baseline getBaseline() {
			return baseline;
		}

		public Vob getVob() {
			return vob;
		}

		public boolean initialize() throws ElementNotCreatedException, ElementDoesNotExistException {
			/* TODO should implement setup env from cool */
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
	 * 
	 * @return {@link PVob}
	 */
	public PVob getPVob() {
		return pvob;
	}

	/**
	 * Returns the last created {@link Vob}
	 * 
	 * @return {@link Vob}
	 */
	public Vob getLastCreatedVob() {
		return lastCreatedVob;
	}

	/**
	 * Get the integration {@link Stream} for the {@link Project}(repository)
	 * 
	 * @return {@link Stream}
	 */
	public Stream getIntegrationStream() {
		return integrationStream;
	}

	/**
	 * Get the initial {@link Baseline} for the {@link Project}(repository)
	 * 
	 * @return {@link Baseline}
	 */
	public Baseline getInitialBaseline() {
		return initialBaseline;
	}

	/**
	 * Boot strap a project/repository given default values
	 * 
	 * @return The {@link PVob} of the project
	 * @throws ElementNotCreatedException
	 */
	public static PVob bootstrap() throws ElementNotCreatedException {
		return bootstrap( ClearCaseVCS.pvobName, ClearCaseVCS.viewPath );
	}

	public static PVob bootstrap( String pvobName ) throws ElementNotCreatedException {
		return bootstrap( pvobName, ClearCaseVCS.viewPath );
	}

	/**
	 * Boot strap a project/repository, given a pvobname and path to dynamic
	 * views
	 * 
	 * @param pvobName
	 *            String
	 * @param viewPath
	 *            {@link File}
	 * @return The {@link PVob} of the project
	 * @throws ElementNotCreatedException
	 */
	public static PVob bootstrap( String pvobName, File viewPath ) throws ElementNotCreatedException {
		logger.info( "Bootstrapping PVOB " + pvobName );

		ClearCaseVCS.viewPath = viewPath;

		PVob pvob = PVob.get( pvobName );
		if( pvob == null ) {
			logger.info( "Creating PVOB" );
			try {
				pvob = PVob.create( pvobName, null, "PVOB" );
			} catch( ClearCaseException e ) {
				logger.error( "Error while creating PVOB: " + e.getMessage() );
				throw new ElementNotCreatedException( "Could not create PVob: " + e.getMessage(), FailureType.INITIALIZATON );
			}
		}

		if( !UCMView.viewExists( ClearCaseVCS.dynView ) ) {
			try {
				logger.info( "Creating baseview" );
				baseView = DynamicView.create( null, dynView, null );
			} catch( Exception e ) {
				logger.error( "Error while creating baseview: " + e.getMessage() );
				throw new ElementNotCreatedException( "Could not create base view: " + e.getMessage(), FailureType.INITIALIZATON );
			}
		} else {
			try {
				new DynamicView( null, dynView ).startView();
			} catch( ClearCaseException e ) {
				logger.error( "Error while starting baseview: " + e.getMessage() );
				throw new ElementNotCreatedException( "Could not starting base view: " + e.getMessage(), FailureType.INITIALIZATON );
			}
		}

		return pvob;
	}

	@Override
	public boolean cleanup() {
		return true;
	}

}
