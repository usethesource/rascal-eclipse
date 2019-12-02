/*******************************************************************************
 * Copyright (c) 2009-2015 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.rascalmpl.eclipse.Activator;

import io.usethesource.vallang.ISourceLocation;

public class TargetURIResolver implements ISourceLocationInputRewriter, ISourceLocationOutputRewriter, IURIResourceResolver {
    
    @Override
    public ISourceLocation rewrite(ISourceLocation uri) throws IOException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.getAuthority());
 
        if (project == null) {
            throw new FileNotFoundException(uri.toString());
        }
        
        try {
            if (project != null && project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
                IJavaProject jProject = JavaCore.create(project);
                ISourceLocation root = ProjectURIResolver.constructProjectURI(jProject.getOutputLocation().makeAbsolute());
                return URIUtil.getChildLocation(root, uri.getPath());
            }
        } catch (CoreException e) {
            Activator.log("could not resolve target folder", e);
        }
        
        try {
            ISourceLocation root = URIUtil.changeScheme(uri, "project");
            ISourceLocation bin = URIUtil.getChildLocation(root, "bin");
            if (URIResolverRegistry.getInstance().exists(bin)) {
                return URIUtil.getChildLocation(bin, uri.getPath());
            }
            bin = URIUtil.getChildLocation(root, "target/classes"); 
            if (URIResolverRegistry.getInstance().exists(bin)) {
                return URIUtil.getChildLocation(bin, uri.getPath());
            }
            
            throw new FileNotFoundException(uri.toString());
        } catch (URISyntaxException e) {
           throw new IOException(e);
        }
    }

    @Override
    public Charset getCharset(ISourceLocation uri) throws IOException {
        return ISourceLocationInputRewriter.super.getCharset(uri);
    }

    @Override
    public boolean supportsHost() {
        return false;
    }

    @Override
    public String scheme() {
        return "target";
    }

    @Override
    public IResource getResource(ISourceLocation uri) throws IOException {
        return URIResourceResolver.getResource(rewrite(uri));
    }

    @Override
    public URIResolverRegistry reg() {
        return URIResolverRegistry.getInstance();
    }
}
