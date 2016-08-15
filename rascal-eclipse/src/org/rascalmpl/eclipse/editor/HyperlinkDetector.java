/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rascalmpl.value.IMap;
import org.rascalmpl.value.ISet;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.value.IString;
import org.rascalmpl.value.ITuple;
import org.rascalmpl.value.IValue;
import org.rascalmpl.values.uptr.ITree;
import org.rascalmpl.values.uptr.TreeAdapter;

import io.usethesource.impulse.parser.IParseController;
import io.usethesource.impulse.services.ISourceHyperlinkDetector;

public class HyperlinkDetector implements ISourceHyperlinkDetector {

	public IHyperlink[] detectHyperlinks(IRegion region, ITextEditor editor,
			ITextViewer textViewer, IParseController parseController) {
		if (parseController == null) {
			return null;
		}
		
		ITree tree = (ITree) parseController.getCurrentAst();
		
		if (tree != null) {
			return getTreeLinks(tree, region);
		}
		
		return null;
	}
	
	private IHyperlink[] getTreeLinks(ITree tree, IRegion region) {
		IValue xref = tree.asAnnotatable().getAnnotation("hyperlinks");
		if (xref != null) {
			if (xref.getType().isSet() && xref.getType().getElementType().isTuple() 
					&& xref.getType().getElementType().getFieldType(0).isSourceLocation()
					&& xref.getType().getElementType().getFieldType(1).isSourceLocation()
					) {
				
				List<IHyperlink> links = new ArrayList<>();
				
				ISet rel = ((ISet)xref); 
				for (IValue v: rel) {
					ITuple t = ((ITuple)v);
					ISourceLocation loc = (ISourceLocation)t.get(0);
					if (region.getOffset() >= loc.getOffset() && region.getOffset() < loc.getOffset() + loc.getLength()) {
						ISourceLocation to = (ISourceLocation)t.get(1);
						if (xref.getType().getElementType().getArity() == 3 && xref.getType().getElementType().getFieldType(2).isString()) {
							links.add(new SourceLocationHyperlink(loc, to, ((IString)t.get(2)).getValue()));
						}
						else {
							links.add(new SourceLocationHyperlink(loc, to, to.toString()));
						}
					}
				}
				if (links.isEmpty()) {
					return null;
				}
				
				return sortAndFilterHyperlinks(links); //.toArray(new IHyperlink[] {});
			}
		}
		
		
		ITree ref = TreeAdapter.locateAnnotatedTree(tree, "link", region.getOffset());
		
		if (ref != null) {
			IValue link = ref.asAnnotatable().getAnnotation("link");
			
			if (link != null && link.getType().isSourceLocation()) { 
				return new IHyperlink[] { new SourceLocationHyperlink(TreeAdapter.getLocation(ref), (ISourceLocation) link) };
			}
			
			
		}
		ref = TreeAdapter.locateAnnotatedTree(tree, "links", region.getOffset());
		if (ref != null) {
			IValue links = ref.asAnnotatable().getAnnotation("links");
			if (links != null && links.getType().isSet() && links.getType().getElementType().isSourceLocation()) {
				IHyperlink[] a = new IHyperlink[((ISet) links).size()];
				int i = 0;
				for (IValue l : ((ISet) links)) {
					a[i++] = new SourceLocationHyperlink(TreeAdapter.getLocation(ref), (ISourceLocation) l);
				}
				return a;
			}
		}
		
		IValue docLinksMapValue = tree.asAnnotatable().getAnnotation("docLinks");
		ITree subtree = TreeAdapter.locateAnnotatedTree(tree, "loc", region.getOffset());
		if (docLinksMapValue != null && docLinksMapValue.getType().isMap() && subtree != null) {
			ISourceLocation loc = TreeAdapter.getLocation(subtree);
			if (loc != null) {
				IMap docLinksMap = (IMap)docLinksMapValue;
				if (docLinksMap.containsKey(loc)) {
					IValue links = docLinksMap.get(loc);
					if (links != null && links.getType().isSet() && links.getType().getElementType().isSourceLocation()) {
						IHyperlink[] a = new IHyperlink[((ISet) links).size()];
						int i = 0;
						for (IValue l : ((ISet) links)) {
							a[i++] = new SourceLocationHyperlink(loc, (ISourceLocation) l);
						}
						return a;
					}
				}
			}
		}

		return null;
	}
	
	private IHyperlink[] sortAndFilterHyperlinks(List<IHyperlink> hyperlinks) {
		Collections.sort(hyperlinks, new Comparator<IHyperlink>() {
			@Override
			public int compare(IHyperlink o1, IHyperlink o2) {
				// Always show the smallest offset link first, this is the link under the mouse cursor
				return o2.getHyperlinkRegion().getOffset() - o1.getHyperlinkRegion().getOffset();
			}
		});
		
		List<IHyperlink> filteredLinks = new ArrayList<>();
		for (IHyperlink link : hyperlinks) {
			if (filteredLinks.isEmpty() || filteredLinks.get(0).getHyperlinkRegion().equals(link.getHyperlinkRegion())) {
				filteredLinks.add(link);
			}
		}
		
		return filteredLinks.toArray(new IHyperlink[] {});
	}
}
