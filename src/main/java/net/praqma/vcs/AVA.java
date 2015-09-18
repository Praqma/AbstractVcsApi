package net.praqma.vcs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.praqma.vcs.model.AbstractBranch;
import net.praqma.vcs.persistence.PersistenceStrategy;

public class AVA {
	
	private static AVA instance;
	
	private List<Extension> extensionsList = new ArrayList<>();
	
	private PersistenceStrategy persistence;
	
	private AVA( PersistenceStrategy persistence ) {
		this.persistence = persistence;
	}
	
    public static AVA getInstance(PersistenceStrategy persistenceStrategy) {
        if(instance == null) {
            instance = new AVA(persistenceStrategy);
        } else if(persistenceStrategy != null) {
            instance.persistence = persistenceStrategy;
        }
        return instance;
    }
	
	public PersistenceStrategy getPersistenceStrategy() {
		return persistence;
	}
	
	public Date getLastCommitDate( AbstractBranch branch ) {
		return persistence.getLastCommitDate( branch );
	}
	
	public void setLastCommitDate( AbstractBranch branch, Date date ) {
		persistence.setLastCommitDate( branch, date );
		persistence.save();
	}
	
	public void registerExtension( String name, Extension ext ) {
		extensionsList.add( ext );
		
		System.out.println("Added " + name);
	}
	
	public void clearExtension() {
		extensionsList.clear();
	}
	
	public <T> void removeExtensionsByType(Class<T> extensionType) {
		for(Extension e : extensionsList) {
			if( extensionType.isInstance( e )) {
				extensionsList.remove( e );
			}
		}
	}
	
	public <T> List<T> getExtensions(Class<T> extensionType) {
		
		List<T> r = new ArrayList<T>();
		for(Extension e : extensionsList) {

			if( extensionType.isInstance( e )) {
				r.add( (T) e );
			}
		}
		
		return r;
	}
}
