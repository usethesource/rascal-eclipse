package org.rascalmpl.eclipse.nature;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.uri.URIResourceResolver;
import org.rascalmpl.value.ISourceLocation;

import io.usethesource.impulse.builder.MarkerCreator;

public class WarningsToMarkers implements IWarningHandler {

  @Override
  public void warning(String msg, ISourceLocation src) {
    try {
      IResource res = URIResourceResolver.getResource(src);

      if (res != null) {
    	  Map<String,Object> attrs = new HashMap<String,Object>();
    	  attrs.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
    	  attrs.put(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);

    	  if (res instanceof IFile) {
    		  new MarkerCreator((IFile) res, IRascalResources.ID_RASCAL_MARKER).handleSimpleMessage(msg, src.getOffset(), src.getOffset() + src.getLength(), src.getBeginColumn(), src.getEndColumn(), src.getBeginLine(), src.getEndLine(), attrs);
    	  }
      }
    }
    catch (Throwable e) {
      // handling error messages should be very robust 
      Activator.log("could not handle warning message: " + msg, e);
    }
  }

}
