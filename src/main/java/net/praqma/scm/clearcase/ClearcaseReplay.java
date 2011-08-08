package net.praqma.scm.clearcase;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import net.praqma.scm.AbstractCommit;
import net.praqma.scm.AbstractReplay;
import net.praqma.scm.ChangeSetElement;

public class ClearcaseReplay extends AbstractReplay {

	public ClearcaseReplay( File path ) {
		super( path );
	}

	@Override
	public void replay( AbstractCommit commit ) {
		List<ChangeSetElement> cs = commit.getChangeSet();
		
		for( ChangeSetElement cse : cs ) {
			File file = new File( path, cse.getFile().getPath() );
			System.out.println( "FILE: " + file );
			
			if( file.exists() ) {
				/* Checkout + Checkin */
				PrintStream ps;
				try {
					ps = new PrintStream( new BufferedOutputStream(new FileOutputStream(file, true) ) );
					ps.println( commit.getKey() + " " + commit.getAuthorDate() );
					ps.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

			} else {
				/* Create */
				try {
					File path = new File( file.getParent() );
					path.mkdirs();
					file.createNewFile();
					PrintStream ps = new PrintStream( file );
					ps.println( commit.getKey() + " " + commit.getAuthorDate() );
					ps.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
