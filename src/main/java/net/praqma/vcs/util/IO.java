package net.praqma.vcs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.praqma.util.debug.Logger;

public abstract class IO {
	private static Logger logger = Logger.getLogger();
	
	public static boolean write( File source, File target ) {
		InputStream in = null;
		OutputStream out = null;
		boolean success = true;
		
		logger.debug( "Writing from " + source + " to " + target );
		
		try {
			in = new FileInputStream( source );
			out = new FileOutputStream( target );
			
		    byte[] buf = new byte[1024];
		    int len;
		    while ((len = in.read(buf)) > 0) {
		        out.write(buf, 0, len);
		    }
			
		} catch (FileNotFoundException e) {
			success = false;
			logger.error( "Could not write to file(" + source + "): " + e );
		} catch (IOException e) {
			success = false;
			logger.error( "Could not write to file(" + source + "): " + e );
		} finally {
			try {
				in.close();
				out.close();
			} catch (Exception e) {
				logger.warning( "Could not close files: " + e.getMessage() );
			}
			
		}
		
		return success;
	}
}
