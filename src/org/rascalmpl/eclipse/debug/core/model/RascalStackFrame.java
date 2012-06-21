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

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.interpreter.env.Environment;
import org.rascalmpl.interpreter.env.Pair;
import org.rascalmpl.interpreter.result.AbstractFunction;

public class RascalStackFrame extends RascalDebugElement implements IStackFrame {

	private final RascalThread fThread;
	
	/**
	 * Environment corresponding to the current scope.
	 */
	private final Environment environment;
	
	/**
	 * Location of the call.
	 */
	private final ISourceLocation location; 

	public RascalStackFrame(RascalThread thread, Environment environment, ISourceLocation location) {
		super(thread.getDebugTarget());
		
		this.fThread = thread;
		this.environment = environment;
		this.location = location;
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
		if (location == null) {
			return -1;
		} else {
			return location.getBeginLine();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getName()
	 */
	public String getName() throws DebugException {
		//TODO: return the name of the current module
		return environment.getName();
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
		return fThread;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		//manage the list of variables local to the current module
		Set<String> vars = environment.getVariables().keySet();
		//manage the list of imported modules
		Set<String> modules = environment.getImports();

		IVariable[] ivars = new IVariable[vars.size()+modules.size()];
		int i = 0;
		for (String m : modules) {
			ivars[i] = new RascalImportedModule(this, m);
			i++;
		}
		for (String v : vars) {
			ivars[i] = new RascalVariable(this, v);
			i++;
		}
		return ivars;
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
		return ! environment.getVariables().isEmpty();
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

	public String getSourceName() {
		if(environment.getRoot() == null) return null;
		
		return environment.getRoot().getName().replaceAll("::", "/")+".rsc";
	}

	public Environment getEnvironment() {
		return environment;
	}

	/**
	 * Transforms the function list from
	 * {@link org.rascalmpl.interpreter.env.Environment#getFunctions()} to a
	 * {@link java.util.Map}.
	 * 
	 * @return function map utilizing native Collections API
	 */
	public SortedMap<String, List<AbstractFunction>> getFunctions() {
		SortedMap<String, List<AbstractFunction>> functionMap = 
					new TreeMap<String, List<AbstractFunction>>(); 
		
		for (Pair<String, List<AbstractFunction>>  functionPair : environment.getFunctions()) {
			functionMap.put(functionPair.getFirst(), functionPair.getSecond());
		}
		
		return functionMap;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 * used by eclipse to refresh the view 
	 * (see the fireDeltaUpdatingSelectedFrame of the ThreadEventHandler class)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof RascalStackFrame) {
			RascalStackFrame sf = (RascalStackFrame)obj;
			return sf.getThread().equals(getThread()) && 
			    sf.getSourceName() == null ? getSourceName() == null : sf.getSourceName().equals(getSourceName()) &&
				sf.getEnvironment().equals(getEnvironment()) &&
				sf.getLocation().equals(getLocation());
		}
		return false;
	}

	private ISourceLocation getLocation() {
		return location;
	}

}
