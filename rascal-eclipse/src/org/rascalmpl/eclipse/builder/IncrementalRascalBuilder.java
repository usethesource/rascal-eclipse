package org.rascalmpl.eclipse.builder;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.editor.IDEServicesModelProvider;
import org.rascalmpl.eclipse.editor.MessagesToMarkers;
import org.rascalmpl.eclipse.preferences.RascalPreferences;
import org.rascalmpl.eclipse.util.ProjectConfig;
import org.rascalmpl.eclipse.util.RascalEclipseManifest;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.java2rascal.Java2Rascal;
import org.rascalmpl.library.lang.rascal.boot.IKernel;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.uri.ProjectURIResolver;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.impulse.builder.MarkerCreator;
import io.usethesource.impulse.runtime.RuntimePlugin;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.IListWriter;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;

/** 
 * This builder manages the execution of the Rascal compiler on all Rascal files which have been changed while editing them in Eclipse.
 * It also interacts with Project Clean actions to clear up files and markers on request.  
 */
public class IncrementalRascalBuilder extends IncrementalProjectBuilder {
    private static ThreadLocal<IKernel> kernel = new ThreadLocal<IKernel>() {
        protected IKernel initialValue() {
            try {
                PrintStream out = new PrintStream(RuntimePlugin.getInstance().getConsoleStream());
                PrintStream err = new PrintStream(RuntimePlugin.getInstance().getConsoleStream());
                IValueFactory vf = ValueFactoryFactory.getValueFactory();
                
                Bundle rascalBundle = Activator.getInstance().getBundle();
                URL entry = FileLocator.toFileURL(rascalBundle.getEntry("lib/rascal.jar"));
                ISourceLocation rascalJarLoc = vf.sourceLocation(entry.toURI());
                PathConfig pcfg = new PathConfig()
                        .addJavaCompilerPath(rascalJarLoc)
                        .addClassloader(rascalJarLoc);
                        
                return Java2Rascal.Builder
                        .bridge(vf, pcfg, IKernel.class)
                        .stderr(err)
                        .stdout(out)
                        .build();
            } catch (IOException | URISyntaxException e) {
                Activator.log("could not initialize incremental Rascal builder", e);
                return null;
            }
        }
    };
    
    private IValueFactory vf = ValueFactoryFactory.getValueFactory();
    private List<String> binaryExtension = Arrays.asList("imps","rvm", "rvmx", "tc","sig","sigs");
    
    private ISourceLocation projectLoc;
    private PathConfig pathConfig;

    public IncrementalRascalBuilder() { }

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		cleanBinFiles(monitor);
		cleanProblemMarkers(monitor);
	}

    private void cleanProblemMarkers(IProgressMonitor monitor) throws CoreException {
        RascalEclipseManifest manifest = new RascalEclipseManifest();
        
        IProject project = getProject();
        
        for (String src : manifest.getSourceRoots(project)) {
            project.findMember(src).accept(new IResourceVisitor() {
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
        IProject project = getProject();
        
        project.findMember(ProjectConfig.BIN_FOLDER).accept(new IResourceVisitor() {
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
	    IProject project = getProject();
	    
	    ICoreRunnable runner = new ICoreRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                switch (kind) {
                    case INCREMENTAL_BUILD:
                    case AUTO_BUILD:
                        buildIncremental(getDelta(project), monitor);
                        break;
                    case FULL_BUILD:
                        buildWholeProject(monitor);
                        break;
                }
            }
        };
	    
	    
        project.getWorkspace().run(runner, project,  IWorkspace.AVOID_UPDATE, monitor);

	    // TODO: return project this project depends on?
		return new IProject[0];
	}

	private void buildWholeProject(IProgressMonitor monitor) throws CoreException {
	    IDEServicesModelProvider.getInstance().invalidateEverything();
	    
	    initializeParameters(false);
	    
	    try {
	        for (IValue srcv : pathConfig.getSrcs()) {
	            ISourceLocation src = (ISourceLocation) srcv;

	            if (!URIResolverRegistry.getInstance().isDirectory(src)) {
	                Activator.log("Source config is not a directory?", new IllegalArgumentException(src.toString()));
	                continue;
	            }

	            // the pathConfig source path currently still contains library sources,
	            // which we want to compile on-demand only:
	            if (src.getScheme().equals("project") && src.getAuthority().equals(projectLoc.getAuthority())) {
	                IList programs = kernel.get().compileAll((ISourceLocation) srcv, pathConfig.asConstructor(kernel.get()), kernel.get().kw_compile());
	                markErrors(programs);
	            }
	        }
	    }
	    catch (Throwable e) {
	        Activator.log("error during compilation of project " + projectLoc, e);
	    }
	    finally {
	        monitor.done();
	    }
	}

	private static class ModuleWork {
	    public IFile file;
	    
	    public ModuleWork(IFile file) {
	        this.file = file;
        }
	    
	    public ISourceLocation getLocation() {
	        return ProjectURIResolver.constructProjectURI(file.getFullPath());
	    }
	    
	    public boolean isValidModule() {
	        return getLocation() != null;
	    }

        public void deleteMarkers() throws CoreException {
            file.deleteMarkers(IRascalResources.ID_RASCAL_MARKER, true, IFile.DEPTH_ZERO);
        }

        public void clearUseDefCache() {
            IDEServicesModelProvider.getInstance().clearUseDefCache(getLocation());
        }
	}
	
    private static class WorkCollector implements IResourceDeltaVisitor {
        private boolean metaDataChanged = false;
        private List<ModuleWork> dirty = new LinkedList<>();
        
        /**
         * Analyzes what to do based on a set of changed resources
         * 
         * @param delta the input model of changed resources in Eclipse
         * @param todo  an output parameter which will contain the worklist
         * @return true iff the whole project must be cleaned for some reason
         */
        public static boolean fillWorkList(IResourceDelta delta, List<ModuleWork> todo) {
            assert todo.isEmpty();

            try {
                WorkCollector c = new WorkCollector();
                delta.accept(c);
                todo.addAll(c.dirty);
                return c.metaDataChanged;
            } catch (CoreException e) {
                Activator.log("incremental builder failed", e);
            }
            
            return false;
        }
        
        public boolean visit(IResourceDelta delta) throws CoreException {
            IPath path = delta.getProjectRelativePath();
            
            if (RascalEclipseManifest.META_INF_RASCAL_MF.equals(path.toPortableString())) {
                metaDataChanged = true;
                return false;
            }
            else if (IRascalResources.RASCAL_EXT.equals(path.getFileExtension() /* could be null */)) {
                if ((delta.getFlags() & IResourceDelta.CONTENT) == 0) {
                    return false;
                }
                
                if (delta.getResource() instanceof IFile) {
                    dirty.add(new ModuleWork((IFile) delta.getResource()));
                }
                
                return false;
            }
            
            return !ProjectConfig.BIN_FOLDER.equals(path.toPortableString())
                // if a duplicate bin folder from maven exists, don't recurse into it:
                // this is brittle, but it saves a lot of time waiting for unnecessary compilation:
                && !ProjectConfig.MVN_TARGET_FOLDER.equals(path.toPortableString());    
        }
    }

    private void buildIncremental(IResourceDelta delta, IProgressMonitor monitor) {
        if (!RascalPreferences.isRascalCompilerEnabled()) {
            return;
        }
        
        try {
            List<ModuleWork> todo = new LinkedList<>();

            if (WorkCollector.fillWorkList(delta, todo)) {
                clean(monitor);
                initializeParameters(true);
                buildWholeProject(monitor);
            }
            else {
                buildDirty(todo, monitor);
            }
        } catch (CoreException e) {
            Activator.log("incremental Rascal build failed", e);
        }
    }
    
    private void buildDirty(List<ModuleWork> todo, IProgressMonitor monitor) {
        try {
            initializeParameters(false);
            cleanChangedModules(todo, monitor);
            buildChangedModules(todo, monitor);
        } catch (CoreException e) {
            Activator.log("exception during increment Rascal build on " + getProject(), e);
        }
    }

    private void buildChangedModules(List<ModuleWork> todo, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Compiling changed Rascal modules", todo.size());
        
        IList locs = getModuleLocations(todo);
        
        try {
            if (!locs.isEmpty()) {
                IList results = kernel.get().compile(locs, pathConfig.asConstructor(kernel.get()), kernel.get().kw_compile());
                markErrors(results);
            }
        } 
        catch (IOException e) {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        }
        
        // this shares the locking of the project for efficiency's sake
        monitor.worked(todo.size());
    }

    private IList getModuleLocations(List<ModuleWork> todo) {
        IListWriter w = vf.listWriter();
        
        todo.stream()
        .filter(m -> m.isValidModule())
        .forEach(m -> w.insert(m.getLocation()));
        
        return w.done();
    }

    private void cleanChangedModules(List<ModuleWork> todo, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Cleaning old errors", todo.size());
        for (ModuleWork mod : todo) {
            mod.deleteMarkers();
            mod.clearUseDefCache();
        }
        monitor.worked(todo.size());
    }

    private void markErrors(IList programs) throws MalformedURLException, IOException {
        for (IValue iprogram : programs){
            IConstructor program = (IConstructor) iprogram;
            
            if (program.has("main_module")) {
                program = (IConstructor) program.get("main_module");
            }
            
            if (!program.has("src")) {
               Activator.log("could not get src for errors", new IllegalArgumentException()); 
            }
            
            markErrors((ISourceLocation) program.get("src"), program);
        }
    }
    
    private void markErrors(ISourceLocation loc, IConstructor result) throws MalformedURLException, IOException {
        if (!"project".equals(loc.getScheme())) {
            // ignoring errors outside of projects
            return;
        }
        
        if (result.has("main_module")) {
            result = (IConstructor) result.get("main_module");
        }
        
        if (!result.has("messages")) {
            Activator.log("Unexpected Rascal compiler result: " + result, new IllegalArgumentException());
        }
        
        new MessagesToMarkers().process(loc, (ISet) result.get("messages"), 
                new MarkerCreator(new ProjectURIResolver().resolveFile(loc), IRascalResources.ID_RASCAL_MARKER));
    }

    private void initializeParameters(boolean force) throws CoreException {
        if (projectLoc != null && !force) {
            return;
        }
        
        IProject project = getProject();
        
        // TODO: these should not be fields
        projectLoc = ProjectURIResolver.constructProjectURI(project.getFullPath());
        pathConfig = new ProjectConfig(vf).getPathConfig(project);
    }
}
