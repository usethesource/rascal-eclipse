package org.rascalmpl.eclipse.tutor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.IDEServicesModelProvider;
import org.rascalmpl.eclipse.preferences.RascalPreferences;
import org.rascalmpl.eclipse.util.ProjectConfig;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.NoSuchRascalFunction;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.ideservices.BasicIDEServices;
import org.rascalmpl.library.experiments.tutor3.CourseCompiler;
import org.rascalmpl.library.experiments.tutor3.TutorCommandExecutor;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.uri.URIResourceResolver;
import org.rascalmpl.uri.URIUtil;
import org.tukaani.xz.UnsupportedOptionsException;

import io.usethesource.impulse.runtime.RuntimePlugin;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.ISourceLocation;

public class Builder extends IncrementalProjectBuilder {
    private final PrintWriter err = new PrintWriter(RuntimePlugin.getInstance().getConsoleStream());
    private PathConfig cachedConfig;
    private TutorCommandExecutor cachedExecutor;

    public Builder() {
    }

//    private boolean inOutputFolder(IResource resource) throws JavaModelException {
//        PathConfig pcfg = getPathConfig(resource);
//        ISourceLocation binURI = ProjectURIResolver.constructProjectURI(resource.getProject(), resource.getProjectRelativePath());
//        IPath targetPath = JavaCore.create(resource.getProject()).getOutputLocation();
//        
//        return binURI.getPath().startsWith(pcfg.getBin().getPath())
//            || resource.getFullPath().toString().startsWith(targetPath.toString());
//    }

    private PathConfig getPathConfig(IResource resource) {
        if (cachedConfig == null) {
            cachedConfig = IDEServicesModelProvider.getInstance().getPathConfig(resource.getProject());
        }
         
        return cachedConfig;
    }

    private static Path loc2path(ISourceLocation loc) {
        return Paths.get(URIResourceResolver.getResource(loc).getLocation().toFile().toURI());
    }
    
    private static Path file2path(IFile file) {
        return Paths.get(file.getLocation().toFile().toURI());
    }
    
    @Override
    protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
            throws CoreException {
        if (RascalPreferences.conceptCompilerEnabled()) {
            IProject project = getProject();

            ICoreRunnable runner = new ICoreRunnable() {
                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    switch (kind) {
                        case INCREMENTAL_BUILD:
                        case AUTO_BUILD:
                            monitor.beginTask("Compiling dirty course concepts", 10);
                            try {
                                List<IFile> todo = new LinkedList<>();

                                if (WorkCollector.fillWorkList(getDelta(getProject()), todo)) {
                                    clean(monitor);
                                }
                                else if (todo.size() > 0) {
                                    for (IFile dirty : todo) {
                                        buildIncremental(dirty, monitor);
                                    }
                                }
                            } catch (CoreException e) {
                                Activator.log("incremental Rascal build failed", e);
                            }
                            break;
                        case FULL_BUILD:
                            Activator.log("Ignoring full tutor build trigger", new UnsupportedOptionsException());
                            break;
                    }
                }
            };

            project.getWorkspace().run(runner, project, 0 /* no resource locking? */, monitor);
        }

        return new IProject[0];
    }
    
    private static class WorkCollector implements IResourceDeltaVisitor {
        private List<IFile> dirty = new LinkedList<>();
        
        /**
         * Analyzes what to do based on a set of changed resources
         * 
         * @param delta the input model of changed resources in Eclipse
         * @param todo  an output parameter which will contain the worklist
         * @return true iff the whole project must be cleaned for some reason
         */
        public static boolean fillWorkList(IResourceDelta delta, List<IFile> todo) {
            assert todo.isEmpty();

            try {
                WorkCollector c = new WorkCollector();
                delta.accept(c);
                todo.addAll(c.dirty);
            } catch (CoreException e) {
                Activator.log("incremental builder failed", e);
            }
            
            return false;
        }
        
        public boolean visit(IResourceDelta delta) throws CoreException {
            IPath path = delta.getProjectRelativePath();
            
            String ext = delta.getFullPath().getFileExtension();
            if ("concept".equals(ext)) {
                if ((delta.getFlags() & IResourceDelta.CONTENT) == 0) {
                    return false;
                }

                dirty.add((IFile) delta.getResource());

                return false;
            }
            
            return !ProjectConfig.BIN_FOLDER.equals(path.toPortableString())
                // if a duplicate bin folder from maven exists, don't recurse into it:
                // this is brittle, but it saves a lot of time waiting for unnecessary compilation:
                && !ProjectConfig.MVN_TARGET_FOLDER.equals(path.toPortableString());    
        }
    }
    
    protected void buildIncremental(IFile file, IProgressMonitor monitor) throws JavaModelException {
        if (!RascalPreferences.conceptCompilerEnabled()) {
            return;
        }
        
        PathConfig pcfg = getPathConfig(file);
        
        Path coursesSrcPath = getSourcePath(pcfg);
        
        // TODO: a project may have multiple source paths
        Path libSrcPath = loc2path((ISourceLocation)pcfg.getSrcs().get(0));
        Path destPath = loc2path((ISourceLocation)pcfg.getBin()).resolve("courses");
        
        try {
            String courseName  = getCourseName(pcfg, file, coursesSrcPath);
            CourseCompiler.copyStandardFiles(coursesSrcPath, destPath);

            monitor.subTask("Initializing tutor command executor");
            TutorCommandExecutor executor = getCommandExecutor(pcfg);
            monitor.worked(2);
           
            if (courseName != null) {
                // we can only have only builder executing at a time due to file sharing on disk
                synchronized (Builder.class) {
                    monitor.subTask("Compiling course " + courseName);
                    CourseCompiler.compileCourseCommand(getDoctorClasspath(), coursesSrcPath, courseName, destPath, libSrcPath, pcfg, executor);
                    monitor.worked(5);
                    err.flush();
                }
                
                monitor.subTask("Starting viewer");
                if (RascalPreferences.liveConceptPreviewEnabled()) {
                    TutorPreview.previewConcept(file);
                }
                monitor.worked(3);
               
                return;
            }
            else {
                Activator.log("could not find course name for " + file, null);
            }
        } catch (IOException | NoSuchRascalFunction | URISyntaxException e) {
            Activator.log("unexpected error during course compilation for " + file, e);
            return;
        } catch (Throwable e) {
            Activator.log("very unexpected error during course compilation of " + file, e);
            e.printStackTrace(err);
            return;
        }
    }

    private static Path getSourcePath(PathConfig pcfg) {
        IList courseList = pcfg.getCourses();
        if (courseList.length() == 0) {
            throw new IllegalArgumentException("no courses configured in META-INF/RASCAL.MF");
        }
        else if (courseList.length() > 1) {
            Activator.getInstance().logException("ignoring all but the first Courses from META-INF/RASCAL.MF", null);
        }
        
        // TODO: project should be able to have multiple courses directories
        return loc2path((ISourceLocation) courseList.get(0));
    }

    public static URL getConceptURL(String scheme, String authority, PathConfig pcfg, IFile file) throws IOException, URISyntaxException {
        String name = getCourseName(pcfg, file);
        String anchor = getConceptAnchor(getSourcePath(pcfg), file);
        return URIUtil.create(scheme, authority, "/" + name + "/index.html", null, anchor).toURL();
    }
    
    private static String getConceptAnchor(Path coursesSrcPath, IFile file) {
        Path name = coursesSrcPath.relativize(file2path(file)).getParent();
        int n = name.getNameCount();
        return n >= 2 ? name.getName(n-2) + "-" + name.getName(n-1) : name.getFileName().toString();
    }
    
    private static String getDoctorClasspath() throws IOException {
        return libLocation("lib/jruby.jar") + File.pathSeparator
                + libLocation("lib/jcommander.jar") + File.pathSeparator
                + libLocation("lib/asciidoctor.jar")
                ;
    }

    private static String libLocation(String lib) throws IOException {
        return FileLocator.resolve(Activator.getInstance().getBundle().getEntry(lib)).getPath();
    }
    
    private TutorCommandExecutor getCommandExecutor(PathConfig pcfg)
            throws IOException, NoSuchRascalFunction, URISyntaxException {
        if (this.cachedConfig != null && !freshConfig(pcfg) && cachedExecutor != null) {
            return cachedExecutor;
        }
         
        cachedConfig = pcfg;
        cachedExecutor = new TutorCommandExecutor(pcfg, err, new BasicIDEServices(err));
        
        return this.cachedExecutor;
    }
    
    private boolean freshConfig(PathConfig pcfg) {
        return !this.cachedConfig.toString().equals(pcfg.toString());
    }

    public static String getCourseName(PathConfig pcfg, IFile file) throws IOException {
        return getCourseName(pcfg,  file, getSourcePath(pcfg));
    }
    
    private static String getCourseName(PathConfig pcfg, IFile file, Path coursesSrcPath) throws IOException {
        String filePath = file.getLocation().toFile().getAbsolutePath();
        
        try (DirectoryStream<Path> dirs = Files.newDirectoryStream(coursesSrcPath)) {
            for (Path course : dirs) {
                if (filePath.startsWith(course.toAbsolutePath().toFile().getAbsolutePath())) {
                    return course.getFileName().toString();
                }
            }
        } 
        
        throw new IOException("No course found containing: " + file);
    }
}
