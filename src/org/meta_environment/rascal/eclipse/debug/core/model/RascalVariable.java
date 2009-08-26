package org.meta_environment.rascal.eclipse.debug.core.model;

import java.io.IOException;

import org.dancingbear.graphbrowser.editor.gef.ui.parts.GraphEditor;
import org.dancingbear.graphbrowser.model.IModelGraph;
import org.dancingbear.graphbrowser.model.ModelGraphRegister;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.meta_environment.rascal.eclipse.lib.View;
import org.meta_environment.rascal.interpreter.env.Environment;
import org.meta_environment.rascal.interpreter.env.ModuleEnvironment;
import org.meta_environment.rascal.interpreter.result.Result;

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
		if (isRelation()) {
			updateRelationModel();
		}
	}

	public void updateRelationModel() {
		/* update the graph model if the view is already open */
		if (ModelGraphRegister.getInstance().isGraphOpen(name)) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					/* close all the editors associated with this graph */
					IWorkbench wb = PlatformUI.getWorkbench();
					IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
					if (win != null) {
						final IWorkbenchPage page = win.getActivePage();
						IEditorReference[] editors = page.getEditorReferences();
						for (int i = 0; i < editors.length; i++) {
							IEditorPart editor = editors[i].getEditor(false);
							if (editor instanceof GraphEditor) {
								GraphEditor graphEditor =  (GraphEditor) editor;
								IModelGraph g = graphEditor.getGraph();	
								if (g.getName().equals(name)) {
									graphEditor.close(false);
								}
							}
						}
					}
					View.dot(name, value.getValue());
				}
			});
		}
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
		try {
			//parse the expression
			org.meta_environment.rascal.ast.Expression ast = getRascalDebugTarget().getInterpreter().getExpression(expression);

			//evaluate
			value = getRascalDebugTarget().getEvaluator().eval(ast);

			//store the result in its environment
			envt.storeVariable(name, value);

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
