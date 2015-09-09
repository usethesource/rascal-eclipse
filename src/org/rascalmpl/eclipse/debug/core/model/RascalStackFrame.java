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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.debug.IRascalFrame;
import org.rascalmpl.interpreter.result.IRascalResult;
import org.rascalmpl.uri.URIResolverRegistry;

public class RascalStackFrame extends RascalDebugElement implements IStackFrame {

	/**
	 * The thread that created this stack frame. 
	 */
	private final RascalThread thread;
	
	/**
	 * Location information w.r.t. current execution point within this stack frame.
	 */
	private final ISourceLocation location;

	/**
	 * Gives identity to stack frames that are nested due to recursion
	 */
  private final IStackFrame parent;

  private final IVariable[] variables;

  private final String name; 

	public RascalStackFrame(RascalThread thread, IRascalFrame environment, ISourceLocation location, IStackFrame parent) {
		super(thread.getDebugTarget());
		this.parent = parent;
		this.thread = thread;
		/* need to clone to know previous state, in order to compute model deltas */ 
		this.location = location;
		this.variables = initVariables(environment);
		this.name = environment.getName();
	}
	
	private IVariable[] initVariables(IRascalFrame environment) {
	  //manage the list of variables local to the current module
    Set<String> keys = environment.getFrameVariables();
    List<String> vars = new ArrayList<>(keys.size());
    vars.addAll(keys);
    Collections.sort(vars);

    ArrayList<IVariable> ivars = new ArrayList<>(vars.size() * 2);

    for (String v : vars) {
      IRascalResult var = environment.getFrameVariable(v);
      ivars.add(new RascalVariable(this, v, var.getType(), var.getValue()));
    }
    
    for (String s:environment.getImports()) {
        IRascalFrame module = thread.getRascalDebugTarget().getEvaluator().getModule(s);
        
        if (module != null) {
            Set<String> variables = module.getFrameVariables();
            for (String v:variables) {
                IRascalResult w = module.getFrameVariable(v);
                ivars.add(new RascalVariable(this, s + "::" + v, w.getType(), w.getValue()));
            }
        }
    }
    
    return ivars.toArray(new IVariable[ivars.size()]);
  }

  /* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharEnd()
	 */
	public int getCharEnd() throws DebugException {
		if (location == null) {
			return -1;
		} else {
			return location.getOffset() + location.getLength();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharStart()
	 */
	public int getCharStart() throws DebugException {
		if (location == null) {
			return -1;
		} else {
			return location.getOffset();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getLineNumber()
	 */
	public int getLineNumber() throws DebugException {
		if (location == null || !location.hasLineColumn()) {
			return -1;
		} else {
			return location.getBeginLine();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getName()
	 */
	public String getName() throws DebugException {
	  return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getRegisterGroups()
	 */
	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		return new IRegisterGroup[] {};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getThread()
	 */
	public IThread getThread() {
		return thread;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		return variables;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#hasRegisterGroups()
	 */
	public boolean hasRegisterGroups() throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		return variables.length > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepInto()
	 */
	public boolean canStepInto() {
		return getThread().canStepInto();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepOver()
	 */

	public boolean canStepOver() {
		return getThread().canStepOver();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepReturn()
	 */
	public boolean canStepReturn() {
		return getThread().canStepReturn();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#isStepping()
	 */
	public boolean isStepping() {
		return getThread().isStepping();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepInto()
	 */
	public void stepInto() throws DebugException {
		getThread().stepInto();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		getThread().stepOver();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException {
		getThread().stepReturn();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return getThread().canResume();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return getThread().canSuspend();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return getThread().isSuspended();
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return getThread().canTerminate();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return getThread().isTerminated();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		getThread().terminate();
	}

	/**
	 * Returns if there is a supported source location associated with this stack frame.
	 * 
	 * @return <code>true</code> if there exists a source file name, otherwise <code>false</code>
	 * @see URIResolverRegistry#getResourceURI(java.net.URI)
	 */	
	public boolean hasSourceName() {
		return location != null;
	}
	
	/**
	 * Returns the source file name associated with this stack frame.
	 * 
	 * @return the source file name associated, if existent
	 * @see #hasSourceName()
	 */
	public String getSourceName() {
		assert hasSourceName();
		return location.top().getURI().toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 * used by eclipse to refresh the view 
	 * (see the fireDeltaUpdatingSelectedFrame of the ThreadEventHandler class)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof RascalStackFrame) {
			RascalStackFrame sf = (RascalStackFrame) obj;
			
			return obj == this 
			    || (Arrays.equals(variables, sf.variables)
			    && ((parent == null && sf.parent == null) || (parent != null && sf.parent != null && parent.equals(sf.parent))) 
			    && thread == sf.thread
			    && location.equals(sf.location)
			    ); 
		}
		return false;
	}
	
	@Override
	public int hashCode() {
	  return 3  +  location.hashCode() + ((parent != null) ? 1331 * parent.hashCode() : 1);
	}
}
