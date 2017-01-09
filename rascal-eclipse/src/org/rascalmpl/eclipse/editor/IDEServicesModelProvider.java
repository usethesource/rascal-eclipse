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
import org.rascalmpl.values.ValueFactoryFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class IDEServicesModelProvider {
    private final IValueFactory vf;
    private final IKernel kernel;
    private final Cache<ISourceLocation, IConstructor> summaryCache;
    
    private IDEServicesModelProvider() {
        try {
            vf = ValueFactoryFactory.getValueFactory();
            kernel = Java2Rascal.Builder.bridge(vf, new PathConfig(), IKernel.class).build();
            summaryCache = Caffeine.newBuilder().weakValues().expireAfterAccess(10, TimeUnit.MINUTES).maximumSize(32).build();
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
           IValue parameter = summary.asWithKeywordParameters().getParameter(field);
           if (parameter != null) {
               return (T) parameter;
           }
       }
       
       return def;
    }
    
    public ISet getUseDef(ISourceLocation file, PathConfig pcfg, String moduleName) {
        return get(file, pcfg, moduleName, "uses", vf.set());
    }
    
    public IConstructor getSummary(ISourceLocation file, PathConfig pcfg, String moduleName) {
        return summaryCache.get(file, loc -> {
            synchronized (kernel) {
                try {
                    IConstructor summary = kernel.makeSummary(vf.string(moduleName), pcfg.asConstructor(kernel));
                    
                    return summary.asWithKeywordParameters().hasParameters() ? summary : null;
                }
                catch (Throwable e) {
                    Activator.log("exception during use def lookup", e);
                    return null;
                }
            }
        });
     }
    
    public void clearUseDefCache(ISourceLocation file) {
        summaryCache.invalidate(file);
    }


    public void invalidateEverything() {
        summaryCache.invalidateAll();
    }
}
