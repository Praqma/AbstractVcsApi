package net.praqma.ava.model.subversion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.util.debug.Logger;
import net.praqma.ava.model.AbstractBranch;
import net.praqma.ava.model.AbstractCommit;
import net.praqma.ava.model.subversion.api.Subversion;
import net.praqma.ava.model.subversion.exceptions.SubversionException;

public class SubversionCommit extends AbstractCommit {
	
	private static Logger logger = Logger.getLogger();
	
	private static final SimpleDateFormat isodate  = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss Z" );
	private static Pattern rx_date = Pattern.compile( "^\\s*(.*?)\\s*\\(.*?$" );

	public SubversionCommit( String key, AbstractBranch branch, int number ) {
		super( key, branch, number );
		// TODO Auto-generated constructor stub
	}

	@Override
	public void load() {
		List<String> out = null;
		try {
			out = Subversion.getRevision( number, branch.getPath() );
		} catch( SubversionException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		String[] info = out.get( 1 ).split( "\\|" );
		String user = info[1].trim();
		
		this.author = user;
		this.committer = user;
		this.title = info[0].trim();
		
		Matcher m = rx_date.matcher( info[2].trim() );
		
		if( m.find() ) {
			try {
				this.authorDate = isodate.parse( m.group( 1 ) );
				this.committerDate = this.authorDate;
			} catch( ParseException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		logger.debug( "USER: " + user + ", " + this.authorDate );
		
	}

}
