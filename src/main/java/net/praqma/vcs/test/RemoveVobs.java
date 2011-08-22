package net.praqma.vcs.test;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.Region;
import net.praqma.clearcase.Site;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;

public class RemoveVobs {
	
	private static net.praqma.util.debug.Logger logger = net.praqma.util.debug.Logger.getLogger();

	public static void main( String[] args ) throws UCMException {
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		/* Setup the logger */
        Logger logger2 = PraqmaLogger.getLogger(false);
        logger2.subscribeAll();
        logger2.setLocalLog( new File( "rmvobs.log") );
        Cool.setLogger(logger2);
        
        Site site = new Site("site");
        Region region = new Region( "SERVER", site );
        
        List<Vob> vobs = site.getVobs( region );
        
        Iterator<Vob> it = vobs.iterator();
        
        logger.info( "Removing Vobs" );
        while( it.hasNext() ) {
        	Vob vob = it.next();
        	if( !vob.isProjectVob() ) {
        		logger.info( "Removing " + vob.getName() );
        		vob.remove();
        		it.remove();
        	}
        }
        
        Iterator<Vob> itp = vobs.iterator();
        
        logger.info( "Removing PVobs" );
        while( itp.hasNext() ) {
        	Vob vob = itp.next();
        	if( !vob.isProjectVob() ) {
        		logger.info( "Removing " + vob.getName() );
        		vob.remove();
        		itp.remove();
        	}
        }

        logger.info( "Done" );

	}

}
