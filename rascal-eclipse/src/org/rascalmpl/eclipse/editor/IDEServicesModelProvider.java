package org.rascalmpl.eclipse.editor;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.util.ProjectConfig;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.java2rascal.Java2Rascal;
import org.rascalmpl.library.lang.rascal.boot.IKernel;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.uptr.IRascalValueFactory;

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
    private final IKernel kernel;
    private final Cache<URI, IConstructor> summaryCache;
    
    private IDEServicesModelProvider() {
        try {
            kernel = Java2Rascal.Builder.bridge(vf, new PathConfig(), IKernel.class).build();
            IDESummaryService serviceToUse = getExtensionPointIDESummary();
            if (serviceToUse == null) {
            	// by default, use the kernel 
                serviceToUse = new IDESummaryService() {
                    @Override
                    public IConstructor calculate(IKernel kernel, IString moduleName, IConstructor pcfg) {
                        return kernel.makeSummary(moduleName, pcfg);
                    }
                    
                    @Override
                    public INode getOutline(IKernel kernel, IConstructor moduleTree) {
                        return kernel.outline(moduleTree);
                    }
                };
            }

            summaryService = serviceToUse;
            summaryCache = Caffeine.newBuilder()
            		.weakValues()
            		.maximumSize(32_000)
            		.expireAfterAccess(10, TimeUnit.MINUTES)
            		.build();
        } 
        catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    		summaryCache.put(occ.getURI(), summary);
    	}
    }
    
    public IConstructor getSummary(ISourceLocation occ, PathConfig pcfg) {
         IConstructor summary = summaryCache.getIfPresent(occ.getURI());
         
         if (summary == null) {
             try {
            	 summary = summaryService.calculate(kernel, vf.string(pcfg.getModuleName(occ)), pcfg.asConstructor(kernel));
                 if (summary != null && summary.asWithKeywordParameters().hasParameters()) {
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
        return summaryService.getOutline(kernel, module);
    }
    
    public IConstructor getPathConfigCons(IProject prj) {
        return getPathConfig(prj).asConstructor(kernel);
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
        summaryCache.invalidate(file.getURI());
    }

    public void invalidateEverything() {
        summaryCache.invalidateAll();
    }
}
