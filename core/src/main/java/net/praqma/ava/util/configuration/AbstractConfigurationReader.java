package net.praqma.ava.util.configuration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import net.praqma.util.debug.Logger;
import net.praqma.util.xml.XML;
import net.praqma.ava.AVA;
import net.praqma.ava.Extension;
import net.praqma.ava.util.configuration.exception.ConfigurationDoesNotExistException;
import net.praqma.ava.util.configuration.exception.ConfigurationException;

public abstract class AbstractConfigurationReader extends XML {
	
	private static Logger logger = Logger.getLogger();
	
	protected AbstractConfigurationReader() {
		
	}
	
	public AbstractConfigurationReader( File conf ) throws IOException {
		super( conf );
	}
	
	private static Map<String, Class<? extends AbstractConfigurationReader>> readers = new HashMap<String, Class<? extends AbstractConfigurationReader>>();

	
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
		
		try {
			Element extensions = xml.getFirstElement( "extensions" );
			List<Element> exts = xml.getElements( extensions, "extension" );
			for( Element e : exts ) {
				String ext = e.getTextContent();
				try {
					AVA.getInstance().registerExtension( "", (Extension) Class.forName( ext ).newInstance() );
					logger.info( "Adding " + ext + " as an extension" );
				} catch ( Exception e1 ) {
					logger.warning( ext + " could not be added as an extension: " + e1.getMessage() );
				}
			}
		} catch( Exception e ) {
			logger.debug( "No extensions" );
		}
		
		Configuration config = new Configuration();
				
		config.setSourceConfiguration( getConfig( source ) );
		config.setTargetConfiguration( getConfig( target ) );
		
		return config;
	}
}
