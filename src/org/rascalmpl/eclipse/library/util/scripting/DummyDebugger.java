/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
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
