package org.rascalmpl.eclipse.debug.core.sourcelookup;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.rascalmpl.eclipse.uri.URIStorage;
import org.rascalmpl.uri.URIUtil;

public class URISourceContainer implements ISourceContainer {
  private URIStorage fRootFile = null;

  public URISourceContainer(URIStorage root) {
    fRootFile = root;
  }
  
  public URIStorage getStorage() {
	  return fRootFile;
  }
  
  public Object[] findSourceElements(String name) throws CoreException {
	  try {
		  URI uri = URIUtil.createFromEncoded(name);
		  URI root = fRootFile.getURI();
		  
		  if (root.getScheme().equals(uri.getScheme())
				  && root.getAuthority().equals(uri.getAuthority())
				  && uri.getPath() != null 
				  && uri.getPath().startsWith(root.getPath())) {
			  URIStorage storage = new URIStorage(fRootFile.getRegistry(), uri, false);
			  return new Object[] {new URISourceContainer(storage)};
		  }
		  
		  return new Object[0];
	  } catch (URISyntaxException e) {
		  return new Object[0];
	  }
  }     
  
  public String getName() {   
    return fRootFile.getName(); 
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
