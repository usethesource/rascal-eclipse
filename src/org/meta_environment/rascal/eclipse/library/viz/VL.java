package org.meta_environment.rascal.eclipse.library.viz;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.meta_environment.rascal.interpreter.IEvaluatorContext;
import org.meta_environment.rascal.library.experiments.VL.VLPApplet;

public class VL {
	
	public static void render(IConstructor velem, IEvaluatorContext ctx){
		VLPApplet vlp = new VLPApplet(velem, ctx);
		VLViewer.open(vlp);
	}
}
