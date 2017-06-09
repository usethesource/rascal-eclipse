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
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IMap;
import io.usethesource.vallang.INode;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.IWithKeywordParameters;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.uptr.ITree;
import org.rascalmpl.values.uptr.TreeAdapter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class IDEServicesModelProvider {
    private final IValueFactory vf;
    private final IKernel kernel;
    private final Cache<URI, IConstructor> summaryCache;
    
    private IDEServicesModelProvider() {
        try {
            vf = ValueFactoryFactory.getValueFactory();
            kernel = Java2Rascal.Builder.bridge(vf, new PathConfig(), IKernel.class).build();
            summaryCache = Caffeine.newBuilder().weakValues().expireAfterAccess(10, TimeUnit.MINUTES).maximumSize(32_000).build();
        } 
        catch (IOException e) {
            throw new RuntimeException(e);
        }
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
         IConstructor summary = summaryCache.getIfPresent(occ.getURI());
         
         if (summary == null) {
             try {
            	 String moduleName = pcfg.getModuleName(occ);
                 summary = kernel.makeSummary(vf.string(moduleName), pcfg.asConstructor(kernel));
                 if (summary.asWithKeywordParameters().hasParameters()) {
                     // otherwise it is an empty model which we do not 
                     // want to cache.
                     summaryCache.put(occ.getURI(), summary);
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
        return kernel.outline(module);
    }
    
    public IConstructor getPathConfig(IProject project) {
        return new ProjectConfig(vf).getPathConfig(project).asConstructor(kernel);
    }
    
    public void clearSummaryCache(ISourceLocation file) {
        summaryCache.invalidate(file.getURI());
    }

    public void invalidateEverything() {
        summaryCache.invalidateAll();
    }
}
