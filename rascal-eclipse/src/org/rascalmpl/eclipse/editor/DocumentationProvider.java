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

import org.eclipse.core.resources.IProject;
import org.rascalmpl.eclipse.preferences.RascalPreferences;
import org.rascalmpl.eclipse.terms.TermParseController;
import org.rascalmpl.eclipse.util.ProjectConfig;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.uptr.ITree;
import org.rascalmpl.values.uptr.RascalValueFactory;
import org.rascalmpl.values.uptr.TreeAdapter;

import io.usethesource.impulse.model.ISourceProject;
import io.usethesource.impulse.parser.IParseController;
import io.usethesource.impulse.services.IDocumentationProvider;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IMap;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.IValue;

/*
 * Assuming the innermost lexical node in the parse tree is given,  we simply return the annotation labeled
 * "doc" which is a string.
 */
public class DocumentationProvider  implements IDocumentationProvider {
    private static final IDEServicesModelProvider imp = IDEServicesModelProvider.getInstance();
    
	public String getDocumentation(Object target,
			IParseController parseController) {
		if (target instanceof IConstructor) {
			if (((IConstructor) target).getType().isSubtypeOf(RascalValueFactory.Tree)) {
				return getDocString((IConstructor) target, (IConstructor) parseController.getCurrentAst(), parseController);
			}
		}
		
		return null;
	}

	private String getDocString(IConstructor arg, IConstructor top, IParseController parseController) {
	    if (top != null && arg != null && parseController instanceof TermParseController) {
            // DSL case
            return getDocStringFromTree(arg, top);
        }
        
        if (arg != null && parseController instanceof ParseController && RascalPreferences.isRascalCompilerEnabled()) {
            ParseController rascalPc = (ParseController) parseController;
            ISourceProject rprj = rascalPc.getProject();
            IProject prj = rprj != null ? rprj.getRawProject() : null;
            PathConfig pcfg =  prj != null ? new ProjectConfig(ValueFactoryFactory.getValueFactory()).getPathConfig(prj) : new PathConfig();

            ISet useDefs = imp.getUseDef(rascalPc.getSourceLocation(), pcfg, rascalPc.getModuleName());
            IMap synopses = imp.getSynopses(rascalPc.getSourceLocation(), pcfg, rascalPc.getModuleName());
            IMap docLinks = imp.getDocLocs(rascalPc.getSourceLocation(), pcfg, rascalPc.getModuleName());
            
            if (useDefs == null || synopses == null) {
                return null;
            }
            
            ISet defs = useDefs.asRelation().index(TreeAdapter.getLocation((ITree) arg));

            if (defs == null) {
                return null;
            }
            
            StringBuffer b = new StringBuffer();
            
            b.append("<ul>");
            for (IValue def : defs) {
                IValue synopsis = synopses.get(def);
                boolean hasLink = false;
                
                b.append("<li>");
                if (docLinks != null) { 
                    ISourceLocation docLink = (ISourceLocation) docLinks.get(def);
                    
                    if (docLink != null) {
                        hasLink = true;
                        b.append("<a href=\"");
                        b.append(docLink.getURI());
                        b.append("\">");
                    }
                }
                
                if (synopsis != null) {
                    b.append(((IString) synopsis).getValue());
                }
                
                if (hasLink) {
                    b.append("</a>");
                }
                
                b.append("</li>");
            }
            b.append("</ul>");
            
            return b.toString();
        }
		
		return null;
	}

    private String getDocStringFromTree(IConstructor arg, IConstructor top) {
        IValue val = arg.asAnnotatable().getAnnotation("doc");

		if (val != null && val.getType().isString()) {
				return ((IString) val).getValue();
		}
		
		if (top != null && top.getType().isSubtypeOf(RascalValueFactory.Tree)) {
			IValue vals = top.asAnnotatable().getAnnotation("docs");
			
			if (vals != null 
					&& vals.getType().isMap() 
					&& vals.getType().getKeyType().isSourceLocation() 
					&& vals.getType().getValueType().isString()) {
				IMap map = (IMap) vals;
				ISourceLocation loc = (ISourceLocation) arg.asAnnotatable().getAnnotation("loc");
				if (loc != null && map.containsKey(loc)) {
					return ((IString) map.get(loc)).getValue();
				}
			}
		}
		
		return null;
    }	
}