package org.rascalmpl.eclipse.editor;

import java.util.HashMap;

import org.eclipse.core.resources.IMarker;

public class MessagesToMarkers extends MessagesTo{

	static final HashMap<String,Integer> severities = new HashMap<String,Integer>(){{
	    put("info",   IMarker.SEVERITY_INFO);
	    put("warning", IMarker.SEVERITY_WARNING);
	    put("error",   IMarker.SEVERITY_ERROR);
	}};
	
	public MessagesToMarkers() {
		super(IMarker.SEVERITY_INFO, severities);
	}
}
