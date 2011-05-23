/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.debug.core.model;

import java.net.URI;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.env.Environment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.result.Result;

/* model for the local variable of a module */

public class RascalVariable extends RascalDebugElement implements IVariable {

	// name & corresponding environment
	private String name;
	private Environment envt;
	private Result<org.eclipse.imp.pdb.facts.IValue> value;


	/**
	 * Constructs a variable contained in the given stack frame
	 * with the given name.
	 * 
	 * @param frame owning stack frame
	 * @param name variable name
	 */
	public RascalVariable(RascalStackFrame frame, String name) {
		this(frame, name, frame.getEnvt());
	}

	/**
	 * Constructs a variable contained in the given stack frame
	 * with the given name and the given imported module.
	 * 
	 * @param frame owning stack frame
	 * @param name variable name
	 * @param module imported module
	 */
	public RascalVariable(RascalStackFrame frame, ModuleEnvironment module) {
		this(frame, module.getName(), module);
	}

	protected RascalVariable(RascalStackFrame frame, String name, Environment envt) {
		super(frame.getRascalDebugTarget());
		this.name = name;
		this.envt = envt;
		this.value = envt.getVariable(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IValue getValue() throws DebugException {
		return new RascalVariableValue(this.getRascalDebugTarget(), value);
	}

	public boolean isRelation() {
		return value.getType().isRelationType() && value.getType().getArity() == 2;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getName()
	 */
	public String getName() throws DebugException {
		return name;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return value.getType().toString();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#hasValueChanged()
	 */
	public boolean hasValueChanged() throws DebugException {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(java.lang.String)
	 */
	public void setValue(String expression) throws DebugException {
		Evaluator eval = getRascalDebugTarget().getEvaluator();
		synchronized(eval){
			//evaluate
			value = eval.eval(null, expression, URI.create("debug:///"));
	
			//store the result in its environment
			envt.storeVariable(name, value);
	
			fireChangeEvent(DebugEvent.CONTENT);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(org.eclipse.debug.core.model.IValue)
	 */
	public void setValue(IValue value) throws DebugException {

	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
	 */
	public boolean supportsValueModification() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(java.lang.String)
	 */
	public boolean verifyValue(String expression) throws DebugException {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(org.eclipse.debug.core.model.IValue)
	 */
	public boolean verifyValue(IValue value) throws DebugException {
		return false;
	}

}
