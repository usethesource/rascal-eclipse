package org.rascalmpl.eclipse.util;


import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.uri.ProjectURIResolver;
import org.rascalmpl.uri.URIUtil;

import io.usethesource.vallang.IListWriter;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.io.StandardTextReader;

/**
 * ProjectConfig is a builder to produce a proper Rascal PathConfig for an Eclipse project.
 * This is not yet complete.
 */
public class ProjectConfig {
    public static final String BIN_FOLDER = "bin";
    public static final String MVN_TARGET_FOLDER = "target";
    private final IValueFactory vf;
    
    public ProjectConfig(IValueFactory vf) {
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
            // TODO: this needs to be configured elsewhere
            libsWriter.append(URIUtil.correctLocation("std", "", ""));
//            libsWriter.append(URIUtil.correctLocation("stdlib", "", "")); // TODO: should replace the previous later
            libsWriter.append(URIUtil.correctLocation("plugin", "rascal_eclipse", "/src/org/rascalmpl/eclipse/library"));
        }
        
        // These are jar files which make contain compiled Rascal code to link to:
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
        
        // These are other projects referenced by the current project for which we add
        // the bin folder to the lib path
        try {
            if (!isRascalBootstrapProject(project)) {
                for (IProject ref : project.getReferencedProjects()) {
                    ISourceLocation child = URIUtil.getChildLocation(ProjectURIResolver.constructProjectURI(ref.getFullPath()), BIN_FOLDER);
                    libsWriter.append(child);
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
            srcsWriter.append(URIUtil.correctLocation("std", "", "")); // TODO should be removed later
            srcsWriter.append(URIUtil.correctLocation("plugin", "rascal_eclipse", "/src/org/rascalmpl/eclipse/library"));
        }

        if (!isRascalBootstrapProject(project)) {
        	// we also add libraries, but they don't have a bin yet, so we add them to the source folder
        	IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
        			.getExtensionPoint("rascal_eclipse", "rascalLibrary");
        	if (extensionPoint != null) {
        		for (IExtension element : extensionPoint.getExtensions()) {
        			String name = element.getContributor().getName();
        			Bundle bundle = Platform.getBundle(name);
        			List<String> roots = new RascalEclipseManifest().getSourceRoots(bundle);
        			for (String root : roots) {
        				srcsWriter.append(URIUtil.correctLocation("plugin", bundle.getSymbolicName(), "/" + root));
        			}
        		}
        	} 
        }
        
        ISourceLocation bin = URIUtil.getChildLocation(projectLoc, BIN_FOLDER);
        ISourceLocation boot = URIUtil.correctLocation("boot", "", "");
        
        libsWriter.insert(bin);
        
        // Here we find out what the compiler class path must be for compiling generated Rascal parsers
        // and we construct a class path for the JavaBridge to load java builtins
        List<ISourceLocation> javaCompilerPath = new ArrayList<>();
        List<ISourceLocation> classloaders = new ArrayList<>();
      
        try {
            Bundle rascalBundle = Activator.getInstance().getBundle();
            
            if (!isRascalBootstrapProject(project)) {
                URL entry = FileLocator.toFileURL(rascalBundle.getEntry("lib/rascal.jar"));
                ISourceLocation loc = vf.sourceLocation(URIUtil.fromURL(entry));
				javaCompilerPath.add(loc);
                classloaders.add(loc);
            }
           
            collectPathForProject(project, javaCompilerPath, classloaders);
            
            return new PathConfig(
                    srcsWriter.done(), 
                    libsWriter.done(), 
                    bin, 
                    boot, 
                    coursesWriter.done(), 
                    vf.list(javaCompilerPath.toArray(new IValue[0])), 
                    vf.list(classloaders.toArray(new IValue[0])));

        } catch (URISyntaxException | CoreException e) {
            throw new IOException(e);
        } 
    }

    private boolean isRascalBootstrapProject(IProject project) {
        return "rascal".equals(project.getName());
    }

    private void collectPathForProject(IProject project,  List<ISourceLocation> compilerPath, List<ISourceLocation> classloaders) throws URISyntaxException, JavaModelException, CoreException {
        // this even works if the project is not a Java project,
        // we load bundle dependencies and local jars directly from RASCAL.MF
        if (project.hasNature(IRascalResources.ID_RASCAL_NATURE)) {
            RascalEclipseManifest mf = new RascalEclipseManifest();
            
            List<String> requiredLibraries = mf.getRequiredLibraries(project);
            if (requiredLibraries != null) {
                for (String lib : requiredLibraries) {
                    ISourceLocation loc = vf.sourceLocation(project.getFile(lib).getFullPath().makeAbsolute().toFile().getAbsolutePath());
                    if (!classloaders.contains(loc)) {
                        classloaders.add(loc);
                    }
                }
            }
        }
        
        // Here we implement the meta-inf from META-INF/MANIFEST.MF, and
        // the .classpath of Eclipse, adding all jar files to the compiler
        // path and the classloader path, while in fact for the compilation we 
        // need less. TODO: only add what is necessary for compiling generated parser code.
        if (project.hasNature(JavaCore.NATURE_ID)) {
            IJavaProject jProject = JavaCore.create(project);

            if (!jProject.isOpen()) {
                return;
            }
            
            IPath binFolder = jProject.getOutputLocation();
            String binLoc = project.getLocation() + "/" + binFolder.removeFirstSegments(1).toString();

            classloaders.add(vf.sourceLocation("file", "", binLoc + "/"));

//            if (!isRascalBootstrapProject(project)) {
                compilerPath.add(vf.sourceLocation("file", "", binLoc + "/"));
//            }
            
           

            IClasspathEntry[] entries = jProject.getResolvedClasspath(true);

            for (int i = 0; i < entries.length; i++) {
                IClasspathEntry entry = entries[i];
                switch (entry.getEntryKind()) {
                    case IClasspathEntry.CPE_LIBRARY:
                        if (entry.getPath().segment(0).equals(project.getName())) {
                            String file = project.getLocation() + "/" + entry.getPath().removeFirstSegments(1).toString();
                            ISourceLocation loc = vf.sourceLocation("file", "", file);

                            if (!classloaders.contains(loc)) {
                                classloaders.add(loc);
                            }
                            
                            if (!compilerPath.contains(loc)) {
                                compilerPath.add(loc);
                            }
                        }
                        else {
                            ISourceLocation url = vf.sourceLocation("file", "", entry.getPath().toString());
                            if (!classloaders.contains(url)) {
                                classloaders.add(url);
                            }
                            if (!compilerPath.contains(url)) {
                                compilerPath.add(url);
                            }
                        }
                        break;
                    case IClasspathEntry.CPE_PROJECT:
                        IProject libProject = (IProject) project.getWorkspace().getRoot().findMember(entry.getPath());
                        if (libProject != null) {
                            collectPathForProject(libProject, compilerPath, classloaders);
                        }
                        else {
                            Activator.log("could not find project for " + entry.getPath() + " reference.", new IllegalArgumentException());
                        }
                        break;
                }
            }
        }
    }
}
