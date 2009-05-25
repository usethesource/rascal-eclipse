package org.meta_environment.rascal.eclipse.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.imp.pdb.facts.ISourceLocation;

import java.util.Set;
import org.meta_environment.rascal.interpreter.env.Environment;

public class RascalStackFrame<var> extends RascalDebugElement implements IStackFrame{

	private Environment envt;
	private ISourceLocation loc;

	public RascalStackFrame(RascalDebugTarget target, Environment envt) {
		super(target);
		this.envt = envt;
		this.loc = envt.getLocation();
	}

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
		//TODO: manage the list of visible variables
		Set<String> vars = envt.getVariables().keySet();
		IVariable[] ivars = new IVariable[vars.size()];
		int i = 0;
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
		//TODO: find how to obtain the file name of the current module from the Environment
		if (envt.getLocation() != null) {
			return envt.getRoot().getName()+".rsc";
		} else {
			return null;
		}
	}

	public Environment getEnvt() {
		return envt;
	}

}
