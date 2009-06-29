package org.meta_environment.rascal.eclipse.debug.core.model;

import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.meta_environment.rascal.interpreter.env.ModuleEnvironment;
import org.meta_environment.rascal.interpreter.result.Result;

public class RascalImportedModuleValue implements IValue {

	private RascalDebugTarget target;
	private ModuleEnvironment module;
	private RascalStackFrame frame;

	public RascalImportedModuleValue(RascalStackFrame frame, RascalDebugTarget target,
			ModuleEnvironment value) {
		this.module = value;
		this.target = target;
		this.frame = frame;
	}

	public String getReferenceTypeName() throws DebugException {
		return "Module";
	}

	public String getValueString() throws DebugException {
		return module.toString();
	}

	public IVariable[] getVariables() throws DebugException {
		Map<String, Result<org.eclipse.imp.pdb.facts.IValue>> vars = module.getVariables();
		IVariable[] varmodels = new RascalVariable[vars.size()];
		int i = 0;
		for (String var: vars.keySet()) {
			varmodels[i] = new RascalVariable(frame, var, module);
			i++;
		}
		return varmodels;
	}

	public boolean hasVariables() throws DebugException {
		return ! module.getVariables().isEmpty();
	}

	public boolean isAllocated() throws DebugException {
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

	public String toString() {
		return module.toString();	
	}
}
