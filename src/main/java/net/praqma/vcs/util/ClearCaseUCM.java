package net.praqma.vcs.util;

import java.io.File;

import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementException;
import net.praqma.vcs.util.configuration.AbstractConfiguration;
import net.praqma.vcs.util.configuration.exception.ConfigurationException;
import net.praqma.vcs.util.configuration.implementation.ClearCaseConfiguration;

public class ClearCaseUCM {
	
	private static final String fdel = System.getProperty( "file.separator" );
	
	public static AbstractConfiguration getConfigurationFromView( File view, String vobname ) throws ElementException, ConfigurationException, UCMException {
		if( !vobname.startsWith( fdel ) ) {
			vobname = fdel + vobname;
		}
		Vob vob = new Vob( vobname );
		
		return getConfigurationFromView( view, vob );
	}
	
	public static AbstractConfiguration getConfigurationFromView( File view, Vob vob ) throws ElementException, ConfigurationException, UCMException {
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
		
		ClearCaseConfiguration config = new ClearCaseConfiguration( parentView, snapview.getViewtag(), vob, stream.getPVob(), stream.getFoundationBaseline(), null, stream );
		
		config.setInputPath( view );
		
		return config;
	}
}
