package net.praqma.ava.persistence;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Element;

import net.praqma.util.debug.Logger;
import net.praqma.util.xml.XML;
import net.praqma.ava.model.AbstractBranch;

public class XMLStrategy extends XML implements PersistenceStrategy {
	
	private static Logger logger = Logger.getLogger();
	
	private SimpleDateFormat format  = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	private File file;
	
	public XMLStrategy( File file ) throws IOException {
		super( file, "ava" );
		this.file = file;
	}
	
	public Element getBranch( AbstractBranch branch ) {
		Element b = null;
		List<Element> branches = this.getElementsWithAttribute( getRoot(), "branch", "path", branch.getPath().toString() );
		if( branches.size() < 1 ) {
			b = addElement( "branch" );
			b.setAttribute( "path", branch.getPath().toString() );
			b.setAttribute( "name", branch.getName() );
		} else {
			b = branches.get( 0 );
		}
		
		return b;
	}

	@Override
	public void setLastCommitDate( AbstractBranch branch, Date date ) {
		Element b = getBranch( branch );
		Element last = null;
		try {
			last = getFirstElement( b, "last" );
		} catch( Exception e ) {
			/* The field did not exist, let's create it */
			last = addElement( b, "last" );
		}
		
		last.setTextContent( format.format( date ) );
	}

	@Override
	public Date getLastCommitDate( AbstractBranch branch ) {
		try {
			Element b = getBranch( branch );
			Element last = getFirstElement( b, "last" );
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
