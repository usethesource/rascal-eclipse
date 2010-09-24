package org.rascalmpl.eclipse.debug.core.model;

import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.console.ConsoleFactory.IRascalConsole;
import org.rascalmpl.eclipse.debug.core.breakpoints.RascalExpressionBreakpoint;
import org.rascalmpl.eclipse.debug.core.breakpoints.RascalLineBreakpoint;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.control_exceptions.QuitException;
import org.rascalmpl.interpreter.debug.DebugStepMode;
import org.rascalmpl.interpreter.debug.DebugSuspendMode;
import org.rascalmpl.interpreter.debug.IDebugger;
import org.rascalmpl.interpreter.env.Environment;

public class RascalThread extends RascalDebugElement implements IThread, IDebugger {

	private boolean fStepping = false;
	private volatile boolean fTerminated = false;
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

	public boolean hasEnabledBreakpoint(ISourceLocation loc){
		IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints(IRascalResources.ID_RASCAL_DEBUG_MODEL);
		synchronized(breakpoints){ 
			for(IBreakpoint bp: breakpoints){
				if(bp instanceof RascalLineBreakpoint){
					RascalLineBreakpoint b = (RascalLineBreakpoint) bp;
					try{
						if(b.isEnabled()){
							//only compare the relative paths from src folders
							String bp_path = b.getResource().getProjectRelativePath().toString().replaceFirst(IRascalResources.RASCAL_SRC, "");
							String loc_path = loc.getURI().getPath();
							if (bp_path.equals(loc_path)) {
								// special case for expression breakpoints
								if(b instanceof RascalExpressionBreakpoint){
									if(b.getCharStart() <= loc.getOffset() && loc.getOffset()+loc.getLength() <= b.getCharEnd()){
										return true;
									}
								}else if(b.getCharStart() == loc.getOffset()){
									return true;
								}
							}
						}
					}catch(CoreException e){
						throw new RuntimeException(e);
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
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getStackFrames()
	 */
	public IStackFrame[] getStackFrames() throws DebugException {
		if (isSuspended()) {
			Evaluator eval = getRascalDebugTarget().getConsole().getRascalInterpreter().getEval();
			Stack<Environment> callStack = eval.getCallStack();
			int size = callStack.size();
			IStackFrame[] theFrames = new IStackFrame[size];
			// for the top, use the current AST location
			ISourceLocation currentLoc = eval.getCurrentAST().getLocation();
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


	public synchronized void notifySuspend(DebugSuspendMode mode) throws QuitException {
		fSuspended = true;

		switch (mode) {
		case BREAKPOINT:
			fSuspendedByBreakpoint = true;
			fireSuspendEvent(DebugEvent.BREAKPOINT);
			break;

		case CLIENT_REQUEST:
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			break;

		case STEP_END:
			fireSuspendEvent(DebugEvent.STEP_END);
			break;

		default:
			break;
		}

		while (isSuspended()) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				// Ignore
			}

			if(isTerminated()) throw new QuitException();
		}
	}


	public boolean canStepInto() {
		return !isTerminated() && isSuspended();
	}


	public boolean canStepOver() {
		return !isTerminated() && isSuspended();
	}


	public boolean canStepReturn() {
		return false;
	}


	public boolean isStepping() {
		return fStepping;
	}

	public void stopStepping() {
		try {
			resume();
		} catch (DebugException e) {
			throw new RuntimeException(e);
		}
	}


	public void stepInto() throws DebugException {
		fStepping = true;
		getRascalDebugTarget().getEvaluator().setStepMode(DebugStepMode.STEP_INTO);
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
		fStepping = true;
		getRascalDebugTarget().getEvaluator().setStepMode(DebugStepMode.STEP_OVER);
		resumed(DebugEvent.STEP_OVER);
	}

	public void stepReturn() throws DebugException {
		//TODO: not implemented
	}

	public boolean canTerminate(){
		return !isTerminated();
	}

	public boolean isTerminated(){
		return fTerminated;
	}

	public synchronized void destroy() {
		fTerminated = true;
		RascalDebugTarget rascalDebugTarget = getRascalDebugTarget();
		notify();
		fireTerminateEvent();
		// for refreshing the icons associated to the debug target
		rascalDebugTarget.fireTerminateEvent();

	}

	public synchronized void terminate() throws DebugException{
		RascalDebugTarget rascalDebugTarget = getRascalDebugTarget();
		rascalDebugTarget.getConsole().terminate();
	}

}
