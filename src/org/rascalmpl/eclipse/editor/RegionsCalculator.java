package org.rascalmpl.eclipse.editor;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IInteger;
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

	@Deprecated
	public static LinkedHashMap<String, IRegion> calculateRegions(
			IConstructor ast) {
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
	
	public static LinkedHashMap<String, IRegion> getRegions(IConstructor ast){
		if (ast.asAnnotatable().hasAnnotation(ORIGINS)){
			IList list = (IList) ast.asAnnotatable().getAnnotation(ORIGINS);
			return toMap(list);
		}
		else
			return null;
	}
	
	private static LinkedHashMap<String, IRegion> toMap(IList regions){
		LinkedHashMap<String, IRegion> result = new LinkedHashMap<String, IRegion>();
		Iterator<IValue> iter = regions.iterator();
		while (iter.hasNext()){
			ITuple tuple = (ITuple) iter.next();
			int start = ((IInteger) tuple.get(0)).intValue();
			int length = ((IInteger) tuple.get(1)).intValue();
			String name = ((IString) tuple.get(2)).getValue();
			result.put(name, new Region(start, length));
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
