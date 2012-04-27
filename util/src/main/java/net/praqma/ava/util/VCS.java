package net.praqma.ava.util;

import java.io.File;

import net.praqma.ava.VersionControlSystems;

public abstract class VCS {
	public static VersionControlSystems determineVCS( File path ) {
		VersionControlSystems vcs = VersionControlSystems.Unknown;
		
		File cc = new File( path, "view.dat" );
		File hg = new File( path, ".hg" );
		File git = new File( path, ".git" );
		/* Check ClearCase */
		if( cc.exists() && cc.isFile() ) {
			return VersionControlSystems.ClearCase;
		/* Check Mercurial */
		} else if( hg.exists() && hg.isDirectory() ) {
			return VersionControlSystems.Mercurial;
		/* Check Git */
		} else if( git.exists() && git.isDirectory() ) {
			return VersionControlSystems.Git;
		}
		
		return vcs;
	}
}
