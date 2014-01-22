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
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.result.Result;

public class RascalImportedModuleValue extends RascalDebugElement implements IValue {

	private RascalDebugTarget target;
	private ModuleEnvironment module;
	private RascalStackFrame frame;

	public RascalImportedModuleValue(RascalStackFrame frame, RascalDebugTarget target,
			ModuleEnvironment value) {
		super(frame.getDebugTarget());
		this.module = value;
		this.target = target;
		this.frame = frame;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return "Module";
	}

	@Override
	public boolean equals(Object obj) {
	  if (obj instanceof RascalImportedModuleValue) {
	    RascalImportedModuleValue o = (RascalImportedModuleValue) obj;
	    
	    return module.equals(o.module)
	        && frame.equals(o.frame);
	  }
	  
	  return false;
	}
	
	@Override
	public int hashCode() {
	  return 5 * module.hashCode() + 23 * frame.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	public String getValueString() throws DebugException {
		return module.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		Map<String, Result<org.eclipse.imp.pdb.facts.IValue>> vars = module.getVariables();
		
		ArrayList<RascalVariable> variables = new ArrayList<RascalVariable>(vars.size());
		
		for (String var: vars.keySet()) {
			variables.add(new RascalVariable(frame, module, var, vars.get(var).getValue()));
		}		
		
		return variables.toArray(new IVariable[] {});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		return ! module.getVariables().isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return target.getAdapter(adapter);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return module.toString();	
	}

}
