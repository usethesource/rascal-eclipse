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

import static org.rascalmpl.interpreter.debug.DebugMessageFactory.requestResumption;
import static org.rascalmpl.interpreter.debug.DebugMessageFactory.requestStepInto;
import static org.rascalmpl.interpreter.debug.DebugMessageFactory.requestStepOver;
import static org.rascalmpl.interpreter.debug.DebugMessageFactory.requestSuspension;
import static org.rascalmpl.interpreter.debug.DebugMessageFactory.requestTermination;

import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.IInterpreterEventListener;
import org.rascalmpl.interpreter.InterpreterEvent;
import org.rascalmpl.interpreter.env.Environment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.result.Result;
import org.rascalmpl.uri.URIUtil;

/**
 * A Rascal thread. Rascal programs are currently modelled single threaded.
 */
public class RascalThread extends RascalDebugElement implements IThread, IInterpreterEventListener {

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
	 * Whether this thread is suspended
	 */
	private boolean fSuspended = false;
	
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
			Set<String> imports = callStack.get(size-1).getImports();
			IStackFrame[] theFrames = new IStackFrame[callStack.size()+imports.size()];
			// for the top, use the current AST location
			ISourceLocation currentLoc = eval.getCurrentAST() != null ? 
			    eval.getCurrentAST().getLocation()
			    : URIUtil.rootLocation("stdin");
			theFrames[0] = new RascalStackFrame(this, callStack.get(size-1), currentLoc, null);
			for (int i = 1; i < size; i++) { 
				theFrames[i] = new RascalStackFrame(this, callStack.get(size-i-1), callStack.get(size-i).getCallerLocation(), theFrames[i-1]);
			}
			Environment e = callStack.peek();
			for (String s:imports) {
				ModuleEnvironment module = callStack.peek().getHeap().getModule(s);
				Map<String, Result<IValue>> variables = module.getVariables();
				for (String v:variables.keySet()) {
					 Result<IValue> w = variables.get(v);
				     e.storeVariable(s+"::"+v, w);
				}
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
	  Evaluator eval = getRascalDebugTarget().getConsole().getRascalInterpreter().getEval();
		return isSuspended() || eval.getCurrentEnvt().isRootStackFrame();
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
		sendRequest(requestResumption());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		sendRequest(requestSuspension());
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
		sendRequest(requestStepInto());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		sendRequest(requestStepOver());
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
		return getDebugTarget().isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
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
	public void handleInterpreterEvent(InterpreterEvent event) {
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
