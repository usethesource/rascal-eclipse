package org.rascalmpl.eclipse.editor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.java2rascal.Java2Rascal;
import org.rascalmpl.library.lang.rascal.boot.IKernel;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.value.ISet;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.value.IValueFactory;
import org.rascalmpl.values.ValueFactoryFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class IDEServicesModelProvider {
    private final IValueFactory vf;
    private final IKernel kernel;
    private final Cache<ISourceLocation, ISet> useDefCache;
    
    private IDEServicesModelProvider() {
        try {
            vf = ValueFactoryFactory.getValueFactory();
            kernel = Java2Rascal.Builder.bridge(vf, new PathConfig(), IKernel.class).build();
            useDefCache = Caffeine.newBuilder().weakValues().expireAfterAccess(10, TimeUnit.MINUTES).maximumSize(32).build();
        } 
        catch (IOException | URISyntaxException  e) {
            throw new RuntimeException(e);
        }
    }
    
    private static class InstanceHolder {
        static IDEServicesModelProvider sInstance = new IDEServicesModelProvider();
    }
    
    public static IDEServicesModelProvider getInstance() {
        return InstanceHolder.sInstance;
    }
    
    
    public ISet getUseDef(ISourceLocation file, PathConfig pcfg, String moduleName) {
       return useDefCache.get(file, loc -> {
           synchronized (kernel) {
               try {
                   return (ISet) kernel.makeSummary(vf.string(moduleName), pcfg.asConstructor(kernel)).get("useDef");
               }
               catch (Throwable e) {
                   Activator.log("exception during use def lookup", e);
                   return vf.set();
               }
           }
       });
    }
    
    public void clearUseDefCache(ISourceLocation file) {
        useDefCache.invalidate(file);
    }


    public void invalidateEverything() {
        useDefCache.invalidateAll();
    }
}
