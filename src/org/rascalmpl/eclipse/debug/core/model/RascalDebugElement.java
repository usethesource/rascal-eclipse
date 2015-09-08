/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.debug.core.model;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.rascalmpl.debug.IDebugMessage;
import org.rascalmpl.eclipse.IRascalResources;

/**
 * Common function for debug elements.
 */
abstract public class RascalDebugElement extends DebugElement {

	/**
	 * Constructs a new debug element in the given target.
	 * 
	 * @param target debug target
	 */
	public RascalDebugElement(IDebugTarget target) {
		super(target);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return IRascalResources.ID_RASCAL_DEBUG_MODEL;
	}
	
	/**
	 * Returns the debug target as a  target.
	 * 
	 * @return  debug target
	 */
	public RascalDebugTarget getRascalDebugTarget() {
	    return (RascalDebugTarget) getDebugTarget();
	}
	
	/**
	 * Returns the breakpoint manager
	 * 
     * @return the breakpoint manager
     */
    protected IBreakpointManager getBreakpointManager() {
        return DebugPlugin.getDefault().getBreakpointManager();
    }	
    
    /**
     * Sends an asynchronous request to the debugger.
     * 
     * @param message containing request
     */
    public void sendRequest(IDebugMessage message) {
    	getRascalDebugTarget().sendRequest(message);
    }
    
}
