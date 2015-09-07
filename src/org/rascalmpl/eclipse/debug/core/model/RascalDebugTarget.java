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

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.rascalmpl.eclipse.debug.core.breakpoints.RascalSourceLocationBreakpoint;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.IInterpreterEventListener;
import org.rascalmpl.interpreter.IInterpreterEventTrigger;
import org.rascalmpl.interpreter.InterpreterEvent;
import org.rascalmpl.interpreter.debug.IDebugMessage;
import org.rascalmpl.interpreter.debug.IDebugSupport;


/**
 *  Rascal Debug Target
 */
public class RascalDebugTarget extends RascalDebugElement implements IDebugTarget, IBreakpointManagerListener, IInterpreterEventListener {

	// containing launch object
	private final ILaunch fLaunch;

	// associated interpreter event trigger to receive 
	// notifications from the runtime
	private IInterpreterEventTrigger fInterpreterEventTrigger;
	
	// associated debug support interface
	private final IDebugSupport fDebugSupport;
	
	// terminated state
	private boolean fTerminated = false;	
	
	// threads
	private IThread[] fThreads;
	private RascalThread fThread;

    private final Evaluator fEvaluator;
	

	/**
	 * Constructs a new debug target in the given launch for the 
	 * associated Rascal console.
	 * 
	 * @param console Rascal console
	 * @exception CoreException if unable to connect to host
	 */
	public RascalDebugTarget(Evaluator eval, ILaunch launch, IInterpreterEventTrigger eventTrigger, IDebugSupport debugSupport) throws CoreException {
		super(null);

		fEvaluator = eval;
		fInterpreterEventTrigger = eventTrigger;
		fDebugSupport = debugSupport;
		addEventListener(this);
		
		fLaunch = launch;
		fThread = new RascalThread(this);
		fThreads = new IThread[]{fThread};

		IBreakpointManager breakpointManager = getBreakpointManager();
		breakpointManager.addBreakpointListener(this);
		breakpointManager.addBreakpointManagerListener(this);
	}
	
	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getProcess()
	 */	
	public IProcess getProcess() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getThreads()
	 */	
	public IThread[] getThreads() throws DebugException {
		return fThreads;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#hasThreads()
	 */
	public boolean hasThreads() throws DebugException {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getName()
	 */
	public String getName() throws DebugException {
		return "Rascal";
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#supportsBreakpoint(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		return breakpoint instanceof RascalSourceLocationBreakpoint;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return this;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fLaunch;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return !isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return fTerminated;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		getThread().terminate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return !isTerminated() && isSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return !isTerminated() && !isSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return !isTerminated() && getThread().isSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		getThread().resume();
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		getThread().suspend();
	}

	/**
	 * When the breakpoint manager disables, remove all registered breakpoints
	 * requests from the VM. When it enables, reinstall them.
	 */
	public void breakpointManagerEnablementChanged(boolean enabled) {
		IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints(getModelIdentifier());
		
		for(IBreakpoint breakpoint : breakpoints) {
			if (enabled) {
				breakpointAdded(breakpoint);
			} else {
				breakpointRemoved(breakpoint, null);
			}			
		}
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
		if (supportsBreakpoint(breakpoint)) {
			try {
				if ((breakpoint.isEnabled() && getBreakpointManager().isEnabled()) || !breakpoint.isRegistered()) {
					RascalSourceLocationBreakpoint rascalBreakpoint = (RascalSourceLocationBreakpoint)breakpoint;
					rascalBreakpoint.install(this);
				}
			} catch (CoreException e) {
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		if (supportsBreakpoint(breakpoint)) {
			try {
				RascalSourceLocationBreakpoint rascalBreakpoint = (RascalSourceLocationBreakpoint)breakpoint;
				rascalBreakpoint.remove(this);
			} catch (CoreException e) {
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#canDisconnect()
	 * 
	 * Feature <em>disconnect</em> is not supported currently.
	 */
	public boolean canDisconnect() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#disconnect()
	 *
	 * Feature <em>disconnect</em> is not supported currently.
	 */
	public void disconnect() throws DebugException {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#isDisconnected()
	 *
	 * Feature <em>disconnect</em> is not supported currently.
	 */
	public boolean isDisconnected() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval()
	 * 
	 * Feature <em>memory block retrieval</em> is not supported currently.
	 */
	public boolean supportsStorageRetrieval() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long, long)
	 *
	 * Feature <em>memory block retrieval</em> is not supported currently.
	 */
	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
		return null;
	}

	/**
	 * Returns this debug target's single thread, or <code>null</code>
	 * if terminated.
	 * 
	 * @return this debug target's single thread, or <code>null</code>
	 * if terminated
	 */
	public synchronized RascalThread getThread() {
		return fThread;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		if (supportsBreakpoint(breakpoint)) {
			try {
				if (breakpoint.isEnabled() && getBreakpointManager().isEnabled()) {
					breakpointAdded(breakpoint);
				} else {
					breakpointRemoved(breakpoint, null);
				}
			} catch (CoreException e) {
			}
		}
	}	
	
	/* (non-Javadoc)
	 * @see org.rascalmpl.eclipse.debug.core.model.RascalDebugElement#sendRequest(org.rascalmpl.interpreter.debug.IDebugMessage)
	 */
	public void sendRequest(IDebugMessage message) {
		fDebugSupport.processMessage(message);
	}
			
	/**
	 * Registers the given event listener. The listener will be notified of
	 * events in the program being interpreted. Has no effect if the listener
	 * is already registered.
	 *  
	 * @param listener event listener
	 */
	public void addEventListener(IInterpreterEventListener listener) {
		fInterpreterEventTrigger.addInterpreterEventListener(listener);
	}
	
	/**
	 * Deregisters the given event listener. Has no effect if the listener is
	 * not currently registered.
	 *  
	 * @param listener event listener
	 */
	public void removeEventListener(IInterpreterEventListener listener) {
		fInterpreterEventTrigger.removeInterpreterEventListener(listener);
	}
	
	/**
	 * Notification we have connected to the VM and it has started.
	 * Resume the VM.
	 */
	private void started() {
		fireCreationEvent();
		installDeferredBreakpoints();
		try {
			resume();
		} catch (DebugException e) {
		}
	}
	
	/**
	 * Install breakpoints that are already registered with the breakpoint
	 * manager.
	 */
	private void installDeferredBreakpoints() {
		IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints(getModelIdentifier());
		for (IBreakpoint bp : breakpoints) {
			breakpointAdded(bp);
		}
	}
	
	/**
	 * Called when this debug target terminates.
	 */
	private synchronized void terminated() {
		fTerminated = true;
		fThread = null;
		fThreads = new IThread[0];
		IBreakpointManager breakpointManager = getBreakpointManager();
        breakpointManager.removeBreakpointListener(this);
		breakpointManager.removeBreakpointManagerListener(this);
		fireTerminateEvent();
		removeEventListener(this);
	}

	@Override
	public void handleInterpreterEvent(InterpreterEvent event) {
	  switch (event.getKind()) {
	  case CREATE:
	    started();
	    break;

	  case TERMINATE:
	    terminated();
	    break;

	  default:
	    break;
	  }
	}



    public Evaluator getEvaluator() {
        return fEvaluator;
    }
	
}
