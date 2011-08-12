package net.praqma.vcs.test;

import java.io.File;
import java.util.List;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.exceptions.ElementDoesNotExistException;
import net.praqma.exceptions.ElementNotCreatedException;
import net.praqma.exceptions.UnableToReplayException;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.vcs.AVA;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.clearcase.ClearcaseBranch;
import net.praqma.vcs.model.clearcase.ClearcaseReplay;
import net.praqma.vcs.model.clearcase.ClearcaseVCS;
import net.praqma.vcs.model.git.GitBranch;
import net.praqma.vcs.util.Utils;

public class GitToClearcase {
	static Logger logger = Logger.getLogger();
	
	public static void main( String[] args ) throws UCMException, ElementNotCreatedException, ElementDoesNotExistException, UnableToReplayException {
		
		if( args.length < 4 ) {
			System.err.println( "Too few parameters" );
			System.err.println( "Usage: GitToClearcase <vobname> <componentname> <viewroot> <append>" );
			System.exit( 1 );
		}
		
		new AVA();
		logger.setMinLogLevel( LogLevel.INFO );
		
		String vname = args[0];
		String cname = args[1];
		String append = args[3];
		File path = new File( args[2] + append );
		
		Vob vob = new Vob( "\\" + vname );
		PVob pvob = new PVob( "\\" + vname + "_PVOB" );
		Stream parent = UCMEntity.getStream( "Development_int", pvob, true );
		Baseline baseline = UCMEntity.getBaseline( "Structure_1_0", pvob, true );
		String name = vname + "_" + append;
		
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		logger.toStdOut( true );
		
		/*
		ClearcaseVCS cc = ClearcaseVCS.create( null, vname, cname, Project.POLICY_INTERPROJECT_DELIVER  | 
                                                                   Project.POLICY_CHSTREAM_UNRESTRICTED | 
                                                                   Project.POLICY_DELIVER_NCO_DEVSTR, new File( "m:") );
		
		
		ClearcaseBranch ccbranch = new ClearcaseBranch( vob, pvob, parent, baseline, path, name, name );
		ccbranch.get();
		ccbranch.pull();

		ClearcaseReplay cr = new ClearcaseReplay( ccbranch );
		
		GitBranch branch = new GitBranch( new File( "C:/projects/monkit/branches" ), "master" );
		List<AbstractCommit> commits = branch.getCommits(true);
		
		logger.info( commits.size() + " commits on branch " + branch );
		
		logger.info( "Commit #1: " + commits.get( 0 ) );

		for( int i = 0 ; i < commits.size() ; i++ ) {
			System.out.print( "\r" + Utils.getProgress( commits.size(), i ) );
			cr.replay( commits.get( i ) );
		}
		System.out.println(" Done");
		
		*/
		
		/* For jenkins */
		logger.info( "Creating special Jenkins project" );
		Project jenkinsProject = null;
		try {
			jenkinsProject = Project.create( "jenkins", null, pvob, Project.POLICY_INTERPROJECT_DELIVER | Project.POLICY_CHSTREAM_UNRESTRICTED | Project.POLICY_DELIVER_NCO_DEVSTR, "Development project", baseline.getComponent() );
		} catch (UCMException e) {
			logger.error("Error while creating Jenkins Project: " + e.getMessage());
		}
		
		logger.info("Creating Jenkins integration stream");
		try {
			Stream jenkinsIntStream = Stream.createIntegration( "jenkins_int", jenkinsProject, baseline );
		} catch (UCMException e) {
			logger.error("Error while creating Jenkins Integratiom Stream: " + e.getMessage());
		}
	}
}
