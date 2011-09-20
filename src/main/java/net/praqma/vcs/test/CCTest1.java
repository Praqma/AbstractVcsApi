package net.praqma.vcs.test;

import java.io.File;

import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.clearcase.ClearcaseVCS;

public class CCTest1 {
	
	static Logger logger = Logger.getLogger();

	public static void main( String[] args ) {
		
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		
		String append = "001";
		File path = new File( "C:/Temp/views/" + append );
		String vname = "Wolle003";
		String cname = "Core";
		
		//File bpath = new File( path, vname + "/" + cname );
		
		//ClearcaseBranch branch = new ClearcaseBranch( path, "master" );
		/*
		ClearcaseVCS cc = ClearcaseVCS.create( null, vname, cname, Project.POLICY_INTERPROJECT_DELIVER  | 
                                                                   Project.POLICY_CHSTREAM_UNRESTRICTED | 
                                                                   Project.POLICY_DELIVER_NCO_DEVSTR    |
                                                                   Project.POLICY_DELIVER_REQUIRE_REBASE, new File( "m:") );
                                                                   */
		

	}

}
