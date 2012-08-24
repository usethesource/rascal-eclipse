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

import java.io.IOException;
import java.io.Writer;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.imp.pdb.facts.io.StandardTextWriter;
import org.rascalmpl.interpreter.result.Result;
import org.rascalmpl.interpreter.utils.LimitedResultWriter;
import org.rascalmpl.interpreter.utils.LimitedResultWriter.IOLimitReachedException;

public class RascalVariableValue extends RascalDebugElement implements IValue {
	
	/* do not print more than MAX_VALUE_STRING characters */
	private final static int MAX_VALUE_STRING = 1000;

	private RascalDebugTarget target;
	private Result<org.eclipse.imp.pdb.facts.IValue> value;

	public RascalVariableValue(RascalDebugTarget target,
			Result<org.eclipse.imp.pdb.facts.IValue> value) {
		super(target);
		this.value = value;
		this.target = target;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return value.getType().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	public String getValueString() throws DebugException {
		if (value.getValue() == null) {
			return "";
		}
		
		Writer w = new LimitedResultWriter(MAX_VALUE_STRING);
		try {
			new StandardTextWriter(true, 2).write(value.getValue(), w);
			return w.toString();
		} catch (IOLimitReachedException e) {
			return w.toString();
		}
		catch (IOException e) {
			return "error during serialization...";
		} 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		return false;
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
		return value.getValue().toString();	
	}
	
	public org.eclipse.imp.pdb.facts.IValue getValue() {
		return value.getValue();
	}

}
