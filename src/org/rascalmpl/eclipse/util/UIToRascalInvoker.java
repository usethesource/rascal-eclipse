package org.rascalmpl.eclipse.util;

import org.eclipse.swt.widgets.Display;
import org.rascalmpl.interpreter.Evaluator;

public class UIToRascalInvoker{
	private final static NotifiableLock LOCK = new NotifiableLock();
	private final static DoubleRotatingQueue<Runnable, Evaluator> QUEUE = new DoubleRotatingQueue<Runnable, Evaluator>();
	
	private static Worker worker;
	
	private UIToRascalInvoker(){
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
	
	private static class Worker implements Runnable{
		private volatile boolean running = true;
		
		public Worker(){
			super();
		}
		
		public void run(){
			do{
				Runnable r = null;
				Evaluator evaluator = null;
				do{
					synchronized(QUEUE){
						if(!QUEUE.isEmpty()){
							r = QUEUE.peekFirstUnsafe();
							evaluator = QUEUE.getSecondUnsafe();
						}
					}
					
					LOCK.block();
					
					if(!running) return;
				}while(r == null);
				
				synchronized(evaluator){
					Display.getDefault().syncExec(r);
				}
			}while(running);
		}
		
		public void stop(){
			running = false;
			LOCK.wakeUp();
		}
	}
	
	public synchronized static void initialize(){
		if(worker != null) return;
		worker = new Worker();
		Thread workerThread = new Thread(worker);
		workerThread.start();
	}
	
	public static void terminate(){
		if(worker != null) worker.stop();
	}
	
	public static void invokeAsync(Runnable runnable, Evaluator evaluator){
		synchronized(QUEUE){
			QUEUE.put(runnable, evaluator);
		}
		LOCK.wakeUp();
	}
}
