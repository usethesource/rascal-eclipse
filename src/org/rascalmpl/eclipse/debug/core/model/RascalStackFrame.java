/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - emilie.balland@inria.fr (INRIA)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.debug.core.model;

import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.interpreter.env.Environment;
import org.rascalmpl.interpreter.result.OverloadedFunctionResult;

public class RascalStackFrame extends RascalDebugElement implements IStackFrame{

	private Environment envt; // Environment corresponding to the current scope
	private ISourceLocation loc; // Location of the call

	public RascalStackFrame(RascalDebugTarget target, Environment envt, ISourceLocation loc) {
		super(target);
		this.envt = envt;
		this.loc = loc;
	}


	public int getCharEnd() throws DebugException {
		if(loc == null) return 0;
		
		return loc.getOffset()+loc.getLength();
	}

	public int getCharStart() throws DebugException {
		if(loc == null)return 0;
		
		return loc.getOffset();
	}

	public int getLineNumber() throws DebugException {
		if(loc == null) return 0;
		
		return loc.getBeginLine();
	}

	public String getName() throws DebugException {
		//TODO: return the name of the current module
		return envt.getName();
	}

	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		return null;
	}

	public IThread getThread() {
		return getRascalDebugTarget().getThread();
	}

	public IVariable[] getVariables() throws DebugException {
		//manage the list of variables local to the current module
		Set<String> vars = envt.getVariables().keySet();
		//manage the list of imported modules
		Set<String> modules = envt.getImports();

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

	public boolean hasRegisterGroups() throws DebugException {
		return false;
	}

	public boolean hasVariables() throws DebugException {
		// TODO Auto-generated method stub
		return ! envt.getVariables().isEmpty();
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
		if(envt.getRoot() == null) return null;
		
		return envt.getRoot().getName().replaceAll("::", "/")+".rsc";
	}

	public Environment getEnvt() {
		return envt;
	}

	public List<Entry<String, OverloadedFunctionResult>> getFunctions() {
		return envt.getFunctions();
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
				sf.getEnvt().equals(getEnvt()) &&
				sf.getLocation().equals(getLocation());
		}
		return false;
	}


	private ISourceLocation getLocation() {
		return loc;
	}

}
