package net.praqma.vcs.persistence;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Element;

import net.praqma.util.debug.Logger;
import net.praqma.util.xml.XML;

public class XMLStrategy extends XML implements PersistenceStrategy {
	
	private static Logger logger = Logger.getLogger();
	
	private SimpleDateFormat format  = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	private File file;
	
	public XMLStrategy( File file ) throws IOException {
		super( file, "ava" );
		this.file = file;
	}

	@Override
	public void setLastCommitDate( Date date ) {
		Element last = null;
		try {
			last = getFirstElement( "last" );
		} catch( Exception e ) {
			/* The field did not exist, let's create it */
			last = addElement( "last" );
		}
		
		last.setTextContent( format.format( date ) );
	}

	@Override
	public Date getLastCommitDate() {
		try {
			Element last = getFirstElement( "last" );
			String d = last.getTextContent();
			return format.parse( d );
		} catch( Exception e ) {
			/* The field did not exist, return null */
			return null;
		}
	}

	@Override
	public void save() {
		saveState( file );
	}

}
