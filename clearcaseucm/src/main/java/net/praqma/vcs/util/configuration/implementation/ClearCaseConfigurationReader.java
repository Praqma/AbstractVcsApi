package net.praqma.vcs.util.configuration.implementation;

import java.io.File;

import org.w3c.dom.Element;

import net.praqma.util.debug.Logger;
import net.praqma.vcs.util.configuration.AbstractConfiguration;
import net.praqma.vcs.util.configuration.AbstractConfigurationReader;
import net.praqma.vcs.util.configuration.exception.ConfigurationException;

public class ClearCaseConfigurationReader extends AbstractConfigurationReader {

	private static Logger logger = Logger.getLogger();

	public ClearCaseConfigurationReader() {
		super();
	}

	public AbstractConfiguration getTypeConfiguration( Element element ) throws ConfigurationException {

		String path = "";
		String viewtag = "";
		String pvob = "";
		String fbaseline = "";
		String pstream = "";
		String streamName = "";

		try {
			path = getFirstElement( element, "path" ).getTextContent();
			viewtag = getFirstElement( element, "viewtag" ).getTextContent();
			pvob = getFirstElement( element, "pvob" ).getTextContent();
			fbaseline = getFirstElement( element, "foundationBaseline" ).getTextContent();
			streamName = getFirstElement( element, "streamName" ).getTextContent();
		} catch( Exception e ) {
			throw new ConfigurationException( "Missing element: " + e.getMessage() );
		}

		try {
			pstream = getFirstElement( element, "parentStream" ).getTextContent();
		} catch( Exception e ) {
			/* no parent given */
			logger.debug( "No parent given" );
		}

		ClearCaseConfiguration config = new ClearCaseConfiguration( path, viewtag, pvob, fbaseline, pstream, streamName );

		try {
			String inputPath = getFirstElement( element, "inputpath" ).getTextContent();
			config.setInputPath( new File( inputPath ) );
		} catch( Exception e ) {
			/* no op */
		}

		try {
			String outputPath = getFirstElement( element, "outputpath" ).getTextContent();
			config.setOutputPath( new File( outputPath ) );
		} catch( Exception e ) {
			/* no op */
		}

		config.generate();

		return config;
	}

}
