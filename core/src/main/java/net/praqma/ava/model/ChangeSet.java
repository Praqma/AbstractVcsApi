package net.praqma.ava.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ChangeSet extends HashMap<String, ChangeSetElement> {

	private static final long serialVersionUID = -8523355971875285682L;

	public void addChanged( ChangeSetElement e ) {
		put( e.getFile().toString(), e );
	}
	
	public List<ChangeSetElement> asList() {
		List<ChangeSetElement> elements = new ArrayList<ChangeSetElement>();
        Iterator<Entry<String, ChangeSetElement>> it = entrySet().iterator();
        while( it.hasNext() ) {
            Map.Entry<String, ChangeSetElement> entry = (Map.Entry<String, ChangeSetElement>) it.next();
            elements.add( entry.getValue() );
        }
        
        return elements;
	}
	

}
