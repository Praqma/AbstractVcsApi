package net.praqma.vcs.util;

import java.io.File;

import net.praqma.clearcase.exceptions.ClearCaseException;
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

	public static AbstractConfiguration getConfigurationFromView( File view, boolean input ) throws ElementException, ConfigurationException, ClearCaseException {
		SnapshotView snapview = null;
		try {
			snapview = SnapshotView.getSnapshotViewFromPath( view );
		} catch( Exception e ) {
			throw new ElementDoesNotExistException( e.getMessage() );
		}

		Stream stream;
		try {
			stream = snapview.getStream().load();
		} catch( Exception e ) {
			throw new ElementException( e.getMessage() );
		}

		File parentView = view.getParentFile();

		ClearCaseConfiguration config = null;

		if( input ) {
			logger.debug( "This is an input" );
			config = new ClearCaseConfiguration( view.toString(), snapview.getViewtag(), stream.getShortname(), new File( parentView, view.getName() + "_out" ).toString(), snapview.getViewtag() + "_out", stream.getShortname() + "_out", stream.getPVob().toString(), stream.getFoundationBaseline().getShortname(), null );
		} else {
			logger.debug( "This is an output" );
			config = new ClearCaseConfiguration( new File( parentView, view.getName() + "_in" ).toString(), snapview.getViewtag() + "_in", stream.getShortname() + "_in", view.toString(), snapview.getViewtag(), stream.getShortname(), stream.getPVob().toString(), stream.getFoundationBaseline().getShortname(), null );
		}

		return config;
	}
}
