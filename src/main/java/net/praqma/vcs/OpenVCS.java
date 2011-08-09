package net.praqma.vcs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenVCS {
	
	private static OpenVCS instance;
	
	private List<Extension> extensionsList = new ArrayList<Extension>();
	
	public OpenVCS() {
		if( instance != null ) {
			throw new IllegalStateException( "Instance already defined" );
		}
		
		instance = this;
	}
	
	public static OpenVCS getInstance() {
		return instance;
	}
	
	public void registerExtension( String name, Extension ext ) {
		extensionsList.add( ext );
		
		System.out.println("Added " + name);
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
