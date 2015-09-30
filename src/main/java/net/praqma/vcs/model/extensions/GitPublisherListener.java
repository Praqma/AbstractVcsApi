/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.vcs.model.extensions;

import java.io.File;
import net.praqma.util.debug.Logger;
import net.praqma.vcs.model.AbstractCommit;
import net.praqma.vcs.model.AbstractReplay;
import net.praqma.vcs.model.git.api.Git;
import net.praqma.vcs.model.git.exceptions.GitException;

/**
 *
 * @author Mads
 */
public class GitPublisherListener extends ReplayListener {
    
    private static final Logger log = Logger.getLogger();
    
    public final String location,branch;
    public final File context;
    
    public GitPublisherListener(String location, String branch, File context) {
        this.branch = branch;
        this.location = location;
        this.context = context;
    } 

    @Override
    public void onPostReplay(AbstractReplay replay, AbstractCommit commit, boolean status) {
        
    }

    @Override
    public void onCommitCreated(AbstractReplay replay, AbstractCommit commit) {
        try {
            log.debug( String.format( "Push commit:%n%s", commit) );
            Git.push(location, branch, context);
        } catch (GitException ex) {
            log.fatal("Failed to push commits to repo: "+ex.getMessage());
        }
 
    }
  
}
