package org.rascalmpl.eclipse.util;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.uri.ProjectURIResolver;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;

import io.usethesource.vallang.IListWriter;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.exceptions.FactTypeUseException;
import io.usethesource.vallang.io.StandardTextReader;

/**
 * ProjectConfig is a builder to produce a proper Rascal PathConfig for an Eclipse project.
 * This is not yet complete.
 */
public class ProjectPathConfig {
    public static final String BIN_FOLDER = "bin";
    public static final String MVN_TARGET_FOLDER = "target";
    private final IValueFactory vf;
    
    public ProjectPathConfig(IValueFactory vf) {
        this.vf = vf;
    }
    
    public PathConfig getPathConfig(IProject project) {
        ISourceLocation projectLoc = ProjectURIResolver.constructProjectURI(project.getFullPath());
        
        RascalEclipseManifest manifest = new RascalEclipseManifest();
        
        IListWriter libsWriter = vf.listWriter();
        IListWriter srcsWriter = vf.listWriter();
        IListWriter coursesWriter = vf.listWriter();
        
        
        // we special-case the rascal project for bootstrapping purposes (avoiding confusing between source and bootstrapped library)
        if (!isRascalBootstrapProject(project)) {
            libsWriter.append(URIUtil.correctLocation("lib", "rascal", ""));
            libsWriter.append(URIUtil.correctLocation("lib", "rascal_eclipse", ""));
        }
        else if (isRascalEclipseBootstrapProject(project)) {
            libsWriter.append(URIUtil.correctLocation("lib", "rascal", ""));
        }
        
        // These are jar files which make contain compiled Rascal code to link to, but also installed libraries and plugins
        for (String lib : manifest.getRequiredLibraries(project)) {
            if (lib.startsWith("|")) {
                try {
                    ISourceLocation libLocation = (ISourceLocation) new StandardTextReader().read(vf, new StringReader(lib));
                    
                    if (libLocation.getScheme().equals("lib")) {
                        // if this is another project in the workspace, resolve
                        // the lib location to the target folder of the respective project
                        String projectName = libLocation.getAuthority();
                        IProject libProject = project.getWorkspace().getRoot().getProject(projectName);
                        
                        if (libProject != null && libProject.exists() && libProject.isOpen()) {
                            ISourceLocation target = getJavaTargetFolder(libProject);
                            
                            libsWriter.append(target);
                            continue;
                        }
                    }

                    if (URIResolverRegistry.getInstance().exists(libLocation)) {
                        libsWriter.append(libLocation);
                    }
                    else {
                        throw new FileNotFoundException(libLocation.toString());
                    }
                } catch (FactTypeUseException | IOException e) {
                    Activator.log("failed to depend on library: [" + lib + "]", e);
                }
            }
            else {
                libsWriter.append(URIUtil.getChildLocation(projectLoc, lib));
            }
        }
        
        for (String course : manifest.getCourses(project)) {
            coursesWriter.append(URIUtil.getChildLocation(projectLoc, course));
        }
        
        // the bin folder to the lib path
        // TODO: this should be removed soon (project references are superceded by Require-Libraries in RASCAL.MF
        try {
            if (!isRascalBootstrapProject(project)) {
                for (IProject ref : project.getReferencedProjects()) {
                    ISourceLocation child = URIUtil.getChildLocation(ProjectURIResolver.constructProjectURI(ref.getFullPath()), BIN_FOLDER);
                    libsWriter.append(child);
                }
            }
        }
        catch (CoreException e) {
            Activator.log(e.getMessage(), e);
        }
        
        for (String srcName : manifest.getSourceRoots(project)) {
            ISourceLocation src = URIUtil.getChildLocation(projectLoc, srcName);
            srcsWriter.append(src);
        }

        ISourceLocation bin = getJavaTargetFolder(project);

        try {
            return new PathConfig(
                    srcsWriter.done(), 
                    libsWriter.done(), 
                    bin, 
                    coursesWriter.done(), 
                    vf.list(),  // TODO compiler path for when code actually has to be compiled
                    vf.list()); // TODO classloader path for when the compiled code must run
        }
        catch (IOException e) {
            // one of the dependencies failed to resolve
            Activator.log("could not resolve project dependencies, defaulting to empty list of dependencies for now.", e);
            try {
                return new PathConfig(
                        srcsWriter.done(), 
                        vf.list(), 
                        bin, 
                        coursesWriter.done(), 
                        vf.list(),  // TODO compiler path for when code actually has to be compiled
                        vf.list());
            } catch (IOException e1) {
                Activator.log("unexpected exception generating a default path configuration", e1);
                return new PathConfig();
            }
        }
    }

    private ISourceLocation getJavaTargetFolder(IProject project) {
        String binFolder = BIN_FOLDER;
        
        try {
            if (project.hasNature(JavaCore.NATURE_ID)) {
                IJavaProject jProject = JavaCore.create(project);
                binFolder = jProject.getOutputLocation().removeFirstSegments(1).toOSString();
            }
        } catch (CoreException e) {
            Activator.log("could not find output location", e);
        }
        
        ISourceLocation projectLoc = ProjectURIResolver.constructProjectURI(project.getFullPath());;
        return URIUtil.getChildLocation(projectLoc, binFolder);
    }

    private boolean isRascalBootstrapProject(IProject project) {
        return Arrays.asList("rascal", "rascal-eclipse", "rascal_eclipse").contains(project.getName());
    }
    
    private boolean isRascalEclipseBootstrapProject(IProject project) {
        return Arrays.asList("rascal-eclipse", "rascal_eclipse").contains(project.getName());
    }
}
