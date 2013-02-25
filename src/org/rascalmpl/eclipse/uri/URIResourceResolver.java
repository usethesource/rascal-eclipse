package org.rascalmpl.eclipse.uri;

import java.io.IOException;
import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.rascalmpl.eclipse.Activator;

public class URIResourceResolver {
  
  public static IResource getResource(URI uri) {
    IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint("rascal_eclipse", "uriResolver");

    if (extensionPoint == null) {
      return null;
    }
    
    for (IExtension element : extensionPoint.getExtensions()) {
      for (IConfigurationElement cfg : element.getConfigurationElements()) {
        try {
          if (cfg.getAttribute("scheme").equals(uri.getScheme())) {
            IURIResourceResolver resolver = (IURIResourceResolver) cfg.createExecutableExtension("class");
            IResource res = resolver.getResource(uri);
            if (res != null) {
              return res;
            }
          }
        }
        catch (IOException e) {
          Activator.log("io exception while resolving " + uri, e);
          // try the next applicable resolver anyway
        }
        catch (ClassCastException e) {
          Activator.log("could not load resource " + uri, e);
          // try the next applicable resolver anyway
        }
        catch (CoreException e) {
          Activator.log("could not load resource " + uri, e);
          // try the next applicable resolver anyway
        }
      }
    }
    
    
    return null;
  }
}
