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
import java.util.List;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.services.ISourceHyperlinkDetector;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rascalmpl.eclipse.terms.TermParseController;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.values.uptr.TreeAdapter;

public class HyperlinkDetector implements ISourceHyperlinkDetector {

	public IHyperlink[] detectHyperlinks(IRegion region, ITextEditor editor,
			ITextViewer textViewer, IParseController parseController) {
		if (parseController == null) {
			return null;
		}
		
		IConstructor tree = (IConstructor) parseController.getCurrentAst();
		
		if (tree != null) {
			URIResolverRegistry reg = null;
			
			if (parseController instanceof ParseController) {
				reg = ((ParseController)parseController).getRegistry();
			}
			else if (parseController instanceof TermParseController) {
				reg = ((TermParseController)parseController).getRegistry();
			}
			
			return getTreeLinks(tree, region, reg);
		}
		
		return null;
	}

	private IHyperlink[] getTreeLinks(IConstructor tree, IRegion region, URIResolverRegistry reg) {
		IValue xref = tree.asAnnotatable().getAnnotation("hyperlinks");
		if (xref != null) {
			if (xref.getType().isSet() && xref.getType().getElementType().isTuple() 
					&& xref.getType().getElementType().getFieldType(0).isSourceLocation()
					&& xref.getType().getElementType().getFieldType(1).isSourceLocation()
					) {
				List<IHyperlink> links = new ArrayList<IHyperlink>();
				ISet rel = ((ISet)xref); 
				for (IValue v: rel) {
					ITuple t = ((ITuple)v);
					ISourceLocation loc = (ISourceLocation)t.get(0);
					if (region.getOffset() >= loc.getOffset() && region.getOffset() < loc.getOffset() + loc.getLength()) {
						ISourceLocation to = (ISourceLocation)t.get(1);
						if (xref.getType().getElementType().getArity() == 3 && xref.getType().getElementType().getFieldType(2).isString()) {
							links.add(new SourceLocationHyperlink(loc, to, ((IString)t.get(2)).getValue(), reg));
						}
						else {
							links.add(new SourceLocationHyperlink(loc, to, to.toString(), reg));
						}
					}
				}
				if (links.isEmpty()) {
					return null;
				}
				return links.toArray(new IHyperlink[] {});
			}
		}
		
		
		IConstructor ref = TreeAdapter.locateAnnotatedTree(tree, "link", region.getOffset());
		
		if (ref != null) {
			IValue link = ref.asAnnotatable().getAnnotation("link");
			
			if (link != null && link.getType().isSourceLocation()) { 
				return new IHyperlink[] { new SourceLocationHyperlink(TreeAdapter.getLocation(ref), (ISourceLocation) link, reg) };
			}
			
			
		}
		ref = TreeAdapter.locateAnnotatedTree(tree, "links", region.getOffset());
		if (ref != null) {
			IValue links = ref.asAnnotatable().getAnnotation("links");
			if (links != null && links.getType().isSet() && links.getType().getElementType().isSourceLocation()) {
				IHyperlink[] a = new IHyperlink[((ISet) links).size()];
				int i = 0;
				for (IValue l : ((ISet) links)) {
					a[i++] = new SourceLocationHyperlink(TreeAdapter.getLocation(ref), (ISourceLocation) l, reg);
				}
				return a;
			}
		}
		
		IValue docLinksMapValue = tree.asAnnotatable().getAnnotation("docLinks");
		IConstructor subtree = TreeAdapter.locateAnnotatedTree(tree, "loc", region.getOffset());
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
							a[i++] = new SourceLocationHyperlink(loc, (ISourceLocation) l, reg);
						}
						return a;
					}
				}
			}
		}

		return null;
	}
}
