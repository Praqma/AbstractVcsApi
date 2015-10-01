package net.praqma.vcs.util.configuration.implementation;

import java.io.File;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.model.AbstractReplay;
import net.praqma.vcs.model.clearcase.ClearCaseBranch;
import net.praqma.vcs.model.clearcase.ClearCaseBranchPart;
import net.praqma.vcs.model.clearcase.ClearCaseReplay;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.UnsupportedBranchException;
import net.praqma.vcs.util.configuration.AbstractConfiguration;
import net.praqma.vcs.util.configuration.exception.ConfigurationException;

public class ClearCaseConfiguration extends AbstractConfiguration {

	private static final long serialVersionUID = -7104484491917047522L;
	private static Logger logger = Logger.getLogger();

	private String viewtagIn;
	private String viewtagOut;
	private String streamNameIn;
	private String streamNameOut;
	private String pathNameOut;
	private String pvobName;
	private String foundationBaselineName;
	private String parentStreamName;

	private boolean dontCare = false;
    
	transient private PVob pvob;
	transient private Stream parentStream;
	transient private Baseline foundationBaseline;
	transient private File inputPath;
	transient private File outputPath;

	public ClearCaseConfiguration( String pathName, String viewtag, String pvobName, String foundationBaselineName, String parentStreamName, String streamName ) throws ConfigurationException {
		super( pathName );
		this.pvobName = pvobName;
		this.viewtagIn = viewtag;
		this.foundationBaselineName = foundationBaselineName;
		this.streamNameIn = streamName;
		this.parentStreamName = parentStreamName;
	}

	public ClearCaseConfiguration( String pathNameIn, String viewtagIn, String streamNameIn, String pathNameOut, String viewtagOut, String streamNameOut, String pvobName, String foundationBaselineName, String parentStreamName ) throws ConfigurationException {
		super( pathNameIn );
		this.pathNameOut = pathNameOut;
		this.viewtagIn = viewtagIn;
		this.viewtagOut = viewtagOut;
		this.streamNameOut = streamNameOut;
		this.streamNameIn = streamNameIn;
		this.pvobName = pvobName;
		this.foundationBaselineName = foundationBaselineName;
		this.parentStreamName = parentStreamName;
	}

	public ClearCaseConfiguration( File path, String viewtag, PVob pvob, Baseline baseline, Stream parentStream, Stream stream ) throws ConfigurationException {
		super( path );
		this.pvob = pvob;
		this.viewtagIn = viewtag;
		this.foundationBaseline = baseline;
		this.streamNameIn = stream.getShortname();
		this.parentStream = parentStream;
	}

	@Override
	public void generate() throws ConfigurationException {
		super.generate();

		try {
			logger.debug( "Creating pvob " + pvobName );

			/* Generate the pvob */
			this.pvob = PVob.get( pvobName );
			if( this.pvob == null ) {
				throw new ConfigurationException( "PVob " + pvobName + " does not exist" );
			}

			logger.debug( "Setting foundation baseline " + foundationBaselineName );

			/* Foundation baseline */
			setFoundationBaseline( foundationBaselineName );

			logger.debug( "Setting parent stream " + parentStreamName );

			/* Parent stream */
			setParentStream( parentStreamName );
		} catch( NullPointerException e ) {
			logger.debug( "This must be a null part" );
		}
	}

	public void setParentStream( String stream ) throws ConfigurationException {
		if(stream != null && stream.length() > 0 ) {
			try {
				parentStream = Stream.get( stream, pvob );
			} catch( UnableToInitializeEntityException e ) {
				throw new ConfigurationException( "Could not get parent stream: " + e.getMessage() );
			}
		} else {
			/* No parent */
			parentStream = null;
		}
	}

	public void setParentStream( Stream parent ) {
		this.parentStream = parent;
	}

	public void setFoundationBaseline( String baseline ) throws ConfigurationException {
		try {
			foundationBaseline = Baseline.get( baseline, pvob );
		} catch( UnableToInitializeEntityException e ) {
			/* Not that important */
		}
	}

	public Stream getParentStream() {
		return parentStream;
	}

	public void setStreamName( String streamName ) {
		this.streamNameIn = streamName;
	}

	public String getStreamName() {
		return streamNameIn;
	}

	public void setOutputStreamName( String name ) {
		this.streamNameOut = name;
	}

	public Baseline getFoundationBaseline() {
		return foundationBaseline;
	}

	public PVob getPVob() {
		return pvob;
	}

	public String getViewtag() {
		return viewtagIn;
	}

	public void setInputPath( File path ) {
		this.inputPath = path;
	}

	public void setOutputPath( File path ) {
		this.outputPath = path;
	}

	public void iDontCare() {
		this.dontCare = true;
	}

    @Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append( "ClearCase UCM configuration:\n-------------------\n" );
		sb.append( "PVob         : " + pvob + "\n" );
		sb.append( "Parent stream: " + parentStream + "\n" );
		sb.append( "Baseline     : " + foundationBaseline + "\n" );
		sb.append( "--- INPUT ---\n" );
		sb.append( "View         : " + pathName + "\n" );
		sb.append( "View tag     : " + viewtagIn + "\n" );
		sb.append( "Stream name  : " + streamNameIn + "\n" );
		sb.append( "--- OUTPUT ---\n" );
		sb.append( "View         : " + pathNameOut + "\n" );
		sb.append( "View tag     : " + viewtagOut + "\n" );
		sb.append( "Stream name  : " + streamNameOut + "\n" );

		return sb.toString();
	}

	@Override
	public AbstractBranch getBranch() throws ElementNotCreatedException, ElementDoesNotExistException {
		if( branch == null ) {
			ClearCaseBranch branch = null;
			if( streamNameOut == null || viewtagOut == null || pathNameOut == null ) {
				branch = new ClearCaseBranch( pvob, parentStream, foundationBaseline, path, viewtagIn, streamNameIn );
			} else {
				ClearCaseBranchPart input = null;
				try {
					input = new ClearCaseBranchPart( pvob, parentStream, foundationBaseline, path, viewtagIn, streamNameIn );
				} catch( Exception e ) {
					logger.debug( "Input part is null" );
					/* No op */
				}

				ClearCaseBranchPart output = null;
				try {
					output = new ClearCaseBranchPart( pvob, parentStream, foundationBaseline, new File( pathNameOut ), viewtagOut, streamNameOut );
				} catch( Exception e ) {
					logger.debug( "Output part is null" );
					/* No op */
				}
				// branch = new ClearcaseBranch( pvob, parentStream, null,
				// foundationBaseline, path, new File( pathNameOut ), viewtagIn,
				// viewtagOut, streamNameIn, streamNameOut );

				logger.debug( "I've created the parts" );
				try {
					branch = new ClearCaseBranch( input, output );
				} catch( NullPointerException e ) {
					logger.debug( "Ok, some of the fields in the input was null, let's try the output stream" );
					branch = new ClearCaseBranch( output, false );
				}
				logger.debug( "I've created the branch" );
			}
			/* Set input path */
			if( inputPath != null ) {
				branch.setInputPath( inputPath );
			}

			/* Set output path */
			if( outputPath != null ) {
				branch.setOutputPath( outputPath );
			}

			if( dontCare ) {
				branch.iDontCare();
			}

			logger.debug( "Getting branch" );
			branch.get( true );
			logger.debug( "WHOOP!" );
			this.branch = branch;
		}

		return branch;
	}

	@Override
	public AbstractReplay getReplay() throws UnsupportedBranchException, ElementNotCreatedException, ElementDoesNotExistException {
		return new ClearCaseReplay( getBranch() );
	}

}
