package org.meta_environment.rascal.eclipse.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.meta_environment.rascal.interpreter.result.Result;

public class RascalValue implements IValue {

	private RascalDebugTarget target;
	private Result<org.eclipse.imp.pdb.facts.IValue> value;

	public RascalValue(RascalDebugTarget target,
			Result<org.eclipse.imp.pdb.facts.IValue> value) {
		this.value = value;
		this.target = target;
	}

	public String getReferenceTypeName() throws DebugException {
		return value.getType().toString();
	}

	public String getValueString() throws DebugException {
		if (value.getValue() == null) return "";
		return value.getValue().toString();
	}

	public IVariable[] getVariables() throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasVariables() throws DebugException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isAllocated() throws DebugException {
		// TODO Auto-generated method stub
		return false;
	}

	public IDebugTarget getDebugTarget() {
		return target;
	}

	public ILaunch getLaunch() {
		return target.getLaunch();
	}

	public String getModelIdentifier() {
		return target.getModelIdentifier();
	}

	public Object getAdapter(Class adapter) {
		return target.getAdapter(adapter);
	}

}
