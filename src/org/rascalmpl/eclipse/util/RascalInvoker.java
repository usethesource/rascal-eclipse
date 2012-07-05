package org.rascalmpl.eclipse.util;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.swt.widgets.Display;
import org.rascalmpl.interpreter.IEvaluator;
import org.rascalmpl.interpreter.result.Result;

public class RascalInvoker{
	
	private RascalInvoker(){
		super();
	}
	
	public static void invokeSync(Runnable runnable, final IEvaluator<Result<IValue>> evaluator){
		if(evaluator == null) throw new IllegalArgumentException("Evaluator can't be null.");
		
		synchronized(evaluator){
			runnable.run();
		}
	}
	
	public static void invokeAsync(final Runnable runnable, final IEvaluator<Result<IValue>> evaluator){
		if(evaluator == null) throw new IllegalArgumentException("Evaluator can't be null.");
		
		new Thread(new Runnable(){
			public void run(){
				synchronized(evaluator){
					runnable.run();
				}
			}
		}).start();
	}
	
	public static void invokeUIAsync(final Runnable runnable, final IEvaluator<Result<IValue>> evaluator){
		if(evaluator == null) throw new IllegalArgumentException("Evaluator can't be null.");
		
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				synchronized (evaluator) {
					runnable.run();
				}
			}
		});
	}
}
