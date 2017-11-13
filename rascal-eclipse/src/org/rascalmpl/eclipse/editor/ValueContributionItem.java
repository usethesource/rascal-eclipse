package org.rascalmpl.eclipse.editor;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.debug.core.model.RascalValue;
import org.rascalmpl.eclipse.debug.core.model.RascalVariable;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.JavaToRascal;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.env.Pair;
import org.rascalmpl.interpreter.load.StandardLibraryContributor;
import org.rascalmpl.interpreter.result.AbstractFunction;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.type.Type;

import io.usethesource.impulse.runtime.RuntimePlugin;

public class ValueContributionItem extends ContributionItem {
  private static final class FunctionAction extends Action {
    private final IValue val;
    private final AbstractFunction f;

    private FunctionAction(String name, IValue val, AbstractFunction f) {
      super(name);
      this.val = val;
      this.f = f;
    }

    @Override
    public void runWithEvent(Event event) {
      try {
        f.call(new Type[] { val.getType() }, new IValue[] { val }, null);
      }
      catch (Throwable e) {
        Activator.log("failed to execute " + getText(), e);
      }
    }
  }

  private static final String UTIL_VALUE_UI = "util::ValueUI";
  private static PrintWriter out;
  private static PrintWriter err;
  private static Evaluator eval;
  
  public ValueContributionItem() {
  }

  public ValueContributionItem(String id) {
    super(id);
  }
  
  private static void init() {
    try {
      out = new PrintWriter(new OutputStreamWriter(RuntimePlugin.getInstance().getConsoleStream(), "UTF16"));
      err = new PrintWriter(new OutputStreamWriter(RuntimePlugin.getInstance().getConsoleStream(), "UTF16"), true);
      eval = new JavaToRascal(out, err).getEvaluator();
      eval.addRascalSearchPathContributor(StandardLibraryContributor.getInstance());
      ProjectEvaluatorFactory.configure(eval, null);
      eval.doImport(null, UTIL_VALUE_UI);
    } catch (UnsupportedEncodingException e) {
      Activator.log("could not init value contributions", e);
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public boolean isDynamic() {
    return true;
  }
  
  private IValue getSelectedValue() {
    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    
    if (window != null) {
        ISelection sel = window.getSelectionService().getSelection();
        
        if (sel instanceof IStructuredSelection) {
          IStructuredSelection selection = (IStructuredSelection) sel;
          Object firstElement = selection.getFirstElement();

          if (firstElement instanceof RascalVariable) {
            return ((RascalVariable) firstElement).getRuntimeValue();
          }
          else if (firstElement instanceof RascalValue) {
            return ((RascalValue) firstElement).getRuntimeValue();
          }
          else if (firstElement instanceof IValue) {
            return (IValue) firstElement;
          }
        }
    }
    
    return null;
  }
  
  @Override
  public void fill(final Menu menu, int index) {
    final IValue val = getSelectedValue();
    
    if (val == null) {
      return;
    }
    
    if (eval == null) {
      init();
    }
    
    ModuleEnvironment module = eval.getHeap().getModule(UTIL_VALUE_UI);
    
    for (Pair<String, List<AbstractFunction>> func : module.getFunctions()) {
      final String name = func.getFirst();
      
      for (final AbstractFunction f : func.getSecond()) {
        if (f.getArity() == 1 
            && f.getReturnType().isBottom()
            && val.getType().isSubtypeOf(f.getFunctionType().getArgumentTypes().getFieldType(0))) {
          IString label = (IString) f.getTag("label");
          Action a = new FunctionAction(label != null ? label.getValue() : name, val, f);
          new ActionContributionItem(a).fill(menu, index);
        };
      }
    }
  }
}
