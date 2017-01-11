package org.rascalmpl.eclipse.editor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.java2rascal.Java2Rascal;
import org.rascalmpl.library.lang.rascal.boot.IKernel;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.value.IConstructor;
import org.rascalmpl.value.ISet;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.IValueFactory;
import org.rascalmpl.value.IWithKeywordParameters;
import org.rascalmpl.values.ValueFactoryFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class IDEServicesModelProvider {
    private final IValueFactory vf;
    private final IKernel kernel;
    private final Cache<ISourceLocation, IConstructor> useDefCache;
    
    private IDEServicesModelProvider() {
        try {
            vf = ValueFactoryFactory.getValueFactory();
            kernel = Java2Rascal.Builder.bridge(vf, new PathConfig(), IKernel.class).build();
            useDefCache = Caffeine.newBuilder().weakValues().expireAfterAccess(10, TimeUnit.MINUTES).maximumSize(32).build();
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
    private <T extends IValue> T get(ISourceLocation file, PathConfig pcfg, String moduleName, String field, T def) {
       IConstructor summary = getSummary(file, pcfg, moduleName);
       
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
    
    public ISet getUseDef(ISourceLocation file, PathConfig pcfg, String moduleName) {
        return get(file, pcfg, moduleName, "useDef", vf.set());
    }
    
    public IConstructor getSummary(ISourceLocation file, PathConfig pcfg, String moduleName) {
         IConstructor summary = useDefCache.getIfPresent(file);
         
         if (summary == null) {
             try {
                 summary = kernel.makeSummary(vf.string(moduleName), pcfg.asConstructor(kernel));
                 if (summary.asWithKeywordParameters().hasParameters()) {
                     // otherwise it is an empty model which we do not 
                     // want to cache.
                     useDefCache.put(file, summary);
                 }
             }
             catch (Throwable e) {
                 Activator.log("failure to create summary for IDE features", e);
             }
         }
         
         return summary;
     }
    
    public void clearUseDefCache(ISourceLocation file) {
        useDefCache.invalidate(file);
    }


    public void invalidateEverything() {
        useDefCache.invalidateAll();
    }
}
