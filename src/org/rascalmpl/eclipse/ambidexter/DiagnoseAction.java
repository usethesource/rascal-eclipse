package org.rascalmpl.eclipse.ambidexter;

import org.eclipse.imp.pdb.facts.IValueFactory;
import org.rascalmpl.eclipse.library.util.ValueUI;
import org.rascalmpl.values.ValueFactoryFactory;

public class DiagnoseAction extends AbstractAmbidexterAction {
	public DiagnoseAction() {
		setText("Diagnose");
		setToolTipText("Experimental feature to diagnose ambiguity");
	}
	
	@Override
	public void run() {
		IValueFactory vf = ValueFactoryFactory.getValueFactory();
		new ValueUI(vf).text(getEvaluator().diagnoseAmbiguity(null, parse()), vf.integer(2));
	}
}
