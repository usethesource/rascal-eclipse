package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IViewPart;
import org.rascalmpl.interpreter.IEvaluator;
import org.rascalmpl.interpreter.result.Result;

public abstract class RascalFunctionOnValueAction extends AbstractIValueAction  {
  private final String module;
  private final String function;

  public RascalFunctionOnValueAction(String module, String function) {
    this.module = module;
    this.function = function;
  }
  
  @Override
  public void run(IAction action) {
    IEvaluator<Result<IValue>> evaluator = getEval().getEvaluator();
    
    if (evaluator.getHeap().getModule(module) == null) {
      evaluator.eval(null, "import " + module + ";", org.rascalmpl.uri.URIUtil.rootScheme("menu"));
    }
    evaluator.call(function, module, null, new IValue[] { getValue() });
  }
  

  @Override
  public void init(IViewPart view) {
    
  }
}
