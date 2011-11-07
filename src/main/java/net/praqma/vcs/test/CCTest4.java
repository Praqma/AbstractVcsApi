package net.praqma.vcs.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.util.Utilities;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;
import net.praqma.vcs.AVA;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.clearcase.ClearCaseBranch;
import net.praqma.vcs.model.clearcase.ClearCaseReplay;
import net.praqma.vcs.model.clearcase.ClearCaseVCS;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.OperationNotImplementedException;
import net.praqma.vcs.model.exceptions.OperationNotSupportedException;
import net.praqma.vcs.model.exceptions.UnableToPerformException;
import net.praqma.vcs.model.exceptions.UnableToReplayException;
import net.praqma.vcs.model.git.GitBranch;

public class CCTest4 {

	static net.praqma.util.debug.Logger logger = net.praqma.util.debug.Logger.getLogger();
	
	public static void main( String[] args ) throws UnableToPerformException, URISyntaxException, OperationNotSupportedException, MalformedURLException, OperationNotImplementedException, UnableToReplayException, UCMException, ElementDoesNotExistException, ElementNotCreatedException {
		
		new AVA(null);
		
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );

		
		String append = "1014";
		File path = new File( "C:/Temp/views/" + append );
		String name = "Wolle1014";
		
		PVob pvob = ClearCaseVCS.bootstrap();

		ClearCaseVCS cc = ClearCaseVCS.create( path, name, Project.POLICY_INTERPROJECT_DELIVER  | 
                                                                   Project.POLICY_CHSTREAM_UNRESTRICTED | 
                                                                   Project.POLICY_DELIVER_NCO_DEVSTR    |
                                                                   Project.POLICY_DELIVER_REQUIRE_REBASE, pvob );
		
		ClearCaseBranch branch = new ClearCaseBranch( pvob, cc.getIntegrationStream(), cc.getInitialBaseline(), path, name + "_view", name + "_dev");
		branch.get();
                                                                   
	}

}
