package org.meta_environment.rascal.eclipse.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.imp.pdb.facts.ISourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.meta_environment.rascal.ast.Name;
import org.meta_environment.rascal.ast.QualifiedName;
import org.meta_environment.rascal.eclipse.debug.ui.presentation.RascalModelPresentation;
import org.meta_environment.rascal.interpreter.env.Environment;
import org.meta_environment.rascal.interpreter.env.Lambda;
import org.meta_environment.rascal.interpreter.env.ModuleEnvironment;

public class RascalStackFrame<var> extends RascalDebugElement implements IStackFrame{

	private Environment envt; // Environment corresponding to the current scope
	private ISourceLocation loc; // Location of the call

	public RascalStackFrame(RascalDebugTarget target, Environment envt, ISourceLocation loc) {
		super(target);
		this.envt = envt;
		this.loc = loc;
	}


	public int getCharEnd() throws DebugException {
		if(loc != null){
			return loc.getOffset()+loc.getLength();
		} else {
			return 0;
		}
	}

	public int getCharStart() throws DebugException {
		if(loc != null){
			return loc.getOffset();
		} else {
			return 0;
		}	
	}

	public int getLineNumber() throws DebugException {
		if(loc != null){
			return loc.getBeginLine();
		} else {
			return 0;
		}
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
		if (envt.getRoot().getName() != null) {
			return envt.getRoot().getName()+".rsc";
		} else {
			return null;
		}
	}

	public Environment getEnvt() {
		return envt;
	}

	public List<Entry<String, List<Lambda>>> getFunctions() {
		return envt.getFunctions();
	}

}
