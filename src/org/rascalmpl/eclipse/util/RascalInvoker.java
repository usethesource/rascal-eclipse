package org.rascalmpl.eclipse.util;

import org.eclipse.swt.widgets.Display;
import org.rascalmpl.interpreter.Evaluator;

public class RascalInvoker{
	
	private RascalInvoker(){
		super();
	}
	
	public static void invokeSync(Runnable runnable, Evaluator evaluator){
		synchronized(evaluator){
			runnable.run();
		}
	}
	
	public static void invokeAsync(final Runnable runnable, final Evaluator evaluator){
		new Thread(new Runnable(){
			public void run(){
				synchronized(evaluator){
					runnable.run();
				}
			}
		}).start();
	}
	
	public static void invokeUISync(Runnable runnable, Evaluator evaluator){
		synchronized(evaluator){
			Display.getDefault().syncExec(runnable);
		}
	}
	
	public static void invokeUIAsync(final Runnable runnable, final Evaluator evaluator){
		new Thread(new Runnable(){
			public void run(){
				synchronized(evaluator){
					Display.getDefault().syncExec(runnable);
				}
			}
		}).start();
	}
}
