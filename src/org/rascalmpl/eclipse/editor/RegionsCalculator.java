package org.rascalmpl.eclipse.editor;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.rascalmpl.library.util.Maybe;


public class RegionsCalculator {
	
	private static final String PROTECTED = "protected";
	private static final String ORIGINS = "origins";

	public static LinkedHashMap<String, IRegion> calculateRegions(
			IConstructor ast, String text) {
		LinkedHashMap<String, IRegion> result = new LinkedHashMap<String, IRegion>();
		if (ast.asAnnotatable().hasAnnotation(ORIGINS)){
			IList list = (IList) ast.asAnnotatable().getAnnotation(ORIGINS);
			int offset = 0;
			Iterator<IValue> iter = list.iterator();
			while (iter.hasNext()){
				ITuple tuple = (ITuple) iter.next();
				IConstructor maybe = (IConstructor) tuple.get(0);
				ISourceLocation loc = (ISourceLocation) Maybe.Maybe_just_val(maybe);
				String originalText = ((IString) tuple.get(1)).getValue();
				if (loc.hasQuery()){
					String protectedName = getQueryStringParam(loc.getQuery(), PROTECTED);
					if (protectedName != null){
						result.put(protectedName, new Region(offset, originalText.length()));
					}
				}
				offset += originalText.length();
			}
		}
		return result;
	}

	private static String getQueryStringParam(String query, String name) {
		String[] params = query.split("&");
		for (String p:params){
			String[] nameValue = p.split("=");
			if (nameValue.length == 2)
				if (nameValue[0].equals(name))
					return nameValue[1];
		}
		return null;
	}

}
