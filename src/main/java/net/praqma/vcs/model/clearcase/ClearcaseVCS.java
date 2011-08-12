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
import net.praqma.exceptions.ElementNotCreatedException;
import net.praqma.exceptions.ElementNotCreatedException.FailureType;
import net.praqma.util.debug.Logger;
import net.praqma.util.structure.tree.Tree;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractVCS;
import net.praqma.vcs.model.Repository;

public class ClearcaseVCS extends AbstractVCS {
	private static Logger logger = Logger.getLogger();

	private static String dynView = "AVA_baseview";
	private static DynamicView baseView;
	private static File viewPath = new File( "m:/" );
	private static String pvobName = "\\AVA_PVOB";

	private String bootView = "AVA_bootstrapview";

	private String baseName;
	private String vobName;
	private int policies = 0;
	private Stream integrationStream;
	private Baseline initialBaseline;

	private PVob pvob;
	
	private Vob lastCreatedVob;

	public ClearcaseVCS( File location ) {
		super( location );
	}

	public static ClearcaseVCS create( File location, String baseName, int policies, PVob pvob ) throws ElementNotCreatedException {
		ClearcaseVCS cc = new ClearcaseVCS( location );

		cc.baseName = baseName;
		cc.vobName = "\\" + baseName;
		cc.policies = policies;

		cc.pvob = pvob;
		
		cc.initialize();
		
		return cc;
	}

	@Override
	public void initialize() throws ElementNotCreatedException {
		logger.info( "Initializing Clearcase Repository" );
		InitializeImpl init = new InitializeImpl();
		doInitialize( init );
		
		lastCreatedVob = init.getVob();
		initialBaseline = init.getBaseline();
	}

	public class InitializeImpl extends Initialize {

		private int step = 0;
		private Baseline baseline;
		private Vob vob;

		public InitializeImpl() {
		}

		public boolean setup() {
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
			logger.info( "Creating Vob " + ClearcaseVCS.this.vobName );

			try {
				vob = Vob.create( ClearcaseVCS.this.vobName, null, ClearcaseVCS.this.vobName + " Vob" );
			} catch (UCMException e) {
				logger.error( "Error while creating Vob: " + e.getMessage() );
				return false;
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
				File basepath = new File( ClearcaseVCS.viewPath, ClearcaseVCS.dynView );
				logger.debug( "Baseview path: " + basepath.getAbsolutePath() );
				c = Component.create( ClearcaseVCS.this.baseName, pvob, ClearcaseVCS.this.baseName, "Main component", basepath );
			} catch (UCMException e) {
				logger.error( "Error while creating Component: " + e.getMessage() );
				return false;
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
				mainlineproject = Project.create( ClearcaseVCS.this.baseName + "_Mainline", null, pvob, policies, "Mainline project", c );
			} catch (UCMException e) {
				logger.error( "Error while creating Mainline Project: " + e.getMessage() );
				return false;
			}

			logger.info( "Creating Mainline integration stream" );
			try {
				integrationStream = Stream.createIntegration( ClearcaseVCS.this.baseName + "_Mainline_int", mainlineproject, initial );
			} catch (UCMException e) {
				logger.error( "Error while creating Mainline Integration Stream: " + e.getMessage() );
				return false;
			}
			
			logger.info( "Creating integration view" );
			try {
				DynamicView bootstrap_int = DynamicView.create( null, ClearcaseVCS.this.baseName + "_" + bootView, integrationStream );
			} catch (UCMException e) {
				logger.error( "Error while creating Integration view: " + e.getMessage() );
				return false;
			}

			logger.info( "Creating Structure_1_0" );
			try {
				baseline = Baseline.create( ClearcaseVCS.this.baseName + "_Structure_1_0", c, new File( viewPath, ClearcaseVCS.this.baseName + "_" + bootView + "/" + ClearcaseVCS.this.baseName ), false, true );
			} catch (UCMException e) {
				logger.error( "Error while creating Structure Baseline: " + e.getMessage() );
				return false;
			}

			/*
			 * logger.info("Creating development project"); Project
			 * developmentProject; try { developmentProject = Project.create(
			 * "Development", null, pvob, policies, "Development project", c );
			 * } catch (UCMException e) {
			 * logger.error("Error while creating Development Project: " +
			 * e.getMessage()); return false; }
			 * 
			 * logger.info("Creating development integration stream"); try {
			 * Stream developmentIntStream = Stream.createIntegration(
			 * "Development_int", developmentProject, structure ); } catch
			 * (UCMException e) {
			 * logger.error("Error while creating Development Integratiom Stream: "
			 * + e.getMessage()); return false; }
			 */

			return true;
		}

		public boolean cleanup( boolean status ) {
			if( !status ) {
				logger.error( "Error while initializing" );
			}

			return true;
		}
	}

	public PVob getPVob() {
		return pvob;
	}
	
	public Vob getLastCreatedVob() {
		return lastCreatedVob;
	}

	public static PVob bootstrap() throws ElementNotCreatedException {
		return bootstrap( ClearcaseVCS.pvobName, ClearcaseVCS.viewPath );
	}
	
	public Stream getIntegrationStream() {
		return integrationStream;
	}
	
	public Baseline getInitialBaseline() {
		return initialBaseline;
	}

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
		}
		

		/*
		 * if( UCMView.ViewExists( bootstrapView ) ) { try {
		 * logger.info("Removing bootstrap view"); DynamicView dv = new
		 * DynamicView(null,bootstrapView); dv.removeView(); } catch( Exception
		 * e ) { logger.error("Error while removing bootstrap view: " +
		 * e.getMessage()); return false; } }
		 * 
		 * // Create project bootstrap
		 * logger.info("Creating bootstrap project"); Project project; try {
		 * project = Project.create( "Bootstrap", null, pvob,
		 * Project.POLICY_INTERPROJECT_DELIVER, "Bootstrap project", c ); }
		 * catch (UCMException e) {
		 * logger.error("Error while creating Bootstrap Project: " +
		 * e.getMessage()); return false; }
		 * logger.info("Creating integration stream");
		 * 
		 * // Create integration stream Baseline testInitial; try { testInitial
		 * = UCMEntity.getBaseline( componentName + "_INITIAL", pvob, true ); }
		 * catch (UCMException e) {
		 * logger.error("Error while creating initial Baseline: " +
		 * e.getMessage()); return false; } Stream intStream; try { intStream =
		 * Stream.createIntegration( "Bootstrap_int", project, testInitial ); }
		 * catch (UCMException e) {
		 * logger.error("Error while creating Bootstrap Integration Stream: " +
		 * e.getMessage()); return false; }
		 * 
		 * logger.info("Creating integration view"); try { DynamicView
		 * bootstrap_int = DynamicView.create(null, bootstrapView, intStream); }
		 * catch (UCMException e) {
		 * logger.error("Error while creating Integration view: " +
		 * e.getMessage()); return false; }
		 */

		return pvob;
	}
}
