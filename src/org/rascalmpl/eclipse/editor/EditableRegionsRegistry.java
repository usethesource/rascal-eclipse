package org.rascalmpl.eclipse.editor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.jface.text.IRegion;

public class EditableRegionsRegistry{
			
	private static Map<String, LinkedHashMap<String,IRegion>> regionsMap =
			new HashMap<String, LinkedHashMap<String,IRegion>>();

	
	private static String makeKey(ISourceLocation loc) {
		return loc.getScheme() + loc.getAuthority() + loc.getPath();
	}
	
	public static LinkedHashMap<String,IRegion> getRegistryForDocument(ISourceLocation c){
		return regionsMap.get(makeKey(c));
	}

	public static boolean hasRegistryForDocument(ISourceLocation c){
		return regionsMap.containsKey(makeKey(c));
	}

	public static void setRegistryForDocument(ISourceLocation c,
			LinkedHashMap<String, IRegion> regions) {
		if (regions!=null)
			regionsMap.put(makeKey(c), regions);
	}
	
	public static void removeRegistryForDocument(ISourceLocation loc) {
		regionsMap.remove(makeKey(loc));
	}
	
}
