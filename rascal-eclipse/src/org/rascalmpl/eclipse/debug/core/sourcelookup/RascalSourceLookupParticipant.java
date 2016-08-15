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
package org.rascalmpl.eclipse.debug.core.sourcelookup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.rascalmpl.eclipse.debug.core.model.RascalStackFrame;

/**
 * The Rascal source lookup participant knows how to translate a 
 * rascal stack frame into a source file name 
 */
public class RascalSourceLookupParticipant extends AbstractSourceLookupParticipant {
	public static final String RASCAL_CONSOLE_DUMMY = "rascal.console.dummy";

	public String getSourceName(Object object) throws CoreException {
	    if (object instanceof RascalStackFrame) {
	        RascalStackFrame stackFrame = (RascalStackFrame)object;

	        if (stackFrame.hasSourceName()) { 
	            return stackFrame.getSourceName(); 
	        }
	        else {
	            return RASCAL_CONSOLE_DUMMY;
	        }
	    }

	    return RASCAL_CONSOLE_DUMMY;
	}

}
