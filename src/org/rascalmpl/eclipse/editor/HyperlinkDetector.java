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

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.services.ISourceHyperlinkDetector;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rascalmpl.values.uptr.TreeAdapter;

public class HyperlinkDetector implements ISourceHyperlinkDetector {

	public IHyperlink[] detectHyperlinks(IRegion region, ITextEditor editor,
			ITextViewer textViewer, IParseController parseController) {
		if (parseController == null) {
			return null;
		}
		
		IConstructor tree = (IConstructor) parseController.getCurrentAst();
		
		if (tree != null) {
			return getTreeLinks(tree, region);
		}
		
		return null;
	}

	private IHyperlink[] getTreeLinks(IConstructor tree, IRegion region) {
		IConstructor ref = TreeAdapter.locateAnnotatedTree(tree, "link", region.getOffset());
		
		if (ref != null) {
			IValue link = ref.getAnnotation("link");
			
			if (link != null && link.getType().isSourceLocationType()) { 
				return new IHyperlink[] { new SourceLocationHyperlink(TreeAdapter.getLocation(ref), (ISourceLocation) link) };
			}
			
			
		}
		
		ref = TreeAdapter.locateAnnotatedTree(tree, "links", region.getOffset());
		if (ref != null) {
			IValue links = ref.getAnnotation("links");
			if (links != null && links.getType().isSetType() && links.getType().getElementType().isSourceLocationType()) {
				IHyperlink[] a = new IHyperlink[((ISet) links).size()];
				int i = 0;
				for (IValue l : ((ISet) links)) {
					a[i++] = new SourceLocationHyperlink(TreeAdapter.getLocation(ref), (ISourceLocation) l);
				}
				return a;
			}
		}
		
		IValue docLinksMapValue = tree.getAnnotation("docLinks");
		IConstructor subtree = TreeAdapter.locateAnnotatedTree(tree, "loc", region.getOffset());
		if (docLinksMapValue != null && docLinksMapValue.getType().isMapType() && subtree != null) {
			ISourceLocation loc = TreeAdapter.getLocation(subtree);
			if (loc != null) {
				IMap docLinksMap = (IMap)docLinksMapValue;
				if (docLinksMap.containsKey(loc)) {
					IValue links = docLinksMap.get(loc);
					if (links != null && links.getType().isSetType() && links.getType().getElementType().isSourceLocationType()) {
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
}
