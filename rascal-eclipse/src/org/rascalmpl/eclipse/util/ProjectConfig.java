package org.rascalmpl.eclipse.util;


import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.uri.ProjectURIResolver;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.value.IListWriter;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.IValueFactory;

/**
 * ProjectConfig is a builder to produce a proper Rascal PathConfig for an Eclipse project.
 * This is not yet complete.
 */
public class ProjectConfig {
    public static final String BIN_FOLDER = "bin";
    private final IValueFactory vf;
    
    public ProjectConfig(IValueFactory vf) {
        this.vf = vf;
    }
    
    public PathConfig getPathConfig(IProject project) {
        ISourceLocation projectLoc = ProjectURIResolver.constructProjectURI(project.getFullPath());
        
        RascalEclipseManifest manifest = new RascalEclipseManifest();
        
        IListWriter libsWriter = vf.listWriter();
        IListWriter srcsWriter = vf.listWriter();
        
        
        // we special-case the rascal project for bootstrapping purposes (avoiding confusing between source and bootstrapped library)
        if (!isRascalBootstrapProject(project)) {
            // TODO: this needs to be configured elsewhere
            libsWriter.append(URIUtil.correctLocation("std", "", ""));
            libsWriter.append(URIUtil.correctLocation("plugin", "rascal_eclipse", "/src/org/rascalmpl/eclipse/library"));
        }
        
        // These are jar files which make contain compiled Rascal code to link to:
        for (String lib : manifest.getRequiredLibraries(project)) {
            libsWriter.append(URIUtil.getChildLocation(projectLoc, lib));
        }
        
        // These are other projects referenced by the current project for which we add
        // the bin folder to the lib path
        try {
            if (!isRascalBootstrapProject(project)) {
                for (IProject ref : project.getReferencedProjects()) {
                    libsWriter.append(URIUtil.getChildLocation(ProjectURIResolver.constructProjectURI(ref.getFullPath()), BIN_FOLDER));

                    // TODO for now we also add the source paths; needs to be done more gracefully 
                    srcsWriter.appendAll(new ProjectConfig(vf).getPathConfig(ref).getSrcs());
                }
            }
            
            //TODO add required libraries of referenced projects as well.
        }
        catch (CoreException e) {
            Activator.log(e.getMessage(), e);
        }
        
        for (String srcName : manifest.getSourceRoots(project)) {
            ISourceLocation src = URIUtil.getChildLocation(projectLoc, srcName);
            srcsWriter.append(src);
        }
        
        // TODO this is necessary while the kernel does not hold a compiled standard library, so remove later:
        // We special-case the rascal project for bootstrapping purposes (avoiding confusing between source and bootstrapped library)
        if (!isRascalBootstrapProject(project)) {
            srcsWriter.append(URIUtil.correctLocation("std", "", ""));
            srcsWriter.append(URIUtil.correctLocation("plugin", "rascal_eclipse", "/src/org/rascalmpl/eclipse/library"));
        }
        
        ISourceLocation bin = URIUtil.getChildLocation(projectLoc, BIN_FOLDER);
        ISourceLocation boot = URIUtil.correctLocation("boot", "", "");
        

        // Here we find out what the compiler class path must be for compiling generated Rascal parsers
        // and we construct a class path for the JavaBridge to load java builtins
        List<ISourceLocation> javaCompilerPath = new ArrayList<>();
        List<ISourceLocation> classloaders = new ArrayList<>();
      
        try {
            Bundle rascalBundle = Activator.getInstance().getBundle();
            
            if (!isRascalBootstrapProject(project)) {
                URL entry = FileLocator.toFileURL(rascalBundle.getEntry("lib/rascal.jar"));
                javaCompilerPath.add(vf.sourceLocation(entry.toURI()));
                classloaders.add(vf.sourceLocation(entry.toURI()));
            }
            
            fillJavaCompilerPath(project, javaCompilerPath);
            collectClassloadersForProject(project, classloaders);
            
        } catch (URISyntaxException | IOException | CoreException e) {
            Activator.log("error while constructing compiler path", e);
        } 
        
        return new PathConfig(
                srcsWriter.done(), 
                libsWriter.done(), 
                bin, 
                boot, 
                vf.list(), 
                vf.list(javaCompilerPath.toArray(new IValue[0])), 
                vf.list(classloaders.toArray(new IValue[0])));
        
    }

    private boolean isRascalBootstrapProject(IProject project) {
        return "rascal".equals(project.getName());
    }

    private void fillJavaCompilerPath(IProject project, List<ISourceLocation> path) throws URISyntaxException {
        RascalEclipseManifest mf = new RascalEclipseManifest();
        
        List<String> requiredBundles = mf.getRequiredBundles(project);
        if (requiredBundles != null) {
            for (String lib : requiredBundles) {
                path.add(vf.sourceLocation("plugin", Platform.getBundle(lib).getSymbolicName(), ""));
            }
        }
        
        List<String> requiredLibraries = mf.getRequiredLibraries(project);
        if (requiredLibraries != null) {
            for (String lib : requiredLibraries) {
                path.add(vf.sourceLocation(project.getFile(lib).getFullPath().makeAbsolute().toFile().getAbsolutePath()));
            }
        }
    }

    private void collectClassloadersForProject(IProject project, List<ISourceLocation> classPath) throws URISyntaxException, JavaModelException, CoreException {
        if (project.hasNature(JavaCore.NATURE_ID)) {
            IJavaProject jProject = JavaCore.create(project);

            IPath binFolder = jProject.getOutputLocation();
            String binLoc = project.getLocation() + "/" + binFolder.removeFirstSegments(1).toString();

            classPath.add(vf.sourceLocation("file", "", binLoc + "/"));

            if (!jProject.isOpen()) {
                return;
            }

            IClasspathEntry[] entries = jProject.getResolvedClasspath(true);

            for (int i = 0; i < entries.length; i++) {
                IClasspathEntry entry = entries[i];
                switch (entry.getEntryKind()) {
                    case IClasspathEntry.CPE_LIBRARY:
                        if (entry.getPath().segment(0).equals(project.getName())) {
                            String file = project.getLocation() + "/" + entry.getPath().removeFirstSegments(1).toString();
                            ISourceLocation loc = vf.sourceLocation("file", "", file);

                            if (!classPath.contains(loc)) {
                                classPath.add(loc);
                            }
                        }
                        else {
                            ISourceLocation url = vf.sourceLocation("file", "", entry.getPath().toString());
                            if (!classPath.contains(url)) {
                                classPath.add(url);
                            }
                        }
                        break;
                    case IClasspathEntry.CPE_PROJECT:
                        collectClassloadersForProject((IProject) project.getWorkspace().getRoot().findMember(entry.getPath()), classPath);
                        break;
                }
            }
        }
    }
}
