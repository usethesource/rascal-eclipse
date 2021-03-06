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
package org.rascalmpl.eclipse.debug.ui.breakpoints;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.rascalmpl.eclipse.IRascalResources;

import io.usethesource.impulse.editor.UniversalEditor;
import io.usethesource.impulse.parser.IParseController;


public class RascalEditorAdapterFactory implements IAdapterFactory {

	@SuppressWarnings({"rawtypes", "unchecked"})
    public Object getAdapter(Object adaptableObject, Class adapterType) {
	  if (adaptableObject instanceof UniversalEditor) {
			IParseController pc = ((UniversalEditor) adaptableObject).getParseController();
            if (pc != null && pc.getPath().getFileExtension().equals(IRascalResources.RASCAL_EXT)) {
			  if (adapterType.equals(IToggleBreakpointsTarget.class)) {
			      return new RascalBreakpointAdapter();
			  }
			}
		}
		return null;
	}

	public Class<?>[] getAdapterList() {
		return new Class[] {IToggleBreakpointsTarget.class};
	}

}
