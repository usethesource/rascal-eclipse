package org.rascalmpl.eclipse.ambidexter;

import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.library.util.ValueUI;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.value.IValueFactory;
import org.rascalmpl.values.ValueFactoryFactory;

public class DiagnoseAction extends AbstractAmbidexterAction {
	public DiagnoseAction() {
		setText("Diagnose");
		setToolTipText("Experimental feature to diagnose ambiguity");
	}
	
	@Override
	public void run() {
		try {
			IValueFactory vf = ValueFactoryFactory.getValueFactory();
			new ValueUI(vf).text(getEvaluator().diagnoseAmbiguity(null, getTree()), vf.integer(2));
		}
		catch (Throw t) {
			Activator.log("internal error while diagnosing ambiguity:" + t, t);
		}
		catch (Throwable t) {
			Activator.log("internal error while diagnosing ambiguity", t);
		}
	}
}
