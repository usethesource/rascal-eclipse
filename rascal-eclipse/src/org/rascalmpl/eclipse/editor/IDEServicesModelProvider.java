package org.rascalmpl.eclipse.editor;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.util.BackgroundInitializer;
import org.rascalmpl.eclipse.util.ProjectConfig;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.values.uptr.IRascalValueFactory;
import org.rascalmpl.values.uptr.ITree;
import org.rascalmpl.values.uptr.TreeAdapter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IMap;
import io.usethesource.vallang.INode;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.IWithKeywordParameters;

public class IDEServicesModelProvider {
    private final IValueFactory vf = IRascalValueFactory.getInstance();
    private final IDESummaryService summaryService;
    
    private final Cache<ISourceLocation, IConstructor> summaryCache;
    private final Cache<ISourceLocation, INode> outlineCache;
    
    private IDEServicesModelProvider() {
            IDESummaryService serviceToUse = getExtensionPointIDESummary();
            if (serviceToUse == null) {
            	// by default, use the the local services 
                serviceToUse = new IDESummaryService() {
                	private final Future<Evaluator> eval = BackgroundInitializer.construct("IDE services evaluator", () -> {
                		Evaluator eval = ProjectEvaluatorFactory.getInstance().getBundleEvaluator(Activator.getInstance().getBundle());
                		eval.doImport(null, "lang::rascal::ide::Outline");
                		return eval;
                	});
                	
                    @Override
                    public IConstructor calculate(IString moduleName, IConstructor pcfg) {
                    	// TODO: include new type checker information here
                        return null;
                    }
                    
                    @Override
                    public INode getOutline(IConstructor moduleTree) {
                        try {
							Evaluator evaluator = eval.get();
							if (evaluator != null) {
								synchronized (evaluator) {
									return (INode) evaluator.call("outline", moduleTree);
								}
							}
						} 
                        catch (InterruptedException | ExecutionException e) {
							Activator.log("outline failed", e);
						}
                        
                        return IRascalValueFactory.getInstance().node("outline failed");
                    }
                };
            }

            summaryService = serviceToUse;
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

	private IDESummaryService getExtensionPointIDESummary() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint("rascal_eclipse", "rascalIDE");
		if (extensionPoint != null) {
			if (extensionPoint.getExtensions().length > 1) {
				Activator.log("multiple IDE summary services registered, not picking any of them", new RuntimeException());
				return null;
			}
			for (IExtension element : extensionPoint.getExtensions()) {
				for (IConfigurationElement cfg : element.getConfigurationElements()) {
					try {
						if (cfg.getAttribute("summaryClass") != null) {
							return (IDESummaryService) cfg.createExecutableExtension("summaryClass");
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
    
    private static class InstanceHolder {
        static IDEServicesModelProvider sInstance = new IDEServicesModelProvider();
    }
    
    public static IDEServicesModelProvider getInstance() {
        return InstanceHolder.sInstance;
    }
    
    @SuppressWarnings("unchecked")
    private <T extends IValue> T get(ISourceLocation occ, PathConfig pcfg, String field, T def) {
       IConstructor summary = getSummary(occ, pcfg);
       
       if (summary != null) {
           IWithKeywordParameters<? extends IConstructor> kws = summary.asWithKeywordParameters();
           if (kws.hasParameters()) {
               T val = (T) kws.getParameter(field);
               
               if (val != null) {
                   return val;
               }
           }
       }
       
       return def;
    }
    
    public void putSummary(ISourceLocation occ, IConstructor summary) {
    	if (summary != null && summary.asWithKeywordParameters().hasParameters()) {
    		summaryCache.put(occ.top(), summary);
    	}
    }
    
    public IConstructor getSummary(ISourceLocation occ, PathConfig pcfg) {
    	return summaryCache.get(occ.top(), (u) -> {
    		try {
    			IConstructor result = summaryService.calculate(vf.string(pcfg.getModuleName(occ)), pcfg.asConstructor());
    			if (result == null || !result.asWithKeywordParameters().hasParameters()) {
    				return null;
    			}
    			return result;
    		}
    		catch (Throwable e) {
    			Activator.log("failure to create summary for IDE features", e);
    			return null;
    		}
    	});
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

    public INode getOutline(IConstructor module) {
    	ISourceLocation loc = getFileLoc((ITree) module);
    	if (loc == null) {
    		return vf.node("");
    	}

    	return outlineCache.get(loc.top(), (l) -> {
    		try {
    			INode result = summaryService.getOutline(module);
    			if (result == null || result.arity() == 0) {
    				return null;
    			}
    			return result;
    		}
    		catch (Throwable e) {
    			Activator.log("failure to create summary for IDE features", e);
    			return null;
    		}
    	});
    }
    
    public void clearSummaryCache(ISourceLocation file) {
        summaryCache.invalidate(file.top());
        outlineCache.invalidate(file.top());
    }

    public void invalidateEverything() {
        summaryCache.invalidateAll();
        outlineCache.invalidateAll();;
    }
    
    
    public PathConfig getPathConfig(IProject prj) {
    	try {
    		if (prj != null) {
    			return new ProjectConfig(IRascalValueFactory.getInstance()).getPathConfig(prj);
    		}
    	} catch (IOException e) {
    		Activator.log("could not create proper path config, defaulting", e);
    	}
    	
    	return null;
    }
}
