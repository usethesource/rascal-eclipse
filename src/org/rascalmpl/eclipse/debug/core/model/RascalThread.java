/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.debug.core.model;

import java.util.Stack;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.debug.DebugResumeMode;
import org.rascalmpl.interpreter.debug.IDebugMessage;
import org.rascalmpl.interpreter.debug.IDebugger;
import org.rascalmpl.interpreter.env.Environment;

import static org.rascalmpl.interpreter.debug.DebugMessageFactory.*;

/**
 * A Rascal thread. Rascal programs are currently modelled single threaded.
 */
public class RascalThread extends RascalDebugElement implements IThread, IDebugger, IRascalDebugEventListener {

	/**
	 * Breakpoint this thread is suspended at or <code>null</code>
	 * if none.
	 */
	private IBreakpoint fBreakpoint;
	
	/**
	 * Whether this thread is stepping
	 */
	private boolean fStepping = false;
	
	/**
	 * Wether this thread is suspended
	 */
	private boolean fSuspended = false;
	
	private volatile boolean fTerminated = false;
	
	public RascalThread(IDebugTarget target) {
		super(target);
		getRascalDebugTarget().addEventListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getName()
	 */
	public String getName() throws DebugException {
		return "Rascal Thread";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getPriority()
	 */
	public int getPriority() throws DebugException {
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getBreakpoints()
	 */
	public IBreakpoint[] getBreakpoints() {
		if (fBreakpoint == null) {
			return new IBreakpoint[0];
		}
		return new IBreakpoint[]{fBreakpoint};
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
			theFrames[0] = new RascalStackFrame(this, callStack.get(size-1), currentLoc);
			for (int i = 1; i < size; i++) {
				theFrames[i] = new RascalStackFrame(this, callStack.get(size-i-1), callStack.get(size-i).getCallerLocation());
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return !isSuspended() && !isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return fSuspended && !isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		sendMessage(requestResumption());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		sendMessage(requestSuspension());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepInto()
	 */
	public boolean canStepInto() {
		return !isTerminated() && isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepOver()
	 */
	public boolean canStepOver() {
		return !isTerminated() && isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepReturn()
	 */
	public boolean canStepReturn() {
		return false;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#isStepping()
	 */
	public boolean isStepping() {
		return fStepping;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepInto()
	 */
	public void stepInto() throws DebugException {
		sendMessage(requestStepInto());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		sendMessage(requestStepOver());
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException {
		/** 
		 * not used, see {@link #canStepReturn()} 
		 * */
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate(){
		return !isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated(){
		return fTerminated;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public synchronized void terminate() throws DebugException{
		sendMessage(requestTermination());
	}

	/* (non-Javadoc)
	 * @see org.rascalmpl.interpreter.debug.IDebugger#destroy()
	 */
	@Deprecated
	@Override
	public synchronized void destroy() {
		fTerminated = true;
		RascalDebugTarget rascalDebugTarget = getRascalDebugTarget();
		notify();
		fireTerminateEvent();
		// for refreshing the icons associated to the debug target
		rascalDebugTarget.fireTerminateEvent();
		
	}	
	
	/* (non-Javadoc)
	 * @see org.rascalmpl.interpreter.debug.IDebugger#sendMessage(org.rascalmpl.interpreter.debug.IDebugMessage)
	 */
	@Override
	public void sendMessage(IDebugMessage message) {
		DebugEvent event = null;
		
		switch (message.getSubject()) {

		case START:
			event = new DebugEvent(getRascalDebugTarget(), DebugEvent.CREATE);
			break;
			
		case TERMINATION:
			event = new DebugEvent(getRascalDebugTarget(), DebugEvent.TERMINATE);
			break;
		
		case SUSPENSION:
			switch (message.getDetail()) {
			case BREAKPOINT:
				event = new DebugEvent(getRascalDebugTarget(), DebugEvent.SUSPEND, DebugEvent.BREAKPOINT);
				event.setData(message.getPayload());
				break;
			
			case CLIENT_REQUEST:
				event = new DebugEvent(getRascalDebugTarget(), DebugEvent.SUSPEND, DebugEvent.CLIENT_REQUEST);
				break;

			case STEP_END:
				event = new DebugEvent(getRascalDebugTarget(), DebugEvent.SUSPEND, DebugEvent.STEP_END);
				break;

			default:
				new UnsupportedOperationException("Suspension mode not supported.");
			}
			break;

		case RESUMPTION:
			switch (message.getDetail()) {
			case STEP_INTO:
				event = new DebugEvent(getRascalDebugTarget(), DebugEvent.RESUME, DebugEvent.STEP_INTO);
				break;

			case STEP_OVER:
				event = new DebugEvent(getRascalDebugTarget(), DebugEvent.RESUME, DebugEvent.STEP_OVER);
				break;
				
			case CLIENT_REQUEST:
				event = new DebugEvent(getRascalDebugTarget(), DebugEvent.RESUME, DebugEvent.CLIENT_REQUEST);
				break;

			default:
				new UnsupportedOperationException("Continuation mode not supported.");
			}
			break;		
		}
		
		if (event != null) {
			// TODO: remove simulation of remote events
			getRascalDebugTarget().fRuntimeEvents.add(event);
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.rascalmpl.interpreter.debug.IDebugger#stopStepping()
	 */
	@Deprecated
	@Override
	public void stopStepping() {
		try {
			resume();
		} catch (DebugException e) {
			throw new RuntimeException(e);
		}
	}		
	
	/**
	 * Sets whether this thread is stepping
	 * 
	 * @param stepping whether stepping
	 */
	private void setStepping(boolean stepping) {
		fStepping = stepping;
	}
	
	/**
	 * Sets whether this thread is suspended
	 * 
	 * @param suspended whether suspended
	 */
	private void setSuspended(boolean suspended) {
		fSuspended = suspended;
	}	
	
	/**
	 * Notifies this thread it has been suspended by the given breakpoint.
	 * 
	 * @param breakpoint breakpoint
	 */
	public void suspendedBy(IBreakpoint breakpoint) {
		fBreakpoint = breakpoint;
		suspended(DebugEvent.BREAKPOINT);
	}	
	
	/**
	 * Notification the target has suspended for the given reason
	 * 
	 * @param detail reason for the suspend
	 */
	private void suspended(int detail) {
		fireSuspendEvent(detail);
	}	
	
	/**
	 * Indicates if the reason for suspending this thread
	 * is a breakpoint hit. 
	 * 
	 * @return suspension caused by breakpoint
	 */
	public boolean isSuspendedByBreakpoint() {
		return fBreakpoint != null && isSuspended();
	}

	/**
	 * Notification the target has resumed for the given reason.
	 * Clears any error condition that was last encountered and
	 * fires a resume event.
	 * 
	 * @param detail reason for the resume
	 */	
	public synchronized void resumed(int detail)  {
		fireResumeEvent(detail);
	}

	@Override
	public void onRascalDebugEvent(DebugEvent event) {
		// clear previous state
		fBreakpoint = null;
		setStepping(false);
		
		switch (event.getKind()) {
	
			case DebugEvent.SUSPEND:	
		
				switch (event.getDetail()) {
				case DebugEvent.BREAKPOINT:
				case DebugEvent.CLIENT_REQUEST:
				case DebugEvent.STEP_END:
					setSuspended(true);
					fireSuspendEvent(event.getDetail());
					break;
			
				default:
					new UnsupportedOperationException("Suspension mode not supported.");
				}
				
				break;
			
		case DebugEvent.RESUME:
			setSuspended(false);
			
			switch (event.getDetail()) {
			case DebugEvent.STEP_INTO:
				setStepping(true);
				resumed(event.getDetail());
				break;

			case DebugEvent.STEP_OVER:
				setStepping(true);
				resumed(event.getDetail());
				break;
				
			case DebugEvent.CLIENT_REQUEST:
				setStepping(false);
				resumed(event.getDetail());
				break;

			default:
				new UnsupportedOperationException("Continuation mode not supported.");
			}
			
			break;
			
		}
	}

}
