package net.praqma.vcs.test;

import java.io.File;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.vcs.model.clearcase.ClearcaseBranch;

public class CCTest2 {
	
	static Logger logger = Logger.getLogger();

	public static void main( String[] args ) throws UCMException {
		
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		/* Setup the logger */
		net.praqma.util.debug.PraqmaLogger.Logger logger2 = net.praqma.util.debug.PraqmaLogger.getLogger(false);
        logger2.subscribeAll();
        logger2.setLocalLog( new File( "versiontest.log") );
        Cool.setLogger(logger2);		
		
		logger.toStdOut( true );
		
		String append = "001";
		File path = new File( "C:/Temp/views/" + append );
		String vname = "Wolle003";
		String cname = "Core";
		
		Vob vob = new Vob( "\\" + vname );
		PVob pvob = new PVob( "\\" + vname + "_PVOB" );
		Stream parent = UCMEntity.getStream( "Development_int", pvob, true );
		Baseline baseline = UCMEntity.getBaseline( "Structure_1_0", pvob, true );
		

		ClearcaseBranch branch = ClearcaseBranch.create( vob, parent, baseline, path, vname + "_" + append, vname + "_" + append + "@" + pvob );
		

	}

}
