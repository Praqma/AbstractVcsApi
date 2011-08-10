package net.praqma.vcs.clearcase;

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
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractVCS;
import net.praqma.vcs.model.Repository;

public class ClearcaseVCS extends AbstractVCS {
	private Logger logger = Logger.getLogger();
	
	private String baseVobName;
	private String vobName;
	private String pvobName;
	private String componentName;
	private int policies = 0;
	private File viewPath;
	
	private String dynView = "AVAbaseview";
	private String bootstrapView = "AVAbootstrapview";

	public ClearcaseVCS( File location ) {
		super( location );
	}
	
	public static ClearcaseVCS create( File location, ClearcaseBranch branch, String baseVobName, String componentName, int policies, File viewPath ) {
		ClearcaseVCS cc = new ClearcaseVCS( location );
		
		cc.baseVobName = baseVobName;
		cc.vobName = "\\" + baseVobName;
		cc.pvobName = cc.vobName + "_PVOB";
		cc.componentName = componentName;
		cc.policies = policies;
		cc.viewPath = viewPath;
		
		cc.initialize();
		return cc;
	}
	
	
	@Override
	public void initialize() {
		doInitialize( new InitializeImpl() );
	}
	
	public class InitializeImpl extends Initialize {
		
		int step = 0;
				
		public InitializeImpl() {
		}
		
		public boolean setup() {
	        Vob v = Vob.get( ClearcaseVCS.this.vobName );
	        if( v != null ) {
	        	logger.info("Removing vob");
	        	try {
	        		v.remove();
	        	} catch( UCMException e ) {
	        		logger.error("Error while removing vob: " + e.getMessage());
	        		return false;
	        	}
	        }
	        
	        PVob pv = PVob.get( ClearcaseVCS.this.pvobName );
	        if( pv != null ) {
	        	logger.info("Removing pvob");
	        	try {
	        		pv.remove();
	        	} catch( UCMException e ) {
	        		logger.error("Error while removing pvob: " + e.getMessage());
	        		return false;
	        	}
	        }
	        
			if( UCMView.ViewExists( dynView ) ) {
		        try {
		        	logger.info("Removing baseview");
		        	DynamicView dv = new DynamicView(null,dynView);
		        	dv.removeView();
		        } catch( Exception e ) {
		        	logger.error("Error while removing baseview: " + e.getMessage());
		        	return false;
		        }
			}
			
			return true;
		} 

		public boolean initialize() {
		     /* Create PVob */
	        logger.info("Creating PVob " + ClearcaseVCS.this.pvobName);	
			PVob pvob;
			try {
				pvob = PVob.create(ClearcaseVCS.this.pvobName, null, "PVOB");
			} catch (UCMException e) {
				logger.error("Error while creating PVob: " + e.getMessage());
				return false;
			}
			
			/* Create Vob */
			logger.info("Creating Vob " + ClearcaseVCS.this.vobName);
			Vob vob;
			try {
				vob = Vob.create(ClearcaseVCS.this.vobName, null, "Vob for testing");
			} catch (UCMException e) {
				logger.error("Error while creating Vob: " + e.getMessage());
				return false;
			}
			logger.info("Loading Vob " + vob);
			try {
				vob.load();
			} catch (UCMException e) {
				logger.error("Error while creating PVob: " + e.getMessage());
				return false;
			}
			try {
				vob.mount();
			} catch (UCMException e) {
				logger.error("Error while mounting Vob: " + e.getMessage());
				return false;
			}
			logger.info("Mounted Vob " + vob);

			/* Create baseview */
			DynamicView baseview;
			try {
				baseview = DynamicView.create(null, dynView, null);
			} catch (UCMException e) {
				logger.error("Error while creating baseview: " + e.getMessage());
				return false;
			}
			
			/* Create component */
			Component c = null;
			try {
				File basepath = new File( ClearcaseVCS.this.viewPath, ClearcaseVCS.this.dynView + "/" + ClearcaseVCS.this.baseVobName );
				logger.debug( "Baseview path: " + basepath.getAbsolutePath() );
				c = Component.create(ClearcaseVCS.this.componentName, pvob, ClearcaseVCS.this.componentName, "Main component", basepath );
			} catch (UCMException e) {
				logger.error("Error while creating Component: " + e.getMessage());
				return false;
			}
			logger.debug("Component=" + c);
			
			/* Create project bootstrap */
			logger.info("Creating bootstrap project");
			Project project;
			try {
				project = Project.create( "Bootstrap", null, pvob, Project.POLICY_INTERPROJECT_DELIVER, "Bootstrap project", c );
			} catch (UCMException e) {
				logger.error("Error while creating Bootstrap Project: " + e.getMessage());
				return false;
			}
			logger.info("Creating integration stream");
			
			/* Create integration stream */
			Baseline testInitial;
			try {
				testInitial = UCMEntity.getBaseline( componentName + "_INITIAL", pvob,	true );
			} catch (UCMException e) {
				logger.error("Error while creating initial Baseline: " + e.getMessage());
				return false;
			}
			Stream intStream;
			try {
				intStream = Stream.createIntegration( "Bootstrap_int", project, testInitial );
			} catch (UCMException e) {
				logger.error("Error while creating Bootstrap Integration Stream: " + e.getMessage());
				return false;
			}
			
			/* Baselines */
			
			logger.info("Creating integration view");
			try {
				DynamicView bootstrap_int = DynamicView.create(null, bootstrapView, intStream);
			} catch (UCMException e) {
				logger.error("Error while creating Integration view: " + e.getMessage());
				return false;
			}
			
			logger.info("Creating Structure_1_0");
			Baseline structure;
			try {
				structure = Baseline.create( "Structure_1_0", c, new File(viewPath, bootstrapView), false, true );
			} catch (UCMException e) {
				logger.error("Error while creating Structure Baseline: " + e.getMessage());
				return false;
			}
			
			logger.info("Creating Mainline project");
			Project mainlineproject;
			try {
				mainlineproject = Project.create( "Mainline", null, pvob, policies, "Mainline project", c );
			} catch (UCMException e) {
				logger.error("Error while creating Mainline Project: " + e.getMessage());
				return false;
			}
			
			logger.info("Creating Mainline integration stream");
			try {
				Stream mainlineIntStream = Stream.createIntegration( "Mainline_int", mainlineproject, structure );
			} catch (UCMException e) {
				logger.error("Error while creating Mainline Integration Stream: " + e.getMessage());
				return false;
			}
			
			logger.info("Creating development project");
			Project developmentProject;
			try {
				developmentProject = Project.create( "Development", null, pvob, policies, "Development project", c );
			} catch (UCMException e) {
				logger.error("Error while creating Development Project: " + e.getMessage());
				return false;
			}
			
			logger.info("Creating development integration stream");
			try {
				Stream developmentIntStream = Stream.createIntegration( "Development_int", developmentProject, structure );
			} catch (UCMException e) {
				logger.error("Error while creating Development Integratiom Stream: " + e.getMessage());
				return false;
			}
			
			return true;
		}
		
		public boolean cleanup( boolean status ) {
			if( !status ) {
				return setup();
			}
			
			return true;
		}
	}
}
