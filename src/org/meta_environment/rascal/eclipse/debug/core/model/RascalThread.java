package org.meta_environment.rascal.eclipse.debug.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.LineBreakpoint;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleManager;
import org.meta_environment.rascal.eclipse.debug.core.breakpoints.RascalExpressionBreakpoint;
import org.meta_environment.rascal.eclipse.debug.core.breakpoints.RascalLineBreakpoint;
import org.meta_environment.rascal.interpreter.DebuggableEvaluator;
import org.meta_environment.rascal.interpreter.IDebugger;
import org.meta_environment.rascal.interpreter.env.Environment;

public class RascalThread extends RascalDebugElement implements IThread, IDebugger {

	private boolean fStepping = false;
	private boolean fTerminated = false;
	private boolean fSuspended = false;
	private boolean fSuspendedByBreakpoint = false;
	private List<RascalLineBreakpoint> lineBreakpoints;

	public RascalThread(IDebugTarget target) {
		super(target);
		lineBreakpoints = new ArrayList<RascalLineBreakpoint>();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getBreakpoints()
	 */
	public IBreakpoint[] getBreakpoints() {
		return lineBreakpoints.toArray(new LineBreakpoint[]{});
	}

	public boolean hasEnabledBreakpoint(ISourceLocation loc)  {
		synchronized (lineBreakpoints) { 
			for (RascalLineBreakpoint b: lineBreakpoints) {
				try {
					if (b.isEnabled()) {
						int l;
						try {
							l = b.getLineNumber();
						} catch (CoreException e) {
							throw new RuntimeException(e);
						}

						if (b.getResource().getName().equals(loc.getURL().getHost())) {
							// special case for expression breakpoints
							if (b instanceof RascalExpressionBreakpoint) {
								if (b.getCharStart() <= loc.getOffset() && loc.getOffset()+loc.getLength() <= b.getCharEnd()) {
									//TODO: avoid side effect
									fSuspendedByBreakpoint = true;
									return true;
								}
							} else if (l==loc.getBeginLine()) {
								//TODO: avoid side effect
								fSuspendedByBreakpoint = true;
								return true;
							}

						}
					}
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
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
			// for the top, use the current AST location
			ISourceLocation currentLoc = getRascalDebugTarget().getConsole().getInterpreter().getEval().getCurrentAST().getLocation();
			theFrames[0] = new RascalStackFrame(getRascalDebugTarget(), callStack.get(size-1), currentLoc);
			for (int i = 1; i < size; i++) {
				theFrames[i] = new RascalStackFrame(getRascalDebugTarget(),callStack.get(size-i-1), callStack.get(size-i).getCallerLocation());
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

	public boolean isSuspendedByBreakpoint() {
		return fSuspendedByBreakpoint && !isTerminated();
	}

	public void resume() throws DebugException {
		setStepping(false);
		resumed(DebugEvent.CLIENT_REQUEST);
		synchronized (this) {
			this.notify();
		}
	}



	public void suspend() {
		fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
		((DebuggableEvaluator) getRascalDebugTarget().getConsole().getInterpreter().getEval()).suspendRequest();
		//setStepping(true);
	}


	public void notifySuspend() {
		synchronized (this) {
			fSuspended = true;
			if (isStepping()) {
				fireSuspendEvent(DebugEvent.STEP_END);
			}
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
		System.out.println("step into");
		setStepping(true);
		resumed(DebugEvent.STEP_INTO);
		synchronized (this) {
			this.notify();
		}
	}

	public void resumed(int detail)  {
		fSuspended = false;
		fSuspendedByBreakpoint = false;
		fireResumeEvent(detail);
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
		//stop the thread of the interpreter
		Thread executorThread = getRascalDebugTarget().getConsole().getInterpreter().getExecutorThread();
		executorThread.interrupt();
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

	public void removeBreakpoint(RascalLineBreakpoint rascalLineBreakpoint) {
		lineBreakpoints.remove(rascalLineBreakpoint);
	}

	public void addBreakpoint(RascalLineBreakpoint rascalLineBreakpoint) {
		lineBreakpoints.add(rascalLineBreakpoint);
	}

	public void restoreBreakpoints(IBreakpoint[] breakpoints) {
		lineBreakpoints.clear();
		for (int i = 0; i < breakpoints.length; i++) {
			if (breakpoints[i] instanceof RascalLineBreakpoint) {
				lineBreakpoints.add((RascalLineBreakpoint)breakpoints[i]);
			}
		}
	}
}
