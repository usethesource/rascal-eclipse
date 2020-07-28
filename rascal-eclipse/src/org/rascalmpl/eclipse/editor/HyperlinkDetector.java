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
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.preferences.RascalPreferences;
import org.rascalmpl.eclipse.terms.TermParseController;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.values.uptr.ITree;
import org.rascalmpl.values.uptr.TreeAdapter;

import io.usethesource.impulse.parser.IParseController;
import io.usethesource.impulse.services.ISourceHyperlinkDetector;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IMap;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.ITuple;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IWithKeywordParameters;
import io.usethesource.vallang.type.Type;
import io.usethesource.vallang.type.TypeFactory;

public class HyperlinkDetector implements ISourceHyperlinkDetector {
    private static final TypeFactory tf = TypeFactory.getInstance();
    private static final Type linksRelType1 = tf.relType(tf.sourceLocationType(), tf.sourceLocationType());
    private static final Type linksRelType2 = tf.relType(tf.sourceLocationType(), tf.sourceLocationType(), tf.stringType());
    private static final RascalLanguageServices imp = RascalLanguageServices.getInstance();
    
    private static ISourceLocation getLocation(ITree t, IParseController controller) {
    	if (t.mayHaveKeywordParameters()) {
    		IWithKeywordParameters<? extends IConstructor> annotatedTree = t.asWithKeywordParameters();
			if (annotatedTree.hasParameter("src")) {
    			return (ISourceLocation)(annotatedTree.getParameter("src"));
    		}
    	}
    	if (controller instanceof TermParseController) {
    		return ((TermParseController)controller).getSourceLocation();
    	}
    	if (controller instanceof ParseController) {
    		return ((ParseController)controller).getSourceLocation();
    	}
    	throw new RuntimeException("Cannot find SourceLocation for " + controller);
    	
    }
    
	public IHyperlink[] detectHyperlinks(IRegion region, ITextEditor editor, ITextViewer textViewer, IParseController parseController) {
		if (parseController == null) {
			return null;
		}
		
		ITree tree = (ITree) parseController.getCurrentAst();
		if (tree == null) {
			return null;
		}

		ISourceLocation rootLocation = getLocation(tree, parseController).top();
		if (tree != null && parseController instanceof TermParseController) {
		    // DSL case
			return getTreeLinks(rootLocation,  tree, region);
		}
		
		if (tree != null && parseController instanceof ParseController && RascalPreferences.isRascalCompilerEnabled()) {
		    ParseController rascalPc = (ParseController) parseController;
		    
		    PathConfig pcfg = RascalLanguageServices.getInstance().getModulePathConfig(rascalPc.getSourceLocation());

		    ISet useDef = imp.getUseDef(rascalPc.getSourceLocation(), pcfg, rascalPc.getModuleName());

		    if (!checkUseDefType(useDef)) {
		        Activator.log(useDef.getType() + " is not a rel[loc,loc] or rel[loc,loc,str]? " + useDef, null);
		        return new IHyperlink[0];
		    }

		    return getLinksForRegionFromUseDefRelation(rootLocation, region, useDef);
		}
		
		return null;
	}

    private boolean checkUseDefType(ISet useDef) {
        return useDef.getType().isSubtypeOf(linksRelType1) || useDef.getType().isSubtypeOf(linksRelType2);
    }
	
	private IHyperlink[] getTreeLinks(ISourceLocation root, ITree tree, IRegion region) {
		IValue xref = tree.asWithKeywordParameters().getParameter("hyperlinks");
		
		if (xref != null && (xref.getType().isSubtypeOf(linksRelType1) || xref.getType().isSubtypeOf(linksRelType2))) {
		    return getLinksForRegionFromUseDefRelation(root, region, (ISet) xref);
		}
		
		
		ITree ref = TreeAdapter.locateAnnotatedTree(tree, "link", region.getOffset());
		
		if (ref != null) {
			IValue link = ref.asWithKeywordParameters().getParameter("link");
			
			if (link != null && link.getType().isSourceLocation()) { 
				return new IHyperlink[] { new SourceLocationHyperlink(TreeAdapter.getLocation(ref), (ISourceLocation) link) };
			}
			
			
		}
		ref = TreeAdapter.locateAnnotatedTree(tree, "links", region.getOffset());
		if (ref != null) {
			IValue links = ref.asWithKeywordParameters().getParameter("links");
			if (links != null && links.getType().isSet() && links.getType().getElementType().isSourceLocation()) {
				IHyperlink[] a = new IHyperlink[((ISet) links).size()];
				int i = 0;
				for (IValue l : ((ISet) links)) {
					a[i++] = new SourceLocationHyperlink(TreeAdapter.getLocation(ref), (ISourceLocation) l);
				}
				return a;
			}
		}
		
		IValue docLinksMapValue = tree.asWithKeywordParameters().getParameter("docLinks");
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

    private IHyperlink[] getLinksForRegionFromUseDefRelation(ISourceLocation rootLocation, IRegion region, ISet rel) {
    	rootLocation = rootLocation.top();

        List<IHyperlink> links = new ArrayList<>();
         
        for (IValue v: rel) {
        	ITuple t = ((ITuple)v);
        	ISourceLocation loc = (ISourceLocation)t.get(0);
            if (loc.top().equals(rootLocation) && region.getOffset() >= loc.getOffset() && region.getOffset() < loc.getOffset() + loc.getLength()) {
                ISourceLocation to = (ISourceLocation)t.get(1);
                if (rel.getType().getElementType().getArity() == 3 && rel.getType().getElementType().getFieldType(2).isString()) {
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
