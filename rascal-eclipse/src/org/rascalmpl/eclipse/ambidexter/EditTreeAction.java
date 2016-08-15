package org.rascalmpl.eclipse.ambidexter;

import org.rascalmpl.eclipse.library.util.ValueUI;
import org.rascalmpl.value.IValueFactory;
import org.rascalmpl.values.ValueFactoryFactory;

public class EditTreeAction extends AbstractAmbidexterAction {
	public EditTreeAction() {
		setText("Trees");
		setToolTipText("Open an editor and show the parse trees");
	}
	
	@Override
	public void run() {
		IValueFactory vf = ValueFactoryFactory.getValueFactory();
		new ValueUI(vf).text(getTree(), vf.integer(2));
	}
}
