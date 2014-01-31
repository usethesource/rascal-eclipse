package org.rascalmpl.eclipse.editor;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IMapWriter;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.rascalmpl.library.util.Maybe;
import org.rascalmpl.values.IRascalValueFactory;


public class RegionsCalculator {
	
	private static final String PROTECTED = "protected";
	private static final String ORIGINS = "origins";
	private static final String REGIONS = "regions";
	

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
	
	public static LinkedHashMap<String, ISourceLocation> getRegions(IConstructor ast){
		if (ast.asAnnotatable().hasAnnotation(REGIONS)){
			IMap map = (IMap) ast.asAnnotatable().getAnnotation(REGIONS);
			return toMap(map);
		}
		else
			return null;
	}
	
	private static LinkedHashMap<String, ISourceLocation> toMap(IMap regions){
		LinkedHashMap<String, ISourceLocation> result = new LinkedHashMap<String, ISourceLocation>();
		
		Iterator<IValue> iter = orderRegions(regions);
		
		while (iter.hasNext()){
			IString regionName = (IString) iter.next();
			ITuple txtpos = (ITuple) regions.get(regionName);
			ISourceLocation loc = (ISourceLocation) txtpos.get(1);
			result.put(regionName.getValue(), loc);
		}
		return result;
	}
	
	private static Iterator<IValue> orderRegions(IMap regions) {
		LinkedList<IValue> unordered = new LinkedList<IValue>();
		Iterator<IValue> iter = regions.iterator();
		while (iter.hasNext()){
			unordered.add(iter.next());
		}
		Collections.sort(unordered, new OffsetComparator(regions));
		return unordered.iterator();
	}

	public static IMap fromMap(IRascalValueFactory values, LinkedHashMap<String, ISourceLocation> regions, String text){
		IMapWriter writer = values.mapWriter();
		for (String name : regions.keySet()){
			ISourceLocation region = regions.get(name);
			writer.put(values.string(name), region);
		}
		return writer.done();
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
	
	private static class OffsetComparator implements Comparator<IValue>{
		private IMap regions;
		OffsetComparator(IMap regions){
			this.regions = regions;
		}
		@Override
		public int compare(IValue v1, IValue v2) {
			return getOffset(v1) - getOffset(v2); 
		}
		
		private int getOffset(IValue regionName){
			ITuple txtpos = (ITuple) regions.get(regionName);
			ISourceLocation loc = (ISourceLocation) txtpos.get(1);
			return loc.getOffset();
		}
		
	}
}
