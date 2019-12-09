package org.rascalmpl.eclipse.util;


import java.io.IOException;
import java.io.StringReader;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.uri.ProjectURIResolver;
import org.rascalmpl.uri.URIUtil;

import io.usethesource.vallang.IListWriter;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValueFactory;
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
    
    public PathConfig getPathConfig(IProject project) throws IOException {
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
        
        // These are jar files which make contain compiled Rascal code to link to, but also installed libraries and plugins
        for (String lib : manifest.getRequiredLibraries(project)) {
            if (lib.startsWith("|")) {
                libsWriter.append(new StandardTextReader().read(vf, new StringReader(lib)));
            }
            else {
                libsWriter.append(URIUtil.getChildLocation(projectLoc, lib));
            }
        }
        
        for (String course : manifest.getCourses(project)) {
            coursesWriter.append(URIUtil.getChildLocation(projectLoc, course));
        }
        
        // the bin folder to the lib path
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

        String binFolder = BIN_FOLDER;
        
        try {
            if (project.hasNature(JavaCore.NATURE_ID)) {
                IJavaProject jProject = JavaCore.create(project);
                binFolder = jProject.getOutputLocation().removeFirstSegments(1).toOSString();
            }
        } catch (CoreException e) {
            Activator.log("could not find output location", e);
        }

        ISourceLocation bin = URIUtil.getChildLocation(projectLoc, binFolder);
        libsWriter.insert(bin);

        return new PathConfig(
                srcsWriter.done(), 
                libsWriter.done(), 
                bin, 
                URIUtil.correctLocation("boot", "", ""), 
                coursesWriter.done(), 
                vf.list(),  // TODO compiler path for when code actually has to be compiled
                vf.list()); // TODO classloader path for when the compiled code must run
    }

    private boolean isRascalBootstrapProject(IProject project) {
        return "rascal".equals(project.getName());
    }
}
