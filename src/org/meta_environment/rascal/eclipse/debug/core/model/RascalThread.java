package org.meta_environment.rascal.eclipse.debug.core.model;


import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleManager;
import org.meta_environment.rascal.eclipse.IRascalResources;
import org.meta_environment.rascal.eclipse.debug.core.breakpoints.RascalExpressionBreakpoint;
import org.meta_environment.rascal.eclipse.debug.core.breakpoints.RascalLineBreakpoint;
import org.meta_environment.rascal.interpreter.IDebugger;
import org.meta_environment.rascal.interpreter.control_exceptions.QuitException;
import org.meta_environment.rascal.interpreter.env.Environment;

public class RascalThread extends RascalDebugElement implements IThread, IDebugger {

	private boolean fStepping = false;
	private boolean fTerminated = false;
	private boolean fSuspended = false;
	private boolean fSuspendedByBreakpoint = false;

	public RascalThread(IDebugTarget target) {
		super(target);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getBreakpoints()
	 */
	public IBreakpoint[] getBreakpoints() {
		return 	getBreakpointManager().getBreakpoints(IRascalResources.ID_RASCAL_DEBUG_MODEL);
	}

	public boolean hasEnabledBreakpoint(ISourceLocation loc)  {
		IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints(IRascalResources.ID_RASCAL_DEBUG_MODEL);
		synchronized (breakpoints) { 
			for (IBreakpoint bp: breakpoints) {
				if (bp instanceof RascalLineBreakpoint) {
					RascalLineBreakpoint b = (RascalLineBreakpoint) bp;
					try {
						if (b.isEnabled()) {
							int l;
							try {
								l = b.getLineNumber();
							} catch (CoreException e) {
								throw new RuntimeException(e);
							}
							//only compare the relative paths from projects
							//TODO: verify that the bp belongs to the same project than the debug target
							//TODO: manage the case where the bp resource or the loc is not in the debug target project
							if (b.getResource().getProjectRelativePath().toString().equals(loc.getURL().getHost()+loc.getURL().getPath())) {
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
		return !isSuspended() && !isTerminated();
	}


	public boolean isSuspended() {
		return (fSuspended || fSuspendedByBreakpoint) && !isTerminated();
	}

	public boolean isSuspendedByBreakpoint() {
		return fSuspendedByBreakpoint && !isTerminated();
	}

	public void resume() throws DebugException {
		fStepping =  false;
		resumed(DebugEvent.CLIENT_REQUEST);	
	}

	public void suspend() {
		getRascalDebugTarget().getEvaluator().suspendRequest();
	}


	public synchronized void notifySuspend() throws QuitException {
		fSuspended = true;
		if (isStepping()) {
			fireSuspendEvent(DebugEvent.STEP_END);
		} else {
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
		}
		try {
			while (isSuspended()) {
				this.wait();
			}
		} catch (InterruptedException e) {
			throw new QuitException();
		}

	}


	public boolean canStepInto() {
		return !isTerminated() && isSuspended();
	}


	public boolean canStepOver() {
		return !isTerminated() && isSuspended();
	}


	public boolean canStepReturn() {
		// TODO Auto-generated method stub
		return false;
	}


	public boolean isStepping() {
		return fStepping;
	}
	
	public void stopStepping() {
		try {
			resume();
		} catch (DebugException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void stepInto() throws DebugException {
		System.out.println("step into");
		fStepping = true;
		resumed(DebugEvent.STEP_INTO);
	}

	public synchronized void resumed(int detail)  {
		// clear the thread state
		fSuspended = false;
		fSuspendedByBreakpoint = false;
		fireResumeEvent(detail);
		// need also to explicitly notify it to the debug target
		// for refreshing the icons associated to the debug target
		// I do not understand why...
		getRascalDebugTarget().fireResumeEvent(detail);
		this.notify();
	}

	public void stepOver() throws DebugException {
		System.out.println("step over");
		fStepping = true;
		getRascalDebugTarget().getEvaluator().setStepOver(true);
		resumed(DebugEvent.STEP_OVER);
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
		//interrupt the Rascal interpreter thread
		Thread executorThread = getRascalDebugTarget().getConsole().getInterpreter().getExecutorThread();
		executorThread.interrupt();
		fTerminated = true;
		fireTerminateEvent();
		// for refreshing the icons associated to the debug target
		getRascalDebugTarget().fireTerminateEvent();
	}

}
