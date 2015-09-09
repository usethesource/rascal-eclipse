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
 *   * Bert Lisser - Bert.Lisser@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.debug.core.model;

import static org.rascalmpl.debug.DebugMessageFactory.requestResumption;
import static org.rascalmpl.debug.DebugMessageFactory.requestStepInto;
import static org.rascalmpl.debug.DebugMessageFactory.requestStepOver;
import static org.rascalmpl.debug.DebugMessageFactory.requestSuspension;
import static org.rascalmpl.debug.DebugMessageFactory.requestTermination;

import java.util.Set;
import java.util.Stack;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.debug.IRascalEventListener;
import org.rascalmpl.debug.IRascalFrame;
import org.rascalmpl.debug.IRascalRuntimeInspection;
import org.rascalmpl.debug.RascalEvent;
import org.rascalmpl.uri.URIUtil;

/**
 * A Rascal thread. Rascal programs are currently modelled single threaded.
 */
public class RascalThread extends RascalDebugElement implements IThread, IRascalEventListener {
	private IBreakpoint fBreakpoint = null;
	private boolean fStepping = false;
	private boolean fSuspended = false;
	
	public RascalThread(IDebugTarget target) {
		super(target);
		getRascalDebugTarget().addEventListener(this);
	}

	@Override
	public String getName() throws DebugException {
		return "Rascal Thread";
	}

	@Override
	public int getPriority() throws DebugException {
		return 0;
	}

	@Override
	public IBreakpoint[] getBreakpoints() {
		if (fBreakpoint == null) {
			return new IBreakpoint[0];
		}
		return new IBreakpoint[]{fBreakpoint};
	}

	@Override
	public IStackFrame[] getStackFrames() throws DebugException {
		if (isSuspended()) {
			IRascalRuntimeInspection eval = getRascalDebugTarget().getEvaluator();
			Stack<IRascalFrame> callStack = eval.getCurrentStack();
			
			int size = callStack.size();
			Set<String> imports = callStack.get(size-1).getImports();
			IStackFrame[] theFrames = new IStackFrame[callStack.size()+imports.size()];
			// for the top, use the current AST location
			ISourceLocation currentLoc = eval.getCurrentPointOfExecution() != null ? 
			    eval.getCurrentPointOfExecution()
			    : URIUtil.rootLocation("stdin");
			theFrames[0] = new RascalStackFrame(this, callStack.get(size-1), currentLoc, null);
			for (int i = 1; i < size; i++) { 
				theFrames[i] = new RascalStackFrame(this, callStack.get(size-i-1), callStack.get(size-i).getCallerLocation(), theFrames[i-1]);
			}
			
			return theFrames;
		}
		return new IStackFrame[0];
	}
	
	@Override
	public IStackFrame getTopStackFrame() throws DebugException {
		IStackFrame[] frames = getStackFrames();
		if (frames.length > 0) {
			return frames[0];
		}
		return null;
	}

	@Override
	public boolean hasStackFrames() throws DebugException {
	    IRascalRuntimeInspection eval = getRascalDebugTarget().getEvaluator();
		return isSuspended() || eval.getTopFrame().getCallerLocation() != null;
	}

	@Override
	public boolean canResume() {
		return isSuspended();
	}

	@Override
	public boolean canSuspend() {
		return !isSuspended() && !isTerminated();
	}

	@Override
	public boolean isSuspended() {
		return fSuspended && !isTerminated();
	}

	@Override
	public void resume() throws DebugException {
		sendRequest(requestResumption());
	}

	@Override
	public void suspend() throws DebugException {
		sendRequest(requestSuspension());
	}

	@Override
	public boolean canStepInto() {
		return !isTerminated() && isSuspended();
	}

	@Override
	public boolean canStepOver() {
		return !isTerminated() && isSuspended();
	}

	@Override
	public boolean canStepReturn() {
		return false;
	}	

	@Override
	public boolean isStepping() {
		return fStepping;
	}

	@Override
	public void stepInto() throws DebugException {
		sendRequest(requestStepInto());
	}

	@Override
	public void stepOver() throws DebugException {
		sendRequest(requestStepOver());
	}
		
	@Override
	public void stepReturn() throws DebugException {
		/** 
		 * not used, see {@link #canStepReturn()} 
		 * */
	}

	@Override
	public boolean canTerminate(){
		return !isTerminated();
	}

	@Override
	public boolean isTerminated(){
		return getDebugTarget().isTerminated();
	}

	@Override
	public synchronized void terminate() throws DebugException{
		sendRequest(requestTermination());
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
	public void handleRascalEvent(RascalEvent event) {
		// clear previous state
		fBreakpoint = null;
		setStepping(false);
		
		switch (event.getKind()) {
			case SUSPEND:	
				setSuspended(true);
		
				switch (event.getDetail()) {
				case BREAKPOINT:
					fireSuspendEvent(DebugEvent.BREAKPOINT);
					break;

				case CLIENT_REQUEST:
					fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
					break;
				
				case STEP_END:
					fireSuspendEvent(DebugEvent.STEP_END);
					break;
				  
				default:
					new UnsupportedOperationException("Suspension mode not supported.");
				}
				
				break;
			
		case RESUME:
			setSuspended(false);
			
			switch (event.getDetail()) {
			case STEP_INTO:
				setStepping(true);
				resumed(DebugEvent.STEP_INTO);
				break;

			case STEP_OVER:
				setStepping(true);
				resumed(DebugEvent.STEP_OVER);
				break;
				
			case CLIENT_REQUEST:
				setStepping(false);
				resumed(DebugEvent.CLIENT_REQUEST);
				break;

			default:
				new UnsupportedOperationException("Continuation mode not supported.");
			}
			
			break;
			
		case CREATE:
		case IDLE:
		    /*
		     * sending a request of resumption to the runtime cleans the state,
		     * if it the last operation before IDLE was a step over or step
		     * into. (JV: but I commented this out to get a variables view for the console frame.)
		     */
		    //      sendRequest(requestResumption());
		    setSuspended(true);
		    fireSuspendEvent(DebugEvent.BREAKPOINT | DebugEvent.STEP_END); // BREAKPOINT is essential to trigger viewer updates
		    break;

		case TERMINATE:
		  setSuspended(true);
		  fireSuspendEvent(DebugEvent.TERMINATE);
		}
	}
}
