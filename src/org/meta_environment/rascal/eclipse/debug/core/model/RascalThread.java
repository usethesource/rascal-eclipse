package org.meta_environment.rascal.eclipse.debug.core.model;

import java.util.Stack;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleManager;

import org.meta_environment.rascal.interpreter.IDebugger;
import org.meta_environment.rascal.interpreter.env.Environment;

public class RascalThread extends RascalDebugElement implements IThread, IDebugger {

	private boolean fStepping = false;
	private boolean fTerminated = false;
	private boolean fSuspended = false;


	public RascalThread(IDebugTarget target) {
		super(target);
	}

	public IBreakpoint[] getBreakpoints() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() throws DebugException {
		return "Rascal";
	}

	public int getPriority() throws DebugException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getStackFrames()
	 */
	public IStackFrame[] getStackFrames() throws DebugException {
		if (isSuspended()) {
			Stack<Environment> callStack = getRascalDebugTarget().getConsole().getInterpreter().getEval().getCallStack();
			int size = callStack.size();
			IStackFrame[] theFrames = new IStackFrame[size];
			for (int i = 0; i < size; i++) {
				theFrames[i] = new RascalStackFrame(getRascalDebugTarget(),callStack.get(size-i-1));
			}
			return theFrames;
		}
		return new IStackFrame[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getTopStackFrame()
	 */
	public IStackFrame getTopStackFrame() throws DebugException {
		IStackFrame[] frames = getStackFrames();
		if (frames.length > 0) {
			return frames[0];
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#hasStackFrames()
	 */
	public boolean hasStackFrames() throws DebugException {
		return isSuspended();
	}


	public boolean canResume() {
		return isSuspended();
	}


	public boolean canSuspend() {
		return !isSuspended();
	}


	public boolean isSuspended() {
		return fSuspended && !isTerminated();
	}


	public void resume() throws DebugException {
		System.out.println("resume");
		setStepping(false);
		fSuspended = false;
		synchronized (this) {
			this.notify();
		}
		fireResumeEvent(DebugEvent.RESUME);
	}



	public void suspend() {
		fSuspended = true;
		setStepping(true);
	}


	public void notifySuspend() {
		System.out.println("suspend");
		synchronized (this) {
			fSuspended = true;
			fireSuspendEvent(DebugEvent.SUSPEND);
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}


	public boolean canStepInto() {
		return !isTerminated() && isSuspended();
	}


	public boolean canStepOver() {
		// TODO Auto-generated method stub
		return false;
	}


	public boolean canStepReturn() {
		// TODO Auto-generated method stub
		return false;
	}


	public boolean isStepping() {
		return fStepping;
	}


	public void stepInto() throws DebugException {
		System.out.println("step");	
		fSuspended = false;
		synchronized (this) {
			this.notify();
		}
		fireChangeEvent(DebugEvent.STEP_INTO);
	}



	public void stepOver() throws DebugException {
		// TODO Auto-generated method stub
	}


	public void stepReturn() throws DebugException {
		// TODO Auto-generated method stub

	}


	public boolean canTerminate() {
		return !isTerminated();
	}


	public boolean isTerminated() {
		return fTerminated;
	}


	public void terminate() throws DebugException {
		IConsoleManager fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
		fConsoleManager.removeConsoles(new org.eclipse.ui.console.IConsole[]{getRascalDebugTarget().getConsole()});		
		fTerminated = true;
		fireTerminateEvent();
	}


	/**
	 * Sets whether this thread is stepping
	 * 
	 * @param stepping whether stepping
	 */
	public void setStepping(boolean stepping) {
		fStepping = stepping;
	}

}
