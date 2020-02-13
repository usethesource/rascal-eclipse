package org.rascalmpl.eclipse.builder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.editor.MessagesToMarkers;
import org.rascalmpl.eclipse.editor.RascalLanguageServices;
import org.rascalmpl.eclipse.preferences.RascalPreferences;
import org.rascalmpl.eclipse.util.ProjectPathConfig;
import org.rascalmpl.eclipse.util.RascalEclipseManifest;
import org.rascalmpl.eclipse.util.RascalProgressMonitor;
import org.rascalmpl.eclipse.util.SchedulingRules;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.uri.ProjectURIResolver;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIResourceResolver;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.ValueFactoryFactory;

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
    private final IValueFactory vf = ValueFactoryFactory.getValueFactory();
    private final List<String> binaryExtension = Arrays.asList("class", "tpl");
    
    private ISourceLocation projectLoc;
    private PathConfig pathConfig;
    
    public IncrementalRascalBuilder() {
        
    }
    
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		cleanBinFiles(monitor);
		cleanProblemMarkers(monitor);
	}
	
	@Override
	public ISchedulingRule getRule(int kind, Map<String, String> args) {
	    if (pathConfig == null) {
	        try {
                initializeParameters(false);
            } catch (CoreException e) {
                Activator.log("failed to initialize builder", e);
            }
	    }
	    
	    if (pathConfig != null) {
	        return URIResourceResolver.getResource(pathConfig.getBin());
	    }
	    else {
	        return SchedulingRules.getRascalProjectBinFolderRule(getProject());
	    }
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
            if (pathConfig == null) {
                try {
                    initializeParameters(false);
                } catch (CoreException e) {
                    Activator.log("failed to initialize builder", e);
                }
            }
            
            IResource binFolder = URIResourceResolver.getResource(pathConfig.getBin());
            
            if (binFolder != null) {
                binFolder.accept(new IResourceVisitor() {
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
    }
	
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
	    IProject project = getProject();
	    
	    try {
	        if (project != null) {
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
	    } finally {
	        monitor.done();
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
	                Activator.log("Source config is not a directory: " + src, new IllegalArgumentException(src.toString()));
	                continue;
	            }

	            // the pathConfig source path currently still contains library sources,
	            // which we want to compile on-demand only:
	            if (src.getScheme().equals("project") && src.getAuthority().equals(projectLoc.getAuthority())) {
	                IList programs = compileAll(monitor, src, pathConfig);

	                if (programs != null) {
	                	markErrors(programs);
	                }
	            }
	        }
	        
	        // TODO: make this invalidate more accurately (scoped by the project that was rebuild)
	        RascalLanguageServices.getInstance().invalidateEverything(); 
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

	private IList compileAll(IProgressMonitor monitor, IList files, PathConfig pcfg) {
	    return RascalLanguageServices.getInstance().compileFileList(new CancelableProgressMonitor(monitor), files, pcfg); 
    }
	
	private IList compileAll(IProgressMonitor monitor, ISourceLocation src, PathConfig pcfg) {
	    return RascalLanguageServices.getInstance().compileFolder(new CancelableProgressMonitor(monitor), src, pcfg);
    }
	
	private final class CancelableProgressMonitor extends RascalProgressMonitor {
        public CancelableProgressMonitor(IProgressMonitor monitor) {
            super(monitor);
        }
	    
	    @Override
	    public boolean isCanceled() {
	        return super.isCanceled() || isInterrupted();
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
            RascalLanguageServices.getInstance().clearSummaryCache(getLocation());
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
            
            return !ProjectPathConfig.BIN_FOLDER.equals(path.toPortableString())
                // if a duplicate bin folder from maven exists, don't recurse into it:
                // this is brittle, but it saves a lot of time waiting for unnecessary compilation:
                && !ProjectPathConfig.MVN_TARGET_FOLDER.equals(path.toPortableString());    
        }
    }

    private class OldBinaryFileRemover implements IResourceDeltaVisitor {
        
        public boolean visit(IResourceDelta delta) throws CoreException {
                IPath path = delta.getProjectRelativePath();

                try {
                    if (IRascalResources.RASCAL_EXT.equals(path.getFileExtension() /* could be null */)) {
                        if ((delta.getKind() & (IResourceDelta.REMOVED | IResourceDelta.MOVED_TO)) != 0) {
                            initializeParameters(false);

                            ISourceLocation module = ProjectURIResolver.constructProjectURI(getProject(), delta.getProjectRelativePath());

                            for (IValue elem : pathConfig.getSrcs()) {
                                ISourceLocation folder = (ISourceLocation) elem;

                                if (module.getPath().startsWith(folder.getPath())) {
                                    String relativePath = module.getPath().substring(folder.getPath().length());

                                    ISourceLocation binFile = URIUtil.getChildLocation(pathConfig.getBin(), relativePath);
                                    binFile = URIUtil.changePath(binFile, binFile.getPath().replace("." + IRascalResources.RASCAL_EXT, ".tpl"));
                                    URIResolverRegistry.getInstance().remove(binFile);
                                }
                            }
                        }
                        
                        return false;
                    }
                } catch (IOException | URISyntaxException e) {
                    Activator.log("could not remove binary file", e);
                    return false;
                }

                return !ProjectPathConfig.BIN_FOLDER.equals(path.toPortableString())
                        // if a duplicate bin folder from maven exists, don't recurse into it:
                        // this is brittle, but it saves a lot of time waiting for unnecessary compilation:
                        && !ProjectPathConfig.MVN_TARGET_FOLDER.equals(path.toPortableString());

        }
    }

    private void buildIncremental(IResourceDelta delta, IProgressMonitor monitor) {
        if (!RascalPreferences.isRascalCompilerEnabled()) {
            return;
        }
        
        if (isRascalBootstrapProject() && !RascalPreferences.bootstrapRascalProject()) {
            return;
        }
        
        removeBinaryFilesForRemovedSourceFiles(delta);
        
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

    private void removeBinaryFilesForRemovedSourceFiles(IResourceDelta delta) {
        try {
            delta.accept(new OldBinaryFileRemover());
        } catch (CoreException e) {
            Activator.log("binary file remover builder failed", e);
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
        		RascalLanguageServices.getInstance().getSummary((ISourceLocation) l, pathConfig);
        	}
        }
	}

	private void buildChangedModules(List<ModuleWork> todo, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Compiling changed Rascal modules", todo.size());
        
        IList locs = getModuleLocations(todo);
        
        try {
            if (!locs.isEmpty()) {
                IList results = compileAll(monitor, locs, pathConfig);

                if (results != null) {
                    markErrors(results);
                }
            }
        } 
        catch (Throwable e) {
            Activator.log("Unexpected error during compilation:" + e.getMessage(), e instanceof ExecutionException ? ((ExecutionException)e).getCause() : e);
        }
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
        if (projectLoc != null && pathConfig != null && !force) {
            return;
        }
        
        IProject project = getProject();
        
        projectLoc = ProjectURIResolver.constructProjectURI(project.getFullPath());
        pathConfig = RascalLanguageServices.getInstance().getPathConfig(project);
    }
}
