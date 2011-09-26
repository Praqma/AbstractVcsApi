package net.praqma.vcs.util.configuration.implementation;

import java.io.File;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractReplay;
import net.praqma.vcs.model.clearcase.ClearcaseBranch;
import net.praqma.vcs.model.clearcase.ClearcaseReplay;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.UnsupportedBranchException;
import net.praqma.vcs.util.configuration.AbstractConfiguration;
import net.praqma.vcs.util.configuration.exception.ConfigurationException;

public class ClearCaseConfiguration extends AbstractConfiguration {
	private String viewtag;
	private Vob vob;
	private PVob pvob;

	private Stream parentStream;
	private String streamName;

	private Baseline foundationBaseline;
	
	public ClearCaseConfiguration( File path, String viewtag, String vobname, String pvobname, String foundationBaselineName, String parentStreamName, String streamName ) throws ConfigurationException {
		super( path );

		this.pvob = PVob.get( pvobname );
		if( this.pvob == null ) {
			throw new ConfigurationException( "PVob " + pvobname + " does not exist" );
		}

		this.vob = new Vob( "\\" + vobname );

		this.viewtag = viewtag;
		setFoundationBaseline( foundationBaselineName );
		this.streamName = streamName;
		setParentStream( parentStreamName );
	}
	
	public static void parse() {
		
	}

	public void setParentStream( String stream ) throws ConfigurationException {
		try {
			parentStream = UCMEntity.getStream( stream, pvob, false );
		} catch (UCMException e) {
			throw new ConfigurationException( "Could not get parent stream: " + e.getMessage() );
		}
	}
	
	public void setFoundationBaseline( String baseline ) throws ConfigurationException {
		try {
			foundationBaseline = UCMEntity.getBaseline( baseline, pvob, false );
		} catch (UCMException e) {
			throw new ConfigurationException( "Could not get foundation baseline: " + e.getMessage() );
		}
	}

	public Stream getParentStream() {
		return parentStream;
	}

	public void setStreamName( String streamName ) {
		this.streamName = streamName;
	}

	public String getStreamName() {
		return streamName;
	}

	public Baseline getFoundationBaseline() {
		return foundationBaseline;
	}

	public Vob getVob() {
		return vob;
	}

	public PVob getPVob() {
		return pvob;
	}

	public String getViewtag() {
		return viewtag;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append( "Parent: " + super.toString() );
		
		sb.append( "View tag: " + viewtag + "\n" );
		sb.append( "Vob: " + vob + "\n" );
		sb.append( "PVob: " + pvob + "\n" );
		
		sb.append( "Stream name: " + streamName + "\n" );
		sb.append( "Parent stream: " + parentStream + "\n" );
		
		return sb.toString();
	}

	@Override
	public AbstractBranch getBranch() throws ElementNotCreatedException, ElementDoesNotExistException {
		ClearcaseBranch branch = new ClearcaseBranch( pvob, vob, parentStream, foundationBaseline, path, viewtag, streamName );
		branch.get(true);
		this.branch = branch;
		return branch;
	}

	@Override
	public AbstractReplay getReplay() throws UnsupportedBranchException {
		return new ClearcaseReplay( branch );
	}

}
