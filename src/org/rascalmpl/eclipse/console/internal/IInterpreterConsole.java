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
package org.rascalmpl.eclipse.console.internal;

import java.io.OutputStream;

import org.eclipse.ui.console.IConsole;

/**
 * Consoles should implement this.
 * 
 * @author Arnold Lankamp
 */
public interface IInterpreterConsole extends IConsole{
	
	/**
	 * Returns the output stream for this console. Interpreters can use this to write output to the
	 * console during the execution of a command.
	 * 
	 * @return The output stream that can be used to write to the console while executing a
	 * command.
	 */
	OutputStream getConsoleOutputStream();
	
	/**
	 * Returns if the console was terminated by {@link #terminate()}.  
	 * 
	 * @return termination status
	 */
	boolean isTerminated();
	
	/**
	 * Terminates this console and it's associated interpreter.
	 */
	void terminate();
	
	/**
	 * Executes the given command in this console.
	 * 
	 * @param command
	 *          The command to execute.
	 */
	void executeCommand(String command);
	
	/**
	 * Returns the interpreter that is associated with this console.
	 * 
	 * @return The interpreter that is associated with this console.
	 */
	IInterpreter getInterpreter();
	
	/**
	 * Checks if this console has a command history associated with it.
	 * 
	 * @return True if this console has a command history associated with; false otherwise.
	 */
	boolean hasHistory();
	
	/**
	 * Returns the command history associated this this console. (Optional operation).
	 * 
	 * @return The command history associated this this console; null if none is present.
	 */
	CommandHistory getHistory();
}
