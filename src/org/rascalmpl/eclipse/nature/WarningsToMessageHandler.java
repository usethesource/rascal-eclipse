package org.rascalmpl.eclipse.nature;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.pdb.facts.ISourceLocation;

public class WarningsToMessageHandler implements IWarningHandler {
  private final ISourceLocation uri;
  private final IMessageHandler handler;

  public WarningsToMessageHandler(ISourceLocation uri, IMessageHandler handler) {
    this.uri = uri;
    this.handler = handler;
  }
  
  @Override
  public void warning(String msg, ISourceLocation src) {
    if (src.top().isEqual(uri.top())) {

      Map<String,Object> attrs = new HashMap<String,Object>();
      attrs.put(IMarker.SEVERITY, IStatus.WARNING);
      attrs.put(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);

      handler.handleSimpleMessage(msg, src.getOffset(), src.getOffset() + src.getLength(), src.getBeginColumn(), src.getEndColumn(), src.getBeginLine(), src.getEndLine(), attrs);
    }
  }
}
