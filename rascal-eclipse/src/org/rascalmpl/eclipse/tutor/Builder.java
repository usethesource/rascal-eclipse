package org.rascalmpl.eclipse.tutor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.IDEServicesModelProvider;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.NoSuchRascalFunction;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.ideservices.BasicIDEServices;
import org.rascalmpl.library.experiments.tutor3.CourseCompiler;
import org.rascalmpl.library.experiments.tutor3.Onthology;
import org.rascalmpl.library.experiments.tutor3.TutorCommandExecutor;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.uri.ProjectURIResolver;
import org.rascalmpl.uri.URIResourceResolver;

import io.usethesource.impulse.builder.BuilderBase;
import io.usethesource.impulse.runtime.PluginBase;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.ISourceLocation;

public class Builder extends BuilderBase {
    private final Map<IProject, Onthology> ontologies = new HashMap<>();
    private final PrintWriter err = new PrintWriter(getConsoleStream());
    private PathConfig cachedConfig;
    private TutorCommandExecutor cachedExecutor;
    private Asciidoctor asciidoctor;

    public Builder() {
    }

    @Override
    protected PluginBase getPlugin() {
        return Activator.getInstance();
    }

    @Override
    protected boolean isSourceFile(IFile file) {
        return "concept".equals(file.getFileExtension());
    }

    @Override
    protected boolean isNonRootSourceFile(IFile file) {
        return false;
    }

    @Override
    protected boolean isOutputFolder(IResource resource) {
        PathConfig pcfg = getPathConfig(resource);
        ISourceLocation binURI = ProjectURIResolver.constructProjectURI(resource.getProject(), resource.getProjectRelativePath());
        
        return pcfg.getBin().equals(binURI);
    }

    private PathConfig getPathConfig(IResource resource) {
        return IDEServicesModelProvider.getInstance().getPathConfig(resource.getProject());
    }

    private static Path loc2path(ISourceLocation loc) {
        return Paths.get(URIResourceResolver.getResource(loc).getLocation().toFile().toURI());
    }
    
    @Override
    protected void compile(IFile file, IProgressMonitor monitor) {
        PathConfig pcfg = getPathConfig(file);
        
        IList courseList = pcfg.getCourses();
        if (courseList.length() == 0) {
            Activator.getInstance().logException("no courses configured in META-INF/RASCAL.MF", null);
            return;
        }
        else if (courseList.length() > 1) {
            Activator.getInstance().logException("ignoring all but the first Courses from META-INF/RASCAL.MF", null);
        }
        
        // TODO: project should be able to have multiple courses directories
        Path coursesSrcPath = loc2path((ISourceLocation) courseList.get(0));
        
        // TODO: a project may have multiple source paths
        Path libSrcPath = loc2path((ISourceLocation)pcfg.getSrcs().get(0));
        Path destPath = loc2path((ISourceLocation)pcfg.getBin()).resolve("courses");
        
        try {
            CourseCompiler.copyStandardFiles(coursesSrcPath, destPath);

            TutorCommandExecutor executor = getCommandExecutor(pcfg);

            String courseName  = getCourseName(pcfg, file, coursesSrcPath);
            if (courseName != null) {
                Onthology ontology = getOntology(file.getProject(), coursesSrcPath, courseName, destPath, libSrcPath, pcfg, executor);

                CourseCompiler.compileCourseCommand(getDoctorClasspath(), coursesSrcPath, courseName, destPath, libSrcPath, pcfg, executor);
                err.flush();

                PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = CourseCompiler.courseIndexFile(destPath.resolve(courseName)).toURI().toURL();
                            System.err.println(url);
//                            HtmlDisplay.browse(url);
                        } catch (MalformedURLException e) {
                            Activator.log("could not show tutor compiled result", e);
                        }
                    }
                });
               
                return;
            }
            else {
                Activator.log("could not find course name for " + file, null);
            }
        } catch (IOException | NoSuchRascalFunction | URISyntaxException e) {
            Activator.log("unexpected error during course compilation for " + file, e);
            return;
        }
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
        if (this.cachedConfig != null && this.cachedConfig.toString().equals(pcfg.toString())) {
            return cachedExecutor;
        }
         
        cachedConfig = pcfg;
        cachedExecutor = new TutorCommandExecutor(pcfg, err, new BasicIDEServices(err));
        return this.cachedExecutor;
    }

    private String getCourseName(PathConfig pcfg, IFile file, Path coursesSrcPath) {
        String filePath = file.getLocation().toFile().getAbsolutePath();
        
        try (DirectoryStream<Path> dirs = Files.newDirectoryStream(coursesSrcPath)) {
            for (Path course : dirs) {
                if (filePath.startsWith(course.toAbsolutePath().toFile().getAbsolutePath())) {
                    return course.getFileName().toString();
                }
            }
        } 
        catch (IOException e) {
            Activator.log("could not compute course name", e);
        }
        
        return null;
    }

    private Onthology getOntology(IProject project, Path srcPath, String courseName, Path destPath, Path libSrcPath, PathConfig pcfg, TutorCommandExecutor executor) throws IOException, NoSuchRascalFunction, URISyntaxException {
        Onthology result = ontologies.get(project);

        if (result == null) {
            result = new Onthology(srcPath, courseName, destPath, libSrcPath, pcfg, executor);
            ontologies.put(project, result);
        }
        
        return result;
    }

    @Override
    protected void collectDependencies(IFile file) {

    }

    @Override
    protected String getErrorMarkerID() {
        return "tutor3.error";
    }

    @Override
    protected String getWarningMarkerID() {
        return "tutor3.warning";
    }

    @Override
    protected String getInfoMarkerID() {
        return "tutor3.info";
    }

}
