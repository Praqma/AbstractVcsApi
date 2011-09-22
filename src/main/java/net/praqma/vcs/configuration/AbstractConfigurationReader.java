package net.praqma.vcs.configuration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import net.praqma.util.xml.XML;
import net.praqma.vcs.configuration.exception.ConfigurationDoesNotExistException;
import net.praqma.vcs.configuration.exception.ConfigurationException;
import net.praqma.vcs.model.clearcase.GitConfigurationReader;
import net.praqma.vcs.model.git.ClearCaseConfigurationReader;

public abstract class AbstractConfigurationReader extends XML {
	
	protected AbstractConfigurationReader() {
		
	}
	
	public AbstractConfigurationReader( File conf ) throws IOException {
		super( conf );
	}
	
	private static Map<String, Class<? extends AbstractConfigurationReader>> readers = new HashMap<String, Class<? extends AbstractConfigurationReader>>();
	
	/* Default readers */
	static {
		readers.put( "clearcase", ClearCaseConfigurationReader.class );
		readers.put( "git", GitConfigurationReader.class );
	}
	
	/**
	 * Add/substitute other readers
	 * @param type The type of the reader
	 * @param clazz The Class of the reader
	 */
	public static void addReader( String type, Class<? extends AbstractConfigurationReader> clazz ) {
		readers.put( type, clazz );
	}
	
	public abstract AbstractConfiguration getTypeConfiguration( Element element ) throws ConfigurationException;
	
	private static AbstractConfiguration getConfig( Element element ) throws ConfigurationDoesNotExistException, ConfigurationException {
		String sourceType = element.getAttribute( "type" );
		
		if( !readers.containsKey( sourceType ) ) {
			throw new ConfigurationDoesNotExistException( "The source type " + sourceType + " does not exist" );
		}
		
		AbstractConfigurationReader r = null;
		try {
			r = readers.get( sourceType ).newInstance();
		} catch (Exception e) {
			throw new ConfigurationException( "Could not instantiate a " + sourceType + " reader" );
		}
		
		return r.getTypeConfiguration( element );
	}
	
	public static Configuration getConfiguration( File file ) throws ConfigurationDoesNotExistException, ConfigurationException, IOException {
		XML xml = new XML( file );
		Element source = xml.getFirstElement( "source" );
		Element target = xml.getFirstElement( "target" );
		
		Configuration config = new Configuration();
				
		config.setSourceConfiguration( getConfig( source ) );
		config.setTargetConfiguration( getConfig( target ) );
		
		return config;
	}
}
