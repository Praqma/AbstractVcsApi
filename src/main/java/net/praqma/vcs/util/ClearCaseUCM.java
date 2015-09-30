package net.praqma.vcs.util;

import java.io.File;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementException;
import net.praqma.vcs.util.configuration.AbstractConfiguration;
import net.praqma.vcs.util.configuration.exception.ConfigurationException;
import net.praqma.vcs.util.configuration.implementation.ClearCaseConfiguration;

public class ClearCaseUCM {
	
	private static final String fdel = System.getProperty( "file.separator" );
	private static Logger logger = Logger.getLogger();
	
	public static AbstractConfiguration getConfigurationFromView( File view, boolean input ) throws ElementException, ConfigurationException, UCMException {
		SnapshotView snapview = null;
		try {
			snapview = SnapshotView.getSnapshotViewFromPath( view );
		} catch( UCMException e ) {
			throw new ElementDoesNotExistException( e.getMessage() );
		}
		
		Stream stream;
		try {
			stream = snapview.getStream();
		} catch( UCMException e ) {
			throw new ElementException( e.getMessage() );
		}
		
		File parentView = view.getParentFile();
		
		ClearCaseConfiguration config = null;
        
        String foundationBaselineShort = stream.getFoundationBaseline().getShortname();
        String pvob = stream.getPVob().toString();
        String snapViewTag = snapview.getViewtag();
        String streamShort = stream.getShortname();
		
		if( input ) {            
			config = new ClearCaseConfiguration( 
                    new File( parentView, view.getName() + "_in" ).toString(),
                    snapViewTag + "_in", 
                    streamShort + "_in",
                    view.toString(),
                    snapViewTag,
                    streamShort,                    
                    pvob,
                    foundationBaselineShort,
                    null 
            );
            
		} else {
			config = new ClearCaseConfiguration( 
                    view.toString(), 
                    snapViewTag,
                    streamShort, 
                    new File( parentView, view.getName()).toString(),
                    snapViewTag,
                    streamShort,
                    pvob,
                    foundationBaselineShort,
                    null 
            );
		}
		
		return config;
	}
}
