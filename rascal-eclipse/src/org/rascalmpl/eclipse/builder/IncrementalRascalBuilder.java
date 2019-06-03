package org.rascalmpl.eclipse.builder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.editor.IDEServicesModelProvider;
import org.rascalmpl.eclipse.editor.MessagesToMarkers;
import org.rascalmpl.eclipse.preferences.RascalPreferences;
import org.rascalmpl.eclipse.util.ProjectConfig;
import org.rascalmpl.eclipse.util.RascalEclipseManifest;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.uri.ProjectURIResolver;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.uptr.IRascalValueFactory;

import io.usethesource.impulse.builder.MarkerCreator;
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
	private static class InstanceHolder {
		private static final BuildRascalService service; 

		static {
			BuildRascalService nonFinalService = getExtensionPointRascalBuilder();
			if (nonFinalService == null) {
				nonFinalService = new BuildRascalService() {
					@Override
					public FutureTask<IList> compile(IList files, IConstructor pcfg) {
						return new FutureTask<>(IRascalValueFactory.getInstance()::list);
					}

					@Override
					public FutureTask<IList> compileAll(ISourceLocation folder, IConstructor pcfg) {
	                    return new FutureTask<>(IRascalValueFactory.getInstance()::list);
					}
				};
			}

			service = nonFinalService;
		}
	}

	private static BuildRascalService getExtensionPointRascalBuilder() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint("rascal_eclipse", "rascalIDE");
		if (extensionPoint != null) {
			if (extensionPoint.getExtensions().length > 1) {
				Activator.log("multiple rascal builder services registered, not picking any of them", new RuntimeException());
				return null;
			}
			for (IExtension element : extensionPoint.getExtensions()) {
				for (IConfigurationElement cfg : element.getConfigurationElements()) {
					try {
						if (cfg.getAttribute("builderClass") != null) {
							return (BuildRascalService) cfg.createExecutableExtension("builderClass");
						}
					}
					catch (ClassCastException | CoreException e) {
						Activator.log("exception while constructing ide service" , e);
					}
				}
			} 
		}
		return null;
	}
    
    private IValueFactory vf = ValueFactoryFactory.getValueFactory();
    private List<String> binaryExtension = Arrays.asList("imps","rvm", "rvmx", "tc","sig","sigs");
    
    private ISourceLocation projectLoc;
    private PathConfig pathConfig;

    public IncrementalRascalBuilder() {
        
    }
    
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		cleanBinFiles(monitor);
		cleanProblemMarkers(monitor);
	}

    private void cleanProblemMarkers(IProgressMonitor monitor) throws CoreException {
        RascalEclipseManifest manifest = new RascalEclipseManifest();
        
        IProject project = getProject();
        
        for (String src : manifest.getSourceRoots(project)) {
            IResource folder = project.findMember(src);
            
            if (folder != null && folder.exists()) {
                folder.accept(new IResourceVisitor() {
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
    }

    private void cleanBinFiles(IProgressMonitor monitor) throws CoreException {
        IProject project = getProject();
        
        if (project != null) {
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
    }
	
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
	    IProject project = getProject();
	    
        if (project != null) {
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
        }

	    // TODO: return project this project depends on?
		return new IProject[0];
	}

	

	
	private void buildWholeProject(IProgressMonitor monitor) throws CoreException {
	    if (!RascalPreferences.isRascalCompilerEnabled()) {
            return;
        }
        
        if (isRascalBootstrapProject() && !RascalPreferences.bootstrapRascalProject()) {
            return;
        }
        
	    
	    initializeParameters(false);
	    
	    try {
	        for (IValue srcv : pathConfig.getSrcs()) {
	            ISourceLocation src = (ISourceLocation) srcv;
	            if (monitor.isCanceled() || isInterrupted()) {
	                return;
	            }

	            if (!URIResolverRegistry.getInstance().isDirectory(src)) {
	                Activator.log("Source config is not a directory?", new IllegalArgumentException(src.toString()));
	                continue;
	            }

	            // the pathConfig source path currently still contains library sources,
	            // which we want to compile on-demand only:
	            if (src.getScheme().equals("project") && src.getAuthority().equals(projectLoc.getAuthority())) {
	                FutureTask<IList> result = InstanceHolder.service.compileAll(src, pathConfig.asConstructor());
	                if (!result.isDone()) {
	                    watchAndCancelTask(result, monitor);
	                    result.run();
	                }
	                IList programs = result.get();
	                if (programs != null) {
	                	markErrors(programs);
	                }
	            }
	        }
	        IDEServicesModelProvider.getInstance().invalidateEverything(); // mark caches as outdated
	        // TODO: make this invalidate more accurate to be scoped by the project that was rebuild
	    }
	    catch (CancellationException e) {
	        // ignore
	    }
	    catch (Throwable e) {
	        Activator.log("error during compilation of project " + projectLoc, e instanceof ExecutionException ? ((ExecutionException)e).getCause() : e);
	    }
	    finally {
	        monitor.done();
	    }
	}

	private final ScheduledExecutorService backgroundCancelation = Executors.newSingleThreadScheduledExecutor();
	
	private void watchAndCancelTask(FutureTask<IList> result, IProgressMonitor monitor) {
	    backgroundCancelation.scheduleAtFixedRate(() -> {
	        if (monitor.isCanceled() || isInterrupted()) {
	            result.cancel(true);
	            throw new RuntimeException("Stop schedule");
	        }
	        else if (result.isDone()) {
	            throw new RuntimeException("Stop schedule");
	        }
	    }, 1, 1, TimeUnit.SECONDS);
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
            IDEServicesModelProvider.getInstance().clearSummaryCache(getLocation());
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
        
        if (isRascalBootstrapProject() && !RascalPreferences.bootstrapRascalProject()) {
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

    private boolean isRascalBootstrapProject() {
        return "rascal".equals(getProject().getName());
    }
    
    private void buildDirty(List<ModuleWork> todo, IProgressMonitor monitor) {
        try {
            initializeParameters(false);
            cleanChangedModulesMarkers(todo, monitor);
            buildChangedModules(todo, monitor);
            cleanChangedModulesUseDefCache(todo, monitor);
            preloadSummaries(todo, monitor);
        } catch (Throwable e) {
            Activator.log("exception during increment Rascal build on " + getProject(), e);
        }
    }

    private void preloadSummaries(List<ModuleWork> todo, IProgressMonitor monitor) {
        IList locs = getModuleLocations(todo);
        monitor.beginTask("Preloading module summary caches", locs.length());
        for (IValue l : locs) {
        	monitor.worked(1);
        	if (l instanceof ISourceLocation) {
        		IDEServicesModelProvider.getInstance().getSummary((ISourceLocation) l, pathConfig);
        	}
        }
	}

	private void buildChangedModules(List<ModuleWork> todo, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Compiling changed Rascal modules", todo.size());
        
        IList locs = getModuleLocations(todo);
        
        try {
            if (!locs.isEmpty()) {
                FutureTask<IList> result = InstanceHolder.service.compile(locs, pathConfig.asConstructor());
                if (!result.isDone()) {
                    watchAndCancelTask(result, monitor);
                    result.run();
                }
                IList results = result.get();
                if (results != null) {
                    markErrors(results);
                }
            }
        } 
        catch (CancellationException e) {
            // ignore
        }
        catch (Throwable e) {
            Activator.log("Unexpected error during compilation:" + e.getMessage(), e instanceof ExecutionException ? ((ExecutionException)e).getCause() : e);
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

    private void cleanChangedModulesMarkers(List<ModuleWork> todo, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Cleaning old errors", todo.size());
        for (ModuleWork mod : todo) {
            mod.deleteMarkers();
        }
        monitor.worked(todo.size());
    }
    
    private void cleanChangedModulesUseDefCache(List<ModuleWork> todo, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Cleaning use-def cache", todo.size());
        for (ModuleWork mod : todo) {
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
        if (!("project".equals(loc.getScheme()))) {
            // ignoring errors outside of projects or synthetic Container module generated by compiler
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
        
        projectLoc = ProjectURIResolver.constructProjectURI(project.getFullPath());
        pathConfig = IDEServicesModelProvider.getInstance().getPathConfig(project);
    }
}
