package org.meta_environment.rascal.eclipse.debug.core.model;

import java.io.IOException;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.meta_environment.rascal.interpreter.env.Environment;
import org.meta_environment.rascal.interpreter.env.ModuleEnvironment;
import org.meta_environment.rascal.interpreter.result.Result;

/* model for the local variable of a module */

public class RascalVariable extends RascalDebugElement implements IVariable {

	// name & corresponding environment
	private String name;
	private Environment envt;

	/**
	 * Constructs a variable contained in the given stack frame
	 * with the given name.
	 * 
	 * @param frame owning stack frame
	 * @param name variable name
	 */
	public RascalVariable(RascalStackFrame frame, String name) {
		super(frame.getRascalDebugTarget());
		this.envt = frame.getEnvt();
		this.name = name;
	}

	/**
	 * Constructs a variable contained in the given stack frame
	 * with the given name and the given imported module.
	 * 
	 * @param frame owning stack frame
	 * @param name variable name
	 * @param module imported module
	 */
	public RascalVariable(RascalStackFrame frame, String name, ModuleEnvironment module) {
		super(frame.getRascalDebugTarget());
		this.name = name;
		this.envt = module;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IValue getValue() throws DebugException {
		Result<org.eclipse.imp.pdb.facts.IValue> value;
		value = envt.getVariable(name);
		return new RascalVariableValue(this.getRascalDebugTarget(), value);
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
		return null;
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
		try {
			//parse the expression
			org.meta_environment.rascal.ast.Expression ast = getRascalDebugTarget().getInterpreter().getExpression(expression);

			boolean expressionStepMode = getRascalDebugTarget().getEvaluator().expressionStepModeEnabled();
			if (expressionStepMode) {
				//deactivate the step by step
				getRascalDebugTarget().getEvaluator().setExpressionStepMode(false);
			}

			//evaluate
			Result<org.eclipse.imp.pdb.facts.IValue> result = getRascalDebugTarget().getEvaluator().eval(ast);

			//store the result in its environment
			envt.storeVariable(name, result);

			//reactivate the expression step by step if necessary
			getRascalDebugTarget().getEvaluator().setExpressionStepMode(expressionStepMode);

			fireChangeEvent(DebugEvent.CONTENT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(org.eclipse.debug.core.model.IValue)
	 */
	public void setValue(IValue value) throws DebugException {
		//TODO
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
