package org.rascalmpl.eclipse.util;

import org.eclipse.swt.widgets.Display;
import org.rascalmpl.interpreter.Evaluator;

public class RascalInvoker{
	private final static NotifiableLock uiWorkerLock = new NotifiableLock();
	private final static DoubleRotatingQueue<Runnable, Evaluator> uiWorkerQueue = new DoubleRotatingQueue<Runnable, Evaluator>();
	private static UIWorker uiWorker;
	
	private RascalInvoker(){
		super();
	}
	
	private static class NotifiableLock{
		private boolean notified = false;
		
		public synchronized void block(){
			while(!notified){
				try{
					wait();
				}catch(InterruptedException irex){
					// Ignore.
				}
			}
			notified = false;
		}
		
		public synchronized void wakeUp(){
			notified = true;
			notify();
		}
	}
	private static class UIWorker implements Runnable{
		private volatile boolean running = true;
		
		public UIWorker(){
			super();
		}
		
		public void run(){
			do{
				Runnable r = null;
				Evaluator evaluator = null;
				do{
					synchronized(uiWorkerQueue){
						if(!uiWorkerQueue.isEmpty()){
							r = uiWorkerQueue.peekFirstUnsafe();
							evaluator = uiWorkerQueue.getSecondUnsafe();
						}
					}
					
					if(r == null) uiWorkerLock.block();
					
					if(!running) return;
				}while(r == null);
				
				synchronized(evaluator){
					Display.getDefault().syncExec(r);
				}
			}while(running);
		}
		
		public void stop(){
			running = false;
			uiWorkerLock.wakeUp();
		}
	}
	
	public synchronized static void initialize(){
		if(uiWorker != null) return;
		
		uiWorker = new UIWorker();
		Thread uiWorkerThread = new Thread(uiWorker);
		uiWorkerThread.start();
	}
	
	public static void terminate(){
		if(uiWorker != null) uiWorker.stop();
	}
	
	public static void invokeUIAsync(Runnable runnable, Evaluator evaluator){
		if(runnable == null || evaluator == null) throw new IllegalArgumentException();
		
		synchronized(uiWorkerQueue){
			uiWorkerQueue.put(runnable, evaluator);
		}
		uiWorkerLock.wakeUp();
	}
	
	public static void invokeAsync(final Runnable runnable, final Evaluator evaluator){
		if(runnable == null || evaluator == null) throw new IllegalArgumentException();
		
		new Thread(new Runnable(){
			public void run(){
				synchronized(evaluator){
					runnable.run();
				}
			}
		}).start();
	}
	
	public static void invokeSync(Runnable runnable, Evaluator evaluator){
		if(runnable == null || evaluator == null) throw new IllegalArgumentException();
		
		synchronized(evaluator){
			runnable.run();
		}
	}
}
