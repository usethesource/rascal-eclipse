/*******************************************************************************
 * Copyright (c) 2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI  
*******************************************************************************/
package org.rascalmpl.eclipse.debug.core.model;

import org.eclipse.debug.core.DebugEvent;

/**
 * Notification when a debug event from the runtime was triggered.
 */
public interface IRascalDebugEventListener {
	
	/**
	 * Notification that a debug event was triggered by the interpreter.
	 * 
	 * @param event the debug event
	 */
	public void onRascalDebugEvent(DebugEvent event);
	
}