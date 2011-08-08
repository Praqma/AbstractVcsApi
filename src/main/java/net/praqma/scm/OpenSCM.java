package net.praqma.scm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenSCM {
	
	private static OpenSCM instance;
	
	private Map<String, Class<Extension>> extensions = new HashMap<String, Class<Extension>>();
	private List<Class<Extension>> extensionsList = new ArrayList<Class<Extension>>();
	
	public OpenSCM() {
		if( instance != null ) {
			throw new IllegalStateException( "Instance already defined" );
		}
		
		instance = this;
	}
	
	public static OpenSCM getInstance() {
		return instance;
	}
	
	public void registerExtension( String name, Class<?> theClass ) {
		extensions.put( name, (Class<Extension>) theClass );
		extensionsList.add( (Class<Extension>) theClass );
		
		System.out.println("Added " + name);
	}
	
	public <T> List<T> getExtensions(Class<T> extensionType) {
		
		List<T> r = new ArrayList<T>();
		System.out.println("HEARE= " + r);
		for(Class<Extension> e : extensionsList) {
			System.out.println("EXT: " + e + " = " + extensionType);
			//if( e.equals( extensionType ) ) {
			if( extensionType.isAssignableFrom( e )) {
				r.add(  (T) e );
			}
		}
		
		net.praqma.util.structure.Printer.listPrinter( r );
		
		return r;
	}
}
