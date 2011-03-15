package org.rascalmpl.eclipse.library.util.scripting;

import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.interpreter.control_exceptions.QuitException;
import org.rascalmpl.interpreter.debug.DebugSuspendMode;
import org.rascalmpl.interpreter.debug.IDebugger;

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
