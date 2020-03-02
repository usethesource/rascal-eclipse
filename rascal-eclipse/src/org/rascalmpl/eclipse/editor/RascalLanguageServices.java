package org.rascalmpl.eclipse.editor;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;
import org.rascalmpl.debug.IRascalMonitor;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.util.ProjectPathConfig;
import org.rascalmpl.eclipse.util.ThreadSafeImpulseConsole;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.control_exceptions.InterruptException;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.uptr.IRascalValueFactory;
import org.rascalmpl.values.uptr.ITree;
import org.rascalmpl.values.uptr.TreeAdapter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.IMap;
import io.usethesource.vallang.INode;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.IWithKeywordParameters;

public class RascalLanguageServices {
    private static final IValueFactory vf = IRascalValueFactory.getInstance();
    
    private final Cache<ISourceLocation, IConstructor> summaryCache;
    private final Cache<ISourceLocation, INode> outlineCache;
    
    private final Future<Evaluator> outlineEvaluator = makeFutureEvaluator("Loading Rascal outline evaluator", "lang::rascal::ide::Outline");
    private final Future<Evaluator> summaryEvaluator = makeFutureEvaluator("Loading Rascal summary evaluator", "lang::rascalcore::check::Summary");
    private final Future<Evaluator> compilerEvaluator = makeFutureEvaluator("Loading Rascal compiler evaluator", "lang::rascalcore::check::Checker");
    
    private RascalLanguageServices() {
            summaryCache = Caffeine.newBuilder()
            		.softValues()
            		.maximumSize(256)
            		.expireAfterAccess(60, TimeUnit.MINUTES)
            		.build();

            outlineCache = Caffeine.newBuilder()
            		.softValues()
            		.expireAfterWrite(60, TimeUnit.MINUTES)
            		.maximumSize(512)
            		.build();
    }

    private static class InstanceHolder {
        static RascalLanguageServices sInstance = new RascalLanguageServices();
    }
    
    public static RascalLanguageServices getInstance() {
        return InstanceHolder.sInstance;
    }
    
    private synchronized <T extends IValue> T get(ISourceLocation occ, PathConfig pcfg, String field, T def) {
       IConstructor summary = getSummary(occ, pcfg);
       
       if (summary != null) {
           IWithKeywordParameters<? extends IConstructor> kws = summary.asWithKeywordParameters();
           if (kws.hasParameters()) {
               @SuppressWarnings("unchecked")
               T val = (T) kws.getParameter(field);
               
               if (val != null) {
                   return val;
               }
           }
       }
       
       return def;
    }
    
    public IConstructor getSummary(ISourceLocation occ, PathConfig pcfg) {
    	return summaryCache.get(occ.top(), (u) -> {
    		try {
    		    Evaluator eval = summaryEvaluator.get();
    		    
                if (eval == null) {
                    Activator.log("Could not calculate summary due to missing evaluator", null);
                    return null;
                }
                
                synchronized (eval) {
                    IConstructor result = (IConstructor) eval.call("makeSummary", vf.string(pcfg.getModuleName(occ)), pcfg.asConstructor());

                    return result != null && result.asWithKeywordParameters().hasParameters() ? result : null;
                }
    		}
    		catch (Throwable e) {
    			Activator.log("failure to create summary for IDE features", e);
    			return null;
    		}
    	});
     }
    
    public IList compileFolder(IRascalMonitor monitor, ISourceLocation folder, PathConfig pcfg) {
        try {
            Evaluator eval = compilerEvaluator.get();
            
            synchronized (eval) {
                try {
                    return (IList) eval.call(monitor, "checkAll", folder, pcfg.asConstructor());
                } catch (Throwable e) {
                    Activator.log("compilation failed", e);
                    return IRascalValueFactory.getInstance().list();
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            Activator.log("compilation failed", e);
            return IRascalValueFactory.getInstance().list();
        }
    }
    
    public IList compileFileList(IRascalMonitor monitor, IList files, PathConfig pcfg) {
        try {
            Evaluator eval = compilerEvaluator.get();
           
            synchronized (eval) {
                try {
                    return (IList) eval.call(monitor, "check", files, pcfg.asConstructor());
                }
                catch (InterruptException e) {
                    return IRascalValueFactory.getInstance().list();
                }
                catch (Throwable e) {
                    Activator.log("compilation failed", e);
                    return IRascalValueFactory.getInstance().list();
                }
                finally {
                    eval.__setInterrupt(false);
                }
            }
        } catch (InterruptedException e) {
            Activator.log("compilation failed", e);
            return IRascalValueFactory.getInstance().list();
        } catch (ExecutionException e1) {
            Activator.log("could not find compiler", e1);
            return IRascalValueFactory.getInstance().list();
        }
    }
    
    // TODO to be removed, rewrite HyperlinkDetector
    public ISet getUseDef(ISourceLocation file, PathConfig pcfg, String moduleName) {
        return get(file, pcfg, "useDef", vf.set());
    }
    
    public IString getType(ISourceLocation occ, PathConfig pcfg){
    	IMap locationTypes = get(occ, pcfg, "locationTypes", vf.mapWriter().done());
    	return (IString) locationTypes.get(occ);
    }
    
    public ISet getDefs(ISourceLocation occ, PathConfig pcfg) {
        ISet useDefs = get(occ, pcfg, "useDef", vf.set());
        return useDefs.asRelation().index(occ);
    }
    
    public IString getSynopsis(ISourceLocation occ, PathConfig pcfg) {
        IMap synopses = get(occ, pcfg, "synopses", vf.mapWriter().done());
        return (IString) synopses.get(occ);
    }
    
    public ISourceLocation getDocLoc(ISourceLocation occ, PathConfig pcfg) {
        IMap docLocs = get(occ, pcfg, "docLocs", vf.mapWriter().done());
        return (ISourceLocation) docLocs.get(occ);
    }
    
    private ISourceLocation getFileLoc(ITree moduleTree) {
    	try {
    		if (TreeAdapter.isTop(moduleTree)) {
    			moduleTree = TreeAdapter.getStartTop(moduleTree);
    		}
    		ISourceLocation loc = TreeAdapter.getLocation(moduleTree);
    		if (loc != null) {
    			return loc.top();
    		}
    		return null;
    	} catch (Throwable t) {
    		return null;
    	}
    }
    
    private final static INode EMPTY_NODE = vf.node("");

    private INode replaceNull(@Nullable INode result) {
    	return result == null ? EMPTY_NODE : result;
	}

    public INode getOutline(IConstructor module) {
        synchronized (outlineCache) {
            ISourceLocation loc = getFileLoc((ITree) module);
            if (loc == null) {
                return EMPTY_NODE;
            }

            return replaceNull(outlineCache.get(loc.top(), (l) -> {
                try {
                    Evaluator eval = outlineEvaluator.get();

                    if (eval == null) {
                        Activator.log("Could not calculate outline due to missing evaluator", null);
                        return null;
                    }

                    synchronized (eval) {
                        return (INode) eval.call("outline", module);
                    }
                }
                catch (Throwable e) {
                    Activator.log("failure to create outline", e);
                    return null;
                }
            }));
        }
    }

	public synchronized void clearSummaryCache(ISourceLocation file) {
        summaryCache.invalidate(file.top());
        outlineCache.invalidate(file.top());
    }

    public synchronized void invalidateEverything() {
        summaryCache.invalidateAll();
        outlineCache.invalidateAll();;
    }
    
    public PathConfig getPathConfig(IProject prj) {
        if (prj != null && prj.isOpen()) {
            return new ProjectPathConfig(IRascalValueFactory.getInstance()).getPathConfig(prj);
        }
    	
    	return new PathConfig();
    }
    
    public PathConfig getModulePathConfig(ISourceLocation module) {
        if (module.getScheme().equals("project")) {
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(module.getAuthority());
            return getPathConfig(project);
        }
        else if (module.getScheme().equals("lib")) {
            try {
                return PathConfig.fromLibraryRascalManifest(module.getAuthority());
            }
            catch (IOException e) {
                Activator.log("could not configure compiler for " + module, e);
            }
        }

        return new PathConfig();
    }
    
    private Future<Evaluator> makeFutureEvaluator(String label, final String... imports) {
        return asyncGenerator(label, () ->  {
            Bundle bundle = Platform.getBundle("rascal_eclipse");
            Evaluator eval = ProjectEvaluatorFactory.getInstance().getBundleEvaluator(bundle, ThreadSafeImpulseConsole.INSTANCE.getWriter(), ThreadSafeImpulseConsole.INSTANCE.getWriter());
           
            eval.addRascalSearchPath(URIUtil.correctLocation("jar+plugin", "rascal_eclipse", "/lib/typepal.jar!/"));
            eval.addRascalSearchPath(URIUtil.correctLocation("jar+plugin", "rascal_eclipse", "/lib/rascal-core.jar!/"));
            
            for (String i : imports) {
                try {
                    eval.doImport(eval, i);
                }
                catch (Throwable e) {
                    Activator.log("failed to import " + i, e);
                }
            }
           
            return eval;
        });
    }
    
    private static <T> Future<T> asyncGenerator(String name, Callable<T> generate) {
        FutureTask<T> result = new FutureTask<>(() -> {
            try {
                return generate.call();
            } catch (Throwable e) {
                Activator.log("Cannot initialize " + name, e);
                return null;
            }
        });
        
        Job job = Job.create(name, new ICoreRunnable() {
            @Override
            public void run(IProgressMonitor monitor) {
                result.run();
            }
        });
        
        job.schedule();
        
        return result;
    }

   
}
