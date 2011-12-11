package org.rascalmpl.eclipse.ambidexter;

import org.eclipse.imp.pdb.facts.IValueFactory;
import org.rascalmpl.eclipse.library.util.ValueUI;
import org.rascalmpl.values.ValueFactoryFactory;

public class BrowseTreeAction extends AbstractAmbidexterAction {
	public BrowseTreeAction() {
		setText("Browse");
		setToolTipText("Open a tree browser with the alternative parse trees");
	}
	
	@Override
	public void run() {
		IValueFactory vf = ValueFactoryFactory.getValueFactory();
		new ValueUI(vf).tree(parse());
	}
}
