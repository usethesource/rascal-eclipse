/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
*******************************************************************************/
package org.rascalmpl.eclipse.debug.core.sourcelookup;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.ui.ISourcePresentation;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.rascalmpl.uri.URIEditorInput;
import org.rascalmpl.uri.URIResourceResolver;
import org.rascalmpl.uri.URIStorage;

public class RascalSourceLookupDirector extends AbstractSourceLookupDirector implements ISourcePresentation {
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector#initializeParticipants()
	 */
	public void initializeParticipants() {
		addParticipants(new ISourceLookupParticipant[] {
		    new RascalSourceLookupParticipant()
		    });
	}

	@Override
	public IEditorInput getEditorInput(Object element) {
		if (element instanceof URISourceContainer) {
			ISourceLocation uri = ((URISourceContainer) element).getURI();
			IResource resource = URIResourceResolver.getResource(uri);
			if (resource != null && resource instanceof IFile) {
				return new FileEditorInput((IFile) resource);
			}
			URIStorage storage = new URIStorage(uri);
			return new URIEditorInput(storage);
		}
		return null;
	}

	@Override
	public String getEditorId(IEditorInput input, Object element) {
		if (element instanceof URISourceContainer || element instanceof ILineBreakpoint) {
			return UniversalEditor.EDITOR_ID;
		}
		return null;
	}
}