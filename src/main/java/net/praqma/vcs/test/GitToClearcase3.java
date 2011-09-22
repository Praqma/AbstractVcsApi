package net.praqma.vcs.test;

import java.io.File;
import java.util.List;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;
import net.praqma.vcs.AVA;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.AbstractReplay;
import net.praqma.vcs.model.clearcase.ClearcaseBranch;
import net.praqma.vcs.model.clearcase.ClearcaseReplay;
import net.praqma.vcs.model.clearcase.ClearcaseVCS;
import net.praqma.vcs.model.clearcase.listeners.ClearcaseReplayListener;
import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
import net.praqma.vcs.model.exceptions.UnableToReplayException;
import net.praqma.vcs.model.git.GitBranch;
import net.praqma.vcs.util.Utils;

public class GitToClearcase3 {
	static net.praqma.util.debug.Logger logger = net.praqma.util.debug.Logger.getLogger();
	
	public static void main( String[] args ) throws UCMException, ElementNotCreatedException, ElementDoesNotExistException, UnableToReplayException {
		
		if( args.length < 4 ) {
			System.err.println( "Too few parameters" );
			System.err.println( "Usage: GitToClearcase3 <vobname> <componentname> <projectname> <viewroot> <git repo>" );
			System.exit( 1 );
		}
		
		new AVA(null);
		//logger.setMinLogLevel( LogLevel.INFO );
		
		String vname = args[0];
		String cname = args[1];
		String projectName = args[2];
		String streamName = projectName + "_int";
		File path = new File( args[3] );
		File grepo = new File( args[4] );
		
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		/* Setup the logger */
		//logger.setMinLogLevel( LogLevel.INFO );
		
		PVob pvob = ClearcaseVCS.bootstrap();
		//PVob pvob = new PVob( "\\AVA_PVOB" );

		
		ClearcaseVCS cc = new ClearcaseVCS( null, vname, cname, projectName, streamName, 
				                                           Project.POLICY_INTERPROJECT_DELIVER  | 
                                                           Project.POLICY_CHSTREAM_UNRESTRICTED | 
                                                           Project.POLICY_DELIVER_NCO_DEVSTR, pvob );
		
		cc.get(true);
		logger.info( "Clearcase initialized" );
		
		/* Make number 1 stream */
		final ClearcaseBranch ccbranch = new ClearcaseBranch( pvob, cc.getLastCreatedVob(), cc.getIntegrationStream(), cc.getInitialBaseline(), new File( path, projectName + "_1" ), projectName + "_1_view", projectName + "_1_dev" );
		try {
			ccbranch.get(true);
		} catch( Exception e ) {
			System.err.println("Unable to get branch 1: " + e.getMessage());
			System.exit( 1 );
		}
		ccbranch.update();

		/* Make number 2 stream */
		final ClearcaseBranch ccbranch2 = new ClearcaseBranch( pvob, cc.getLastCreatedVob(), cc.getIntegrationStream(), cc.getInitialBaseline(), new File( path, projectName + "_2" ), projectName + "_2_view", projectName + "_2_dev" );
		try {
			ccbranch2.get(true);
		} catch( Exception e ) {
			System.err.println("Unable to get branch 1: " + e.getMessage());
			System.exit( 1 );
		}
		ccbranch2.update();

		ClearcaseReplay cr = new ClearcaseReplay( ccbranch );
		
		//GitBranch branch = new GitBranch( new File( "C:/projects/monkit/branches" ), "master" );
		GitBranch branch = new GitBranch( grepo, "master" );
		List<AbstractCommit> commits = branch.getCommits(true);
		
		logger.info( commits.size() + " commits on branch " + branch );
		
		logger.info( "Commit #1: " + commits.get( 0 ) );
		
		AVA.getInstance().registerExtension( "hej", new ClearcaseReplayListener() {

			@Override
			public void onReplay( ClearcaseReplay replay, AbstractCommit commit ) {
				logger.debug( "Calling listener " + commit.getNumber() );
				if( commit.getNumber() % 2 == 0 ) {
					logger.debug( "Using branch 1" );
					replay.setBranch( ccbranch );
				} else {
					logger.debug( "Using branch 2" );
					replay.setBranch( ccbranch2 );
				}
			}

			@Override
			public String onSelectBaselineName( AbstractCommit commit ) {
				return null;
			}} );

		int ml = 50;
		for( int i = 0 ; i < commits.size() ; i++ ) {
		//for( int i = 0 ; i < 3 ; i++ ) {
			String t = commits.get( i ).getTitle().substring( 0, Math.min(commits.get( i ).getTitle().length(), ml) );
			System.out.print( "\r" + Utils.getProgress( commits.size(), i ) );
			System.out.print( " - " + t + new String( new char[ml - t.length()] ).replace( "\0", " " ) );
			//branch.checkout( commits.get( i ) );
			//branch.update( commits.get( i ) );
			branch.checkoutCommit( commits.get( i ) );
			cr.replay( commits.get( i ) );
		}
		System.out.println(" Done");
		
		/* For jenkins */
		logger.info( "Creating special Jenkins project" );
		Project jenkinsProject = null;
		try {
			jenkinsProject = Project.create( "jenkins", null, pvob, Project.POLICY_INTERPROJECT_DELIVER | Project.POLICY_CHSTREAM_UNRESTRICTED | Project.POLICY_DELIVER_NCO_DEVSTR, "Development project", cc.getInitialBaseline().getComponent() );
		} catch (UCMException e) {
			logger.error("Error while creating Jenkins Project: " + e.getMessage());
		}
		
		logger.info("Creating Jenkins integration stream");
		try {
			Stream jenkinsIntStream = Stream.createIntegration( "jenkins_int", jenkinsProject, cc.getInitialBaseline() );
		} catch (UCMException e) {
			logger.error("Error while creating Jenkins Integratiom Stream: " + e.getMessage());
		}
	}
}
