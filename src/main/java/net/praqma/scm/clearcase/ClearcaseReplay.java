package net.praqma.scm.clearcase;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.exceptions.UnableToReplayException;
import net.praqma.scm.model.AbstractCommit;
import net.praqma.scm.model.AbstractReplay;
import net.praqma.scm.model.ChangeSetElement;

public class ClearcaseReplay extends AbstractReplay {
	
	private PVob pvob;
	private SnapshotView view;
	private Component component;

	public ClearcaseReplay( File path, SnapshotView view, Component component, PVob pvob ) {
		super( path );
		
		this.pvob = pvob;
		this.view = view;
		this.component = component;
	}

	@Override
	public void replay( AbstractCommit commit ) throws UnableToReplayException {
		List<ChangeSetElement> cs = commit.getChangeSet();
		
		try {
			Activity activity = Activity.create( null, pvob, true, "CCReplay: " + commit.getKey(), path );
		} catch (UCMException e1) {
			throw new UnableToReplayException( "ClearCase Activity could not be created: " + e1.getMessage() );
		}
		
		try {
			Version.checkOut( path, path );
		} catch (UCMException e1) {
			throw new UnableToReplayException( "ClearCase could not checkout path: " + e1.getMessage() );
		}
		
		
		for( ChangeSetElement cse : cs ) {
			File file = new File( path, cse.getFile().getPath() );
			System.out.println( "FILE: " + file );
			
			Version version = null;
			if( !file.exists() ) {
				try {
					version = Version.create( file, view );
				} catch (UCMException e1) {
					throw new UnableToReplayException( "ClearCasecould not create version: " + e1.getMessage() );
				}
			} else {
				try {
					version = Version.getUnextendedVersion( file, path );
				} catch (UCMException e1) {
					throw new UnableToReplayException( "ClearCasecould not get version: " + e1.getMessage() );
				}
			}
			
			System.out.println( "Writing" );
				
			PrintStream ps;
			try {
				ps = new PrintStream( new BufferedOutputStream(new FileOutputStream(version.getVersion(), true) ) );
				ps.println( commit.getKey() + " - " + commit.getAuthorDate() );
				ps.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println( "Checking in...." );
		
		
		try {
			Version.checkIn( path, path );
		} catch (UCMException e1) {
			throw new UnableToReplayException( "ClearCase could not checkin path: " + e1.getMessage() );
		}
		
		try {
			Baseline.create( "OpenSCM_baseline_" + commit.getKey(), component, path, true, true );
		} catch (UCMException e1) {
			throw new UnableToReplayException( "ClearCase could not create baseline: " + e1.getMessage() );
		}
	}
}
