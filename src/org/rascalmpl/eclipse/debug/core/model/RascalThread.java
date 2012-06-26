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

import java.net.URI;
import java.util.Stack;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.debug.core.breakpoints.RascalExpressionBreakpoint;
import org.rascalmpl.eclipse.debug.core.breakpoints.RascalLineBreakpoint;
import org.rascalmpl.eclipse.uri.ProjectURIResolver;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.control_exceptions.QuitException;
import org.rascalmpl.interpreter.debug.DebugStepMode;
import org.rascalmpl.interpreter.debug.DebugSuspendMode;
import org.rascalmpl.interpreter.debug.IDebugger;
import org.rascalmpl.interpreter.env.Environment;

/**
 * A Rascal thread. Rascal programs are currently modelled single threaded.
 */
public class RascalThread extends RascalDebugElement implements IThread, IDebugger {

	private boolean fStepping = false;
	private volatile boolean fTerminated = false;
	private boolean fSuspended = false;
	private boolean fSuspendedByBreakpoint = false;

	public RascalThread(IDebugTarget target) {
		super(target);
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
		return 	getBreakpointManager().getBreakpoints(IRascalResources.ID_RASCAL_DEBUG_MODEL);
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
		return (fSuspended || fSuspendedByBreakpoint) && !isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		fStepping =  false;
		resumed(DebugEvent.CLIENT_REQUEST);	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() {
		getRascalDebugTarget().getEvaluator().suspendRequest();
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
		fStepping = true;
		getRascalDebugTarget().getEvaluator().setStepMode(DebugStepMode.STEP_INTO);
		resumed(DebugEvent.STEP_INTO);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		fStepping = true;
		getRascalDebugTarget().getEvaluator().setStepMode(DebugStepMode.STEP_OVER);
		resumed(DebugEvent.STEP_OVER);
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
		RascalDebugTarget rascalDebugTarget = getRascalDebugTarget();
		rascalDebugTarget.getConsole().terminate();
	}

	/* (non-Javadoc)
	 * @see org.rascalmpl.interpreter.debug.IDebugger#destroy()
	 */
	public synchronized void destroy() {
		fTerminated = true;
		RascalDebugTarget rascalDebugTarget = getRascalDebugTarget();
		notify();
		fireTerminateEvent();
		// for refreshing the icons associated to the debug target
		rascalDebugTarget.fireTerminateEvent();
		
	}

	/* (non-Javadoc)
	 * @see org.rascalmpl.interpreter.debug.IDebugger#notifySuspend(org.rascalmpl.interpreter.debug.DebugSuspendMode)
	 */
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
	
	/* (non-Javadoc)
	 * @see org.rascalmpl.interpreter.debug.IDebugger#stopStepping()
	 */
	public void stopStepping() {
		try {
			resume();
		} catch (DebugException e) {
			throw new RuntimeException(e);
		}
	}	
	
	/* (non-Javadoc)
	 * @see org.rascalmpl.interpreter.debug.IDebugger#hasEnabledBreakpoint(org.eclipse.imp.pdb.facts.ISourceLocation)
	 */
	public boolean hasEnabledBreakpoint(ISourceLocation loc){
		IBreakpoint[] breakpoints = getBreakpoints();
		synchronized(breakpoints){ 
			for(IBreakpoint bp: breakpoints){
				if(bp instanceof RascalLineBreakpoint){
					RascalLineBreakpoint b = (RascalLineBreakpoint) bp;
					try{
						if(b.isEnabled()){
							/*
							 * Location information of the breakpoint (in format
							 * {@link IPath}) and location information of the
							 * source location (in format {@link
							 * ISourceLocation}) are both transformed to {@link
							 * URI} instances of schmema type "project".
							 */
							URI uriBreakPointLocation = ProjectURIResolver.constructNonEncodedProjectURI(b.getResource().getFullPath());
							URI uriSourceLocation     = getRascalDebugTarget().getDebuggableURIResolverRegistry().getResourceURI(loc.getURI());
							
							if (uriBreakPointLocation.equals(uriSourceLocation)) {
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
					}catch(Exception e){
						throw new RuntimeException(e);
					}
				}
			}
		}
		return false;
	}
		
	public boolean isSuspendedByBreakpoint() {
		return fSuspendedByBreakpoint && !isTerminated();
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
	
}
