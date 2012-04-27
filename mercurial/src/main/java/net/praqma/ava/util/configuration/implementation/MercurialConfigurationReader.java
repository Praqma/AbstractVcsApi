package net.praqma.ava.util.configuration.implementation;

import java.io.File;

import org.w3c.dom.Element;

import net.praqma.vcs.util.configuration.AbstractConfiguration;
import net.praqma.vcs.util.configuration.AbstractConfigurationReader;
import net.praqma.vcs.util.configuration.exception.ConfigurationException;

public class MercurialConfigurationReader extends AbstractConfigurationReader {

	public MercurialConfigurationReader() {
		super();
	}
	
	public AbstractConfiguration getTypeConfiguration( Element element ) throws ConfigurationException {
		
		String path = getFirstElement( element, "path" ).getTextContent();
		String branch = getFirstElement( element, "branch" ).getTextContent();
		
		MercurialConfiguration config = null;
		
		try {
			Element parent = getFirstElement( element, "parent" );
			String location = parent.getTextContent();
			String name = parent.getAttribute( "name" );
			
			config = new MercurialConfiguration( new File( path ), branch, location, name );
		} catch( Exception e ) {
			/* The parent does not exist */
			config = new MercurialConfiguration( new File( path ), branch );
		}
				
		return config;
	}

}
