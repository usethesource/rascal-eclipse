package org.rascalmpl.eclipse.editor;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IProject;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.util.ProjectConfig;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.java2rascal.Java2Rascal;
import org.rascalmpl.library.lang.rascal.boot.IKernel;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.values.ValueFactoryFactory;

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
    private final IValueFactory vf;
    private final ThreadLocal<IKernel> kernel;
    private final ThreadLocal<Cache<URI, IConstructor>> summaryCache;
    
    private IDEServicesModelProvider() {
        vf = ValueFactoryFactory.getValueFactory();
        kernel = new ThreadLocal<IKernel>() {
            @Override
            protected IKernel initialValue() {
                try {
                    return Java2Rascal.Builder.bridge(vf, new PathConfig(), IKernel.class).build();
                } catch (IOException e) {
                    Activator.log("Could not initialize kernel for Rascal IDE services on this thread.", e);
                    return null;
                }
            }
            //                Java2Rascal.Builder.bridge(vf, new PathConfig(), IKernel.class).build();
        };
        
        summaryCache = new ThreadLocal<Cache<URI, IConstructor>>() {
            @Override
            protected Cache<URI, IConstructor> initialValue() {
                return Caffeine.newBuilder().weakValues().expireAfterAccess(10, TimeUnit.MINUTES).maximumSize(32_000).build();
            }
        };
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
    
    public IConstructor getSummary(ISourceLocation occ, PathConfig pcfg) {
        Cache<URI, IConstructor> cache = summaryCache.get();
        IConstructor summary = cache.getIfPresent(occ.getURI());
        
        if (summary == null) {
            try {
                String moduleName = pcfg.getModuleName(occ);
                summary = kernel.get().makeSummary(vf.string(moduleName), pcfg.asConstructor(kernel.get()));
                if (summary.asWithKeywordParameters().hasParameters()) {
                    // otherwise it is an empty model which we do not 
                    // want to cache.
                    cache.put(occ.getURI(), summary);
                }
            }
            catch (Throwable e) {
                Activator.log("failure to create summary for IDE features", e);
            }
        }

        return summary;
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
    
    public INode getOutline(IConstructor module) {
        return kernel.get().outline(module);
    }
    
    public IConstructor getPathConfigCons(IProject prj) {
        return getPathConfig(prj).asConstructor(kernel.get());
    }
    
    public PathConfig getPathConfig(IProject prj) {
        try {
            if (prj != null) {
              return new ProjectConfig(ValueFactoryFactory.getValueFactory()).getPathConfig(prj);
            }
        } catch (IOException e) {
            Activator.log("could not create proper path config, defaulting", e);
        }
        
        return new PathConfig();
    }
    
    public void clearSummaryCache(ISourceLocation file) {
        summaryCache.get().invalidate(file.getURI());
    }

    public void invalidateEverything() {
        summaryCache.get().invalidateAll();
    }
}
