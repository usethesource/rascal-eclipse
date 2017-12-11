package org.rascalmpl.eclipse.tutor;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.IDEServicesModelProvider;
import org.rascalmpl.eclipse.library.util.HtmlDisplay;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.NoSuchRascalFunction;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.ideservices.BasicIDEServices;
import org.rascalmpl.library.experiments.tutor3.CourseCompiler;
import org.rascalmpl.library.experiments.tutor3.Onthology;
import org.rascalmpl.library.experiments.tutor3.TutorCommandExecutor;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.uri.ProjectURIResolver;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIResourceResolver;
import org.rascalmpl.values.uptr.IRascalValueFactory;

import io.usethesource.impulse.builder.BuilderBase;
import io.usethesource.impulse.runtime.PluginBase;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.ISourceLocation;

public class Builder extends BuilderBase {
    private final Map<IProject, Onthology> ontologies = new HashMap<>();
    private final PrintWriter err = new PrintWriter(getConsoleStream());

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
        
        Path coursesSrcPath = loc2path((ISourceLocation) courseList.get(0));
        
        // TODO: a project may have multiple source paths
        Path libSrcPath = loc2path((ISourceLocation)pcfg.getSrcs().get(0));
        Path destPath = loc2path((ISourceLocation)pcfg.getBin()).resolve("courses");
        
        try {
            CourseCompiler.copyStandardFiles(coursesSrcPath, destPath);

            TutorCommandExecutor executor = new TutorCommandExecutor(pcfg, err, new BasicIDEServices(err));

            String courseName  = getCourseName(pcfg, file, coursesSrcPath);
            if (courseName != null) {
                Onthology ontology = getOntology(file.getProject(), coursesSrcPath, courseName, destPath, libSrcPath, pcfg, executor);

                CourseCompiler.compileCourse(coursesSrcPath, courseName, destPath, libSrcPath, pcfg, executor);
                err.flush();
                //            writeFile(destPath + "/course-compilation-errors.txt", sw.toString());

                System.err.println("Removing intermediate files");

                //        FileVisitor<Path> fileProcessor = new RemoveAdocs();
                //        try {
                //            Files.walkFileTree(destPath, fileProcessor);
                //        } catch (IOException e) {
                //            // TODO: handle file issue (one file failed) with proper error handling mechanism.
                //            System.err.println(e.getMessage());
                //        }

                HtmlDisplay.browse(destPath.resolve("index.html").toFile().toURI().toURL());
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
