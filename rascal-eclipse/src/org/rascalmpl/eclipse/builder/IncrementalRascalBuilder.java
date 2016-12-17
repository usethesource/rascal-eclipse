package org.rascalmpl.eclipse.builder;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.editor.IDEServicesModelProvider;
import org.rascalmpl.eclipse.editor.MessagesToMarkers;
import org.rascalmpl.eclipse.preferences.RascalPreferences;
import org.rascalmpl.eclipse.util.ProjectConfig;
import org.rascalmpl.eclipse.util.RascalEclipseManifest;
import org.rascalmpl.eclipse.util.ResourcesToModules;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.RascalExecutionContext;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.java2rascal.Java2Rascal;
import org.rascalmpl.library.lang.rascal.boot.IKernel;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.uri.ProjectURIResolver;
import org.rascalmpl.value.IConstructor;
import org.rascalmpl.value.ISet;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.value.IValueFactory;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.impulse.builder.MarkerCreator;
import io.usethesource.impulse.runtime.RuntimePlugin;

/** 
 * This builder manages the execution of the Rascal compiler on all Rascal files which have been changed while editing them in Eclipse.
 * It also interacts with Project Clean actions to clear up files and markers on request.  
 */
public class IncrementalRascalBuilder extends IncrementalProjectBuilder {
    // A kernel is 100Mb, so we can't have one for every project; that's why it's static:
    private static IKernel kernel;
	private static PrintWriter out;
    private static PrintWriter err;
    private static IValueFactory vf;
    private static RascalExecutionContext rex;
    private static List<String> binaryExtension = Arrays.asList("imps","rvm.gz", "tc","sig","sigs");
    
    private ISourceLocation projectLoc;
    private PathConfig pathConfig;

    static {
        synchronized(IncrementalRascalBuilder.class){ 
            try {
                out = new PrintWriter(new OutputStreamWriter(RuntimePlugin.getInstance().getConsoleStream(), "UTF16"), true);
                err = new PrintWriter(new OutputStreamWriter(RuntimePlugin.getInstance().getConsoleStream(), "UTF16"), true);
                vf = ValueFactoryFactory.getValueFactory();
                kernel = Java2Rascal.Builder.bridge(vf, new PathConfig(), IKernel.class).build();
            } catch (IOException | URISyntaxException e) {
                Activator.log("could not initialize incremental Rascal builder", e);
            }
        }
    }
    
    public IncrementalRascalBuilder() {
        
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		cleanBinFiles(monitor);
		cleanProblemMarkers(monitor);
	}

    private void cleanProblemMarkers(IProgressMonitor monitor) throws CoreException {
        RascalEclipseManifest manifest = new RascalEclipseManifest();
		 
        for (String src : manifest.getSourceRoots(getProject())) {
            getProject().findMember(src).accept(new IResourceVisitor() {
                @Override
                public boolean visit(IResource resource) throws CoreException {
                    if (IRascalResources.RASCAL_EXT.equals(resource.getFileExtension())) {
                        resource.deleteMarkers(IRascalResources.ID_RASCAL_MARKER, true, IResource.DEPTH_ONE);
                        return false;
                    }
                    
                    return true;
                }
            });
        }
    }

    private void cleanBinFiles(IProgressMonitor monitor) throws CoreException {
        getProject().findMember(ProjectConfig.BIN_FOLDER).accept(new IResourceVisitor() {
            @Override
            public boolean visit(IResource resource) throws CoreException {
                if (binaryExtension.contains(resource.getFileExtension())) {
                    resource.delete(true, monitor);
                    return false;
                }
                
                return true;
            }
        });
    }
	
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
	    switch (kind) {
	    case INCREMENTAL_BUILD:
	    case AUTO_BUILD:
	        buildIncremental(getDelta(getProject()), monitor);
	        break;
	    case FULL_BUILD:
	        buildMain(monitor);
	        break;
	    }
	    
	    // TODO: return project this project depends on?
		return new IProject[0];
	}

	private void buildMain(IProgressMonitor monitor) throws CoreException {
	    IDEServicesModelProvider.getInstance().invalidateEverything();
	    
	    IFile mfFile = getProject().getFile(RascalEclipseManifest.META_INF_RASCAL_MF);
	    
	    if (mfFile == null || !mfFile.exists()) {
	        return; // fine; no meta file so we don't know what to compile.
	    }
	    else {
	        // remove previous markers
	        mfFile.deleteMarkers(IMarker.PROBLEM, true, 1);
	    }
	    
	    RascalEclipseManifest mf = new RascalEclipseManifest();
        String main = mf.getMainModule(getProject());
        
	    if (main == null) {
	        // no main defined in the RASCAL.MF file is fine
	        return;
	    }
	    
	    initializeParameters(false);
	    ISourceLocation module = rex.getPathConfig().resolveModule(main);
	    
	    if (module == null) {
	        // TODO: this should be a marker on RASCAL.MF
	      
            IMarker marker = mfFile.createMarker(IMarker.PROBLEM);
	        marker.setAttribute(IMarker.MESSAGE, "Main module with name " + main + " does not exist.");
	        marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
	        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
	        marker.setAttribute(IMarker.LINE_NUMBER, 1);
	        return;
	    }
	    
	    try {
	        IConstructor result = kernel.compileAndLink(vf.string(main), pathConfig.asConstructor(kernel), kernel.kw_compileAndLink());
            markErrors(module, result);
	    }
	    catch (Throwable e) {
	        Activator.log("error during compilation of " + main, e);
	    }
	    finally {
	        monitor.done();
	    }
    }

    private void buildIncremental(IResourceDelta delta, IProgressMonitor monitor) {
        if (!RascalPreferences.isRascalCompilerEnabled()) {
            return;
        }
        
	    try {
            delta.accept(new IResourceDeltaVisitor() {
                @Override
                public boolean visit(IResourceDelta delta) throws CoreException {
                    IPath path = delta.getProjectRelativePath();
                    
                    if (RascalEclipseManifest.META_INF_RASCAL_MF.equals(path.toPortableString())) {
                        // if the meta information has changed, we need to recompile everything
                        clean(monitor);
                        initializeParameters(true);
                        buildMain(monitor);
                        return false;
                    }
                    else if (IRascalResources.RASCAL_EXT.equals(path.getFileExtension() /* could be null */)) {
                        if ((delta.getFlags() & IResourceDelta.CONTENT) == 0) {
                            // if no content changed, we can bail out now.
                            return false;
                        }
                        
                        ISourceLocation loc = ProjectURIResolver.constructProjectURI(delta.getFullPath());
                        IDEServicesModelProvider.getInstance().clearUseDefCache(loc);
                        
                        monitor.beginTask("Compiling " + loc, 100);
                        try {
                            IFile file = (IFile) delta.getResource();
                            file.deleteMarkers(IMarker.PROBLEM, true, 1);
                            String module = ResourcesToModules.moduleFromFile(file);
                            initializeParameters(false);
                            synchronized (kernel) {
                                IConstructor result = kernel.compile(vf.string(module), pathConfig.asConstructor(kernel), kernel.kw_compile());
                                markErrors(loc, result);
                            }
                        }
                        catch (Throwable e) {
                            Activator.log("Error during compilation of " + loc, e);
                        }
                        finally {
                            monitor.done();
                        }
                        
                        return false;
                    }
                    
                    return !ProjectConfig.BIN_FOLDER.equals(path.toPortableString());
                }
            });
        } catch (CoreException e) {
            Activator.log("error during Rascal compilation", e);
        }
    }
    
    private void markErrors(ISourceLocation loc, IConstructor result) throws MalformedURLException, IOException {
        if (result.has("main_module")) {
            result = (IConstructor) result.get("main_module");
        }
        
        if (!result.has("messages")) {
            Activator.log("Unexpected Rascal compiler result: " + result, new IllegalArgumentException());
        }
        
        new MessagesToMarkers().process(loc, (ISet) result.get("messages"), new MarkerCreator(new ProjectURIResolver().resolveFile(loc)));
    }

    private void initializeParameters(boolean force) throws CoreException {
        if (projectLoc != null && !force) {
            return;
        }
        
        IProject project = getProject();
        projectLoc = ProjectURIResolver.constructProjectURI(project.getFullPath());
        pathConfig = new ProjectConfig(vf).getPathConfig(project);
        rex.getPathConfig().addSourceLoc(PathConfig.getDefaultStd());
    }
}
