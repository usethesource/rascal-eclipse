package org.rascalmpl.eclipse.debug.core.sourcelookup;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.rascalmpl.uri.URIUtil;

public class URISourceContainer implements ISourceContainer {
  private URI fRootFile = null;

  public URISourceContainer(URI root) {
    fRootFile = root;
  }
  
  public URI getURI() {
	  return fRootFile;
  }
  
  public Object[] findSourceElements(String name) throws CoreException {
	  try {
		  URI uri = URIUtil.createFromEncoded(name);
		  URI root = fRootFile;
		  
		  if (root.getScheme().equals(uri.getScheme())
				  && root.getAuthority().equals(uri.getAuthority())
				  && uri.getPath() != null 
				  && uri.getPath().startsWith(root.getPath())) {
			  return new Object[] {new URISourceContainer(uri)};
		  }
		  
		  return new Object[0];
	  } catch (URISyntaxException e) {
		  return new Object[0];
	  }
  }     
  
  public String getName() {   
    return URIUtil.getURIName(fRootFile); 
  }

  public boolean equals(Object obj) {
    if (obj != null && obj instanceof URISourceContainer) {
      URISourceContainer loc = (URISourceContainer) obj;
      return loc.fRootFile.equals(fRootFile);
    } 
    return false;
  } 
  
  public int hashCode() {
    return fRootFile.hashCode();
  }

  public boolean isComposite() {  
    return false;
  }

  @Override
  public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
    return null;
  }

  @Override
  public void init(ISourceLookupDirector director) {
    
  }

  @Override
  public ISourceContainer[] getSourceContainers() throws CoreException {
    return new ISourceContainer[0];
  }

  @Override
  public ISourceContainerType getType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void dispose() {
    // TODO Auto-generated method stub
    
  }
}
