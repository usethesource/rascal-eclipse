package org.rascalmpl.eclipse.editor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.jface.text.IRegion;

public class EditableRegionsRegistry{
			
	private static Map<IConstructor, LinkedHashMap<String,IRegion>> regionsMap =
			new HashMap<IConstructor, LinkedHashMap<String,IRegion>>();

	
	public static LinkedHashMap<String,IRegion> getRegistryForDocument(IConstructor c){
		return regionsMap.get(c);
	}

	public static boolean hasRegistryForDocument(IConstructor c){
		return regionsMap.containsKey(c);
	}

	public static void setRegistryForDocument(IConstructor c,
			LinkedHashMap<String, IRegion> regions) {
		if (regions!=null)
			regionsMap.put(c, regions);
	}
	
	public static void removeRegistryForDocument(IConstructor currentAst) {
		regionsMap.remove(currentAst);
	}
	
}
