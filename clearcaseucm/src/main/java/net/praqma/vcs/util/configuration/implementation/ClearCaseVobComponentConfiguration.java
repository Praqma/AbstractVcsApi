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
import net.praqma.vcs.model.clearcase.ClearCaseBranch;
import net.praqma.vcs.model.clearcase.ClearCaseReplay;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.UnsupportedBranchException;
import net.praqma.vcs.util.configuration.AbstractConfiguration;
import net.praqma.vcs.util.configuration.exception.ConfigurationException;

public class ClearCaseVobComponentConfiguration extends AbstractConfiguration {
	private String viewtag;
	private Vob vob;
	private PVob pvob;

	private Stream parentStream;
	private String streamName;

	private Baseline foundationBaseline;
	
	private File inputPath;
	private File outputPath;
	
	private File developmentPath;
	
	public ClearCaseVobComponentConfiguration( File path, String viewtag, String vobname, String pvobname, String foundationBaselineName, String parentStreamName, String streamName ) throws ConfigurationException {
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
	
	public ClearCaseVobComponentConfiguration( File path, String viewtag, Vob vob, PVob pvob, Baseline baseline, Stream parentStream, Stream stream ) throws ConfigurationException {
		super( path );

		this.pvob = pvob;

		this.vob = vob;

		this.viewtag = viewtag;
		this.foundationBaseline = baseline;
		this.streamName = stream.getFullyQualifiedName();
		this.parentStream = parentStream;
	}	
	
	public static void parse() {
		
	}

	public void setParentStream( String stream ) throws ConfigurationException {
		if( stream != null && stream.length() > 0 ) {
			try {
				parentStream = UCMEntity.getStream( stream, pvob, false );
			} catch (UCMException e) {
				throw new ConfigurationException( "Could not get parent stream: " + e.getMessage() );
			}
		} else {
			/* No parent */
			parentStream = null;
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
	
	public void setInputPath( File path ) {
		this.inputPath = path;
	}
	
	public void setOutputPath( File path ) {
		this.outputPath = path;
	}
	
	public void setDevelopmentPath( File path ) {
		this.developmentPath = path;
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
		if( branch == null ) {
			//ClearcaseBranch branch = new ClearcaseBranch( pvob, vob, parentStream, foundationBaseline, path, viewtag, streamName );
			ClearCaseBranch branch = new ClearCaseBranch( pvob, parentStream, foundationBaseline, path, viewtag, streamName );
			/* Set input path */
			if( inputPath != null ) {
				branch.setInputPath( inputPath );
			}
			
			/* Set output path */
			if( outputPath != null ) {
				branch.setOutputPath( outputPath );
			}
			
			/* Set the development path */
			if( developmentPath != null ) {
				/* TODO Fix when branch vob-component is implemented */
				//branch.setDevelopmentPath( developmentPath );
			}
			
			branch.get(true);
			this.branch = branch;
		}
		return branch;
	}

	@Override
	public AbstractReplay getReplay() throws UnsupportedBranchException, ElementNotCreatedException, ElementDoesNotExistException {
		return new ClearCaseReplay( getBranch() );
	}

}
