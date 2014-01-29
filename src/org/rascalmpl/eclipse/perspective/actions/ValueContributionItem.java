package org.rascalmpl.eclipse.perspective.actions;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.widgets.Menu;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.console.internal.StdAndErrorViewPart;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.JavaToRascal;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.env.Pair;
import org.rascalmpl.interpreter.load.StandardLibraryContributor;
import org.rascalmpl.interpreter.result.AbstractFunction;

public class ValueContributionItem extends ContributionItem {
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
      out = new PrintWriter(new OutputStreamWriter(StdAndErrorViewPart.getStdOut(), "UTF16"));
      err = new PrintWriter(new OutputStreamWriter(StdAndErrorViewPart.getStdErr(), "UTF16"), true);
      eval = new JavaToRascal(out, err).getEvaluator();
      eval.addRascalSearchPathContributor(StandardLibraryContributor.getInstance());
      ProjectEvaluatorFactory.getInstance().configure(eval);
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
  
  @Override
  public void fill(Menu menu, int index) {
    if (eval == null) {
      init();
    }
    
    ModuleEnvironment module = eval.getHeap().getModule(UTIL_VALUE_UI);
    List<Pair<String, List<AbstractFunction>>> functions = module.getFunctions();
    
    for (Pair<String, List<AbstractFunction>> func : functions) {
      final String name = func.getFirst();
      List<AbstractFunction> overloads = func.getSecond();
      
      for (final AbstractFunction f : overloads) {
        if (f.getArity() == 1 && f.getReturnType().isBottom()) {
          Action a = new Action() {
            @Override
            public void run() {
              System.err.println("called " + name);
            } 
          };
          ActionContributionItem aci = new ActionContributionItem(a);
          aci.fill(menu, index);
        }
      }
    }
  }
}
