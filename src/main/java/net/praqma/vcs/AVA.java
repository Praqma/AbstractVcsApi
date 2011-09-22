package net.praqma.vcs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.praqma.vcs.persistence.PersistenceStrategy;

public class AVA {
	
	private static AVA instance;
	
	private List<Extension> extensionsList = new ArrayList<Extension>();
	
	private PersistenceStrategy persistence;
	
	public AVA( PersistenceStrategy persistence ) {
		if( instance != null ) {
			throw new IllegalStateException( "Instance already defined" );
		}
		
		this.persistence = persistence;
		
		instance = this;
	}
	
	public static AVA getInstance() {
		return instance;
	}
	
	public PersistenceStrategy getPersistenceStrategy() {
		return persistence;
	}
	
	public Date getLastCommitDate() {
		return persistence.getLastCommitDate();
	}
	
	public void setLastCommitDate( Date date ) {
		persistence.setLastCommitDate( date );
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
