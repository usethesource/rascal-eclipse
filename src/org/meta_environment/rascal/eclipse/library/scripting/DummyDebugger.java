package org.meta_environment.rascal.eclipse.library.scripting;

import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.meta_environment.rascal.interpreter.control_exceptions.QuitException;
import org.meta_environment.rascal.interpreter.debug.DebugSuspendMode;
import org.meta_environment.rascal.interpreter.debug.IDebugger;

public class DummyDebugger implements IDebugger{
	private volatile boolean terminated;
	
	public DummyDebugger(){
		super();
		
		terminated = false;
	}

	public void destroy(){
		terminated = true;
	}

	public boolean hasEnabledBreakpoint(ISourceLocation sourceLocation){
		return false;
	}

	public boolean isStepping(){
		return false;
	}

	public boolean isTerminated(){
		return terminated;
	}

	public void notifySuspend(DebugSuspendMode mode) throws QuitException{
		// Don't
	}

	public void stopStepping(){
		// Don't
	}
}
