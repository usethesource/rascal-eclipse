/*******************************************************************************
 * Copyright (c) 2009-2023 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Jurgen J. Vinju - CWI  
 *******************************************************************************/
package org.rascalmpl.eclipse.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.rascalmpl.values.parsetrees.ITree;
import org.rascalmpl.values.parsetrees.TreeAdapter;

import io.usethesource.impulse.parser.IMessageHandler;
import io.usethesource.impulse.parser.IModelListener;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.type.Type;

public class MessagesTo {
	private static int MAX_MESSAGE_LENGTH = 2000;
	
	MessagesTo(int defaultSeverity,Map<String,Integer> severityMap){
		this.severityMap = severityMap;
		this.defaultSeverity = defaultSeverity;
	}
	
	private Map<String,Integer> severityMap;
	private int defaultSeverity;
	

	
	public void process(final ITree parseTree, IMessageHandler handler) {
		if (parseTree != null) {
			processMarkers(parseTree, handler); 
		}
	}

	private void processMarkers(ITree tree, IMessageHandler handler) {
		if (TreeAdapter.isAppl(tree) && !TreeAdapter.isLexical(tree)) {
			IValue anno = tree.asWithKeywordParameters().getParameter("message");
			if (anno != null && anno.getType().isAbstractData() && anno.getType().getName().equals("Message")) {
				IConstructor message = (IConstructor) anno;
				ISourceLocation loc = TreeAdapter.getLocation(tree);
				processMessage(message, loc, handler);
			}
			
			anno = tree.asWithKeywordParameters().getParameter("messages");
			
			if (anno != null && anno.getType().isSet()) {
				process((ISourceLocation) tree.asWithKeywordParameters().getParameter("src"),  (ISet) anno, handler);
				return; // we do not recurse if we found messages (for efficiency)
			}
			
			for (IValue child : TreeAdapter.getArgs(tree)) {
				processMarkers((ITree) child, handler);
			}
		}
		else if (TreeAdapter.isAmb(tree)) {
			for (IValue alt : TreeAdapter.getAlternatives(tree)) {
				processMarkers((ITree) alt, handler);
			}
		}
	}

	public void process(ISourceLocation treeLoc, ISet set, IMessageHandler handler) {
		Type elemType = set.getType().getElementType();

		if (elemType.isAbstractData() && elemType.getName().equals("Message")) {

			for (IValue messagev : ((ISet) set)) {
				IConstructor message = (IConstructor)messagev;
				ISourceLocation loc = (ISourceLocation) message.get(1);
				
				if (loc.getPath().equals(treeLoc.getPath()))
					processMessage(message, loc, handler);
			}
		}
	}
	
	private void processMessage(IConstructor marker, ISourceLocation loc, IMessageHandler handler)  {
		int severity;
		if(severityMap.containsKey(marker.getName())){
			severity = severityMap.get(marker.getName());
		} else {
			severity = defaultSeverity;
		}

		String msg = ((IString) marker.get(0)).getValue();
		if(msg.length() >= MAX_MESSAGE_LENGTH) {
			msg = msg.substring(0, MAX_MESSAGE_LENGTH);
		}
		Map<String,Object> attrs = new HashMap<String,Object>();
		attrs.put(IMarker.SEVERITY, severity);
		attrs.put(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
		if(loc.hasOffsetLength() && loc.hasLineColumn()) {
			handler.handleSimpleMessage(msg, loc.getOffset(), loc.getOffset() + loc.getLength(), loc.getBeginColumn(), loc.getEndColumn(), loc.getBeginLine(), loc.getEndLine(), attrs);
		} else {
			handler.handleSimpleMessage(msg, 0, 0, 0, 0, 0, 0, attrs);
		}
	}
	
	public int compareTo(IModelListener o) {
		return 0;
	}
}
