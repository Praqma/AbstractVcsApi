package net.praqma.vcs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
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
	
	private static FilenameFilter filter = new FilenameFilter() {
	    public boolean accept(File dir, String name) {
	        return !name.matches( "^\\.{1,2}$" );
	    }
	};
	
	/**
	 * Returns true if the path is kept
	 * @param path
	 * @return
	 */
	public static boolean removeEmptyFolders( File path ) {
		File[] files = path.listFiles( filter );
		
		/* Remove */
		if( files.length == 0 ) {
			logger.debug( path + " is empty - removed" );
			path.delete();
			return false;
		}
		
		boolean delete = true;
		
		for( File file : files ) {
			if( file.isDirectory() ) {
				/* If the path is kept, the directory is not empty */
				if( removeEmptyFolders( file ) ) {
					delete = false;
				}
			} else {
				return true;
			}
		}
		
		/* Remove */
		if( delete ) {
			path.delete();
			logger.debug( path + " is removed" );
			return false;
		}
		
		return true;
	}
}
