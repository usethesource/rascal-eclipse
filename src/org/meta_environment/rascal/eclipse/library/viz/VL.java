package org.meta_environment.rascal.eclipse.library.viz;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.meta_environment.rascal.interpreter.IEvaluatorContext;
import org.meta_environment.rascal.library.experiments.VL.VLPApplet;

public class VL {
	
	public VL(IValueFactory values){
		super();
	}
	
	public void render(IConstructor velem, IEvaluatorContext ctx){
		VLPApplet vlp = new VLPApplet(velem, ctx);
		VLViewer.open(vlp);
	}
}
