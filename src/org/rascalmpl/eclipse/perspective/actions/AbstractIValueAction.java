package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.rascalmpl.eclipse.debug.core.model.RascalDebugTarget;
import org.rascalmpl.eclipse.debug.core.model.RascalValue;
import org.rascalmpl.eclipse.debug.core.model.RascalVariable;
import org.rascalmpl.interpreter.IEvaluatorContext;

public abstract class AbstractIValueAction  implements IViewActionDelegate, IObjectActionDelegate, IActionDelegate{
  private IValue value;
  private IEvaluatorContext eval;

  protected IValue getValue() {
    return value;
  }
  
  protected IEvaluatorContext getEval() {
    return eval;
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection sel = (IStructuredSelection) selection;
      Object elem = sel.getFirstElement();
      
      if (elem instanceof RascalValue) {
        this.value = ((RascalValue) elem).getRuntimeValue();
        IDebugTarget target = ((RascalValue) elem).getDebugTarget();
        
        if (target instanceof RascalDebugTarget) {
          this.eval = ((RascalDebugTarget) target).getEvaluator();
        }
      }
      else if (elem instanceof RascalVariable) {
        this.value = ((RascalVariable) elem).getRuntimeValue();
        
        IDebugTarget target = ((RascalVariable) elem).getDebugTarget();
        
        if (target instanceof RascalDebugTarget) {
          this.eval = ((RascalDebugTarget) target).getEvaluator();
        }
      }
    }
  }
  
  @Override
  public void init(IViewPart view) {  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {  }
}