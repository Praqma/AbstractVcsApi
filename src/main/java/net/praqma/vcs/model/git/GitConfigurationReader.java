package net.praqma.vcs.model.git;

import java.io.File;

import org.w3c.dom.Element;

import net.praqma.vcs.configuration.AbstractConfiguration;
import net.praqma.vcs.configuration.AbstractConfigurationReader;
import net.praqma.vcs.configuration.exception.ConfigurationException;

public class GitConfigurationReader extends AbstractConfigurationReader {

	public GitConfigurationReader() {
		super();
	}
	
	public AbstractConfiguration getTypeConfiguration( Element element ) throws ConfigurationException {
		
		String path = getFirstElement( element, "path" ).getTextContent();
		String branch = getFirstElement( element, "branch" ).getTextContent();
		
		GitConfiguration config = null;
		
		try {
			Element parent = getFirstElement( element, "parent" );
			String location = parent.getTextContent();
			String name = parent.getAttribute( "name" );
			
			config = new GitConfiguration( new File( path ), branch, location, name );
		} catch( Exception e ) {
			/* The parent does not exist */
			config = new GitConfiguration( new File( path ), branch );
		}
				
		return config;
	}

}
