package net.praqma.vcs;

import java.util.ArrayList;
import java.util.List;

public class AVA {
	
	private static AVA instance;
	
	private boolean verbose = true;
	
	private List<Extension> extensionsList = new ArrayList<Extension>();
	
	public AVA() {
		if( instance != null ) {
			throw new IllegalStateException( "Instance already defined" );
		}
		
		instance = this;
	}
	
	public static AVA getInstance() {
		return instance;
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
