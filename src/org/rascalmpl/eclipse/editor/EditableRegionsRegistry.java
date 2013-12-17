package org.rascalmpl.eclipse.editor;

import java.util.HashMap;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.utils.Pair;
import org.eclipse.jface.text.source.ISourceViewer;

public class EditableRegionsRegistry{

	private static Map<IParseController, LinkedHashMap<String,Pair<Integer, Integer>>> regionsMap =
			new HashMap<IParseController, LinkedHashMap<String,Pair<Integer,Integer>>>();

	private static Map<IParseController, ISourceViewer> viewersMap =
			new HashMap<IParseController, ISourceViewer>();
			
	
	public static LinkedHashMap<String,Pair<Integer, Integer>> getRegistryForDocument(IParseController pc){
		return regionsMap.get(pc);
	}
	
	public static void setRegistryForDocument(IParseController pc, LinkedHashMap<String,Pair<Integer, Integer>> regions){
		regionsMap.put(pc, regions);
	}	
	
	public static boolean hasRegistryForDocument(IParseController pc){
		return regionsMap.containsKey(pc);
	}
	
	public static ISourceViewer getSourceViewerForDocument(IParseController pc){
		return viewersMap.get(pc);
	}
	
	public static void setSourceViewerForDocument(IParseController pc, ISourceViewer viewer){
		viewersMap.put(pc, viewer);
	}
	
	public static boolean hasViewerForDocument(IParseController pc){
		return viewersMap.containsKey(pc);
	}
	
	
}
