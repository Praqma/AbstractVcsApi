package net.praqma.vcs.util.configuration.implementation;

import java.io.File;

import org.w3c.dom.Element;

import net.praqma.vcs.util.configuration.AbstractConfiguration;
import net.praqma.vcs.util.configuration.AbstractConfigurationReader;
import net.praqma.vcs.util.configuration.exception.ConfigurationException;

public class ClearCaseConfigurationReader extends AbstractConfigurationReader {

	public ClearCaseConfigurationReader() {
		super();
	}
	
	public AbstractConfiguration getTypeConfiguration( Element element ) throws ConfigurationException {
		
		String path = getFirstElement( element, "path" ).getTextContent();
		String viewtag = getFirstElement( element, "viewtag" ).getTextContent();
		String vob = getFirstElement( element, "vob" ).getTextContent();
		String pvob = getFirstElement( element, "pvob" ).getTextContent();
		String fbaseline = getFirstElement( element, "foundationBaseline" ).getTextContent();
		String pstream = getFirstElement( element, "parentStream" ).getTextContent();
		String streamName = getFirstElement( element, "streamName" ).getTextContent();
		
		ClearCaseConfiguration config = new ClearCaseConfiguration( new File( path ), viewtag, vob, pvob, fbaseline, pstream, streamName );
		
		return config;
	}

}
