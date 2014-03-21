package org.rascalmpl.eclipse.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.ISourceLocation;

public class EditableRegionsRegistry{
			
	private static Map<String, IMap> regionsMap =
			new HashMap<String, IMap>();
	
	private static String makeKey(ISourceLocation loc) {
		return loc.getScheme() + loc.getAuthority() + loc.getPath();
	}
	
	public static IMap getRegistryForDocument(ISourceLocation c){
		return regionsMap.get(makeKey(c));
	}

	public static boolean hasRegistryForDocument(ISourceLocation c){
		return regionsMap.containsKey(makeKey(c));
	}

	public static void setRegistryForDocument(ISourceLocation c,
			IMap regions) {
		if (regions!=null)
			regionsMap.put(makeKey(c), regions);
	}
	
	public static void removeRegistryForDocument(ISourceLocation loc) {
		regionsMap.remove(makeKey(loc));
	}
	
}
