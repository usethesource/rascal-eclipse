package org.rascalmpl.uri;

import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.value.ISourceLocation;

public class URIResourceResolver {
  
  /**
   * Compute a handle to an Eclipse resource, given a URI, in the context of a project. This uses extensions that implement 
   * the @link {@link IURIResourceResolver} interface to map URI to IResources.
   * 
   * @param uri 
   * @param projectName the context of the URI, can be null
   * @return null if no IURIResourceResolved could resolve the URI to a resource, or an IResource handle.
   */
  public static IResource getResource(ISourceLocation uri) {
    IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint("rascal_eclipse", "uriResolver");

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
        catch (IOException | ClassCastException | CoreException e) {
          Activator.log("exception while resolving " + uri, e);
          continue;
        }
      }
    }
    
    return null;
  }
}
