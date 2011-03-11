package org.rascalmpl.eclipse.library.visdeprecated;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;

public class Basic {

	public Basic(IValueFactory values) {
		super();
	}

	// Various views



	public void boxView(IValue v) {
		BoxViewer.display(v);
	}

}
