package org.rascalmpl.eclipse.builder;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.util.RascalEclipseManifest;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.NoSuchRascalFunction;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.RascalExecutionContext;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.RascalExecutionContextBuilder;
import org.rascalmpl.library.lang.rascal.boot.Kernel;
import org.rascalmpl.uri.ProjectURIResolver;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.value.IConstructor;
import org.rascalmpl.value.IList;
import org.rascalmpl.value.IListWriter;
import org.rascalmpl.value.IMapWriter;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.value.IValueFactory;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.impulse.runtime.RuntimePlugin;

public class IncrementalRascalBuilder extends IncrementalProjectBuilder {
	private final Kernel kernel;
	private final String checkerModuleName = "RascalIDEChecker";
	private final PrintWriter out;
    private final PrintWriter err;
    private final IValueFactory vf;
    
    // compiler config
    private ISourceLocation projectLoc;
    private IList srcPath;
    private ISourceLocation bootDir;
    private IList libPath;
    private ISourceLocation binDir;

    public IncrementalRascalBuilder() throws IOException, NoSuchRascalFunction {
        out = new PrintWriter(new OutputStreamWriter(RuntimePlugin.getInstance().getConsoleStream(), "UTF16"), true);
        err = new PrintWriter(new OutputStreamWriter(RuntimePlugin.getInstance().getConsoleStream(), "UTF16"), true);
        vf = ValueFactoryFactory.getValueFactory();
        
        IMapWriter moduleTags = vf.mapWriter();
        moduleTags.put(vf.string(checkerModuleName), vf.mapWriter().done());
        
	    RascalExecutionContext rex = 
                RascalExecutionContextBuilder.normalContext(vf, out, err)
                    .withModuleTags(moduleTags.done())
                    .forModule(checkerModuleName)
                    .setJVM(true)         
                    .build();
        
        kernel = new Kernel(vf, rex);
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		super.clean(monitor);
	}
	
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
	    switch (kind) {
	    case INCREMENTAL_BUILD:
	    case AUTO_BUILD:
	        buildIncremental(getDelta(getProject()), monitor);
	    case FULL_BUILD:
	        // do nothing we don't know what the main module is and the current compiler would take too long
	    }
	    
	    // TODO: return project this project depends on?
		return new IProject[0];
	}

	private void buildIncremental(IResourceDelta delta, IProgressMonitor monitor) {
	    try {
            delta.accept(new IResourceDeltaVisitor() {
                @Override
                public boolean visit(IResourceDelta delta) throws CoreException {
                    if (delta.getProjectRelativePath().lastSegment().equals("RASCAL.MF")) {
                        initializeParameters();
                        return false;
                    }
                    else if (delta.getProjectRelativePath().getFileExtension().equals(IRascalResources.RASCAL_EXT)) {
                        ISourceLocation loc = ProjectURIResolver.constructProjectURI(delta.getFullPath());
                        monitor.beginTask("Compiling " + loc, 100);
                        try {
                            err.println("Compiling Main for lack of a better module name"); // TODO
                            IConstructor result = kernel.compile(vf.string("Main"), srcPath, libPath, bootDir, binDir, vf.mapWriter().done());
                            err.print(result);
                        }
                        finally {
                            monitor.done();
                        }
                        
                        return false;
                    }
                    
                    return true;
                }
            });
        } catch (CoreException e) {
            Activator.log("error during Rascal compilation", e);
        }
    }
	
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
	        throws CoreException {
	    super.setInitializationData(config, propertyName, data);
	    initializeParameters();
	}

    private void initializeParameters() {
        IProject project = getProject();
        projectLoc = ProjectURIResolver.constructProjectURI(project.getFullPath());
        
        RascalEclipseManifest manifest = new RascalEclipseManifest();
        
        IListWriter libPathWriter = vf.listWriter();
        for (String lib : manifest.getRequiredLibraries(project)) {
            libPathWriter.append(URIUtil.getChildLocation(projectLoc, lib));
        }
        libPath = libPathWriter.done();
        
        IListWriter srcPathWriter = vf.listWriter();
        for (String src : manifest.getSourceRoots(project)) {
            srcPathWriter.append(URIUtil.getChildLocation(projectLoc, src));
        }
        srcPath = srcPathWriter.done();
        
        binDir = URIUtil.getChildLocation(projectLoc, "bin");
        bootDir = URIUtil.correctLocation("boot", "", "");
        manifest.getSourceRoots(project);
    }
}
