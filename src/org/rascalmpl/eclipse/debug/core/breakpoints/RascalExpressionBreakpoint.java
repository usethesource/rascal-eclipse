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
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.debug.core.breakpoints;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.text.ITextSelection;

/**
 * Rascal expression breakpoint
 * 
 * TODO: check if this kind of breakpoint is used at all.
 */
public class RascalExpressionBreakpoint extends RascalSourceLocationBreakpoint {
	
	private ITextSelection selection;

	/**
	 * Default constructor is required for the breakpoint manager
	 * to re-create persisted breakpoints. After instantiating a breakpoint,
	 * the <code>setMarker(...)</code> method is called to restore
	 * this breakpoint's attributes.
	 */
	public RascalExpressionBreakpoint() {
		super();
	}
	
	public RascalExpressionBreakpoint(final IResource resource, final ITextSelection selection) throws CoreException {
		this.selection = selection;
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IMarker marker = resource.createMarker("rascal.markerType.sourceLocationBreakpoint");
				setMarker(marker);
				marker.setAttribute(IBreakpoint.ENABLED, Boolean.TRUE);
				marker.setAttribute(IMarker.LINE_NUMBER, selection.getStartLine()+1);
				marker.setAttribute(IMarker.CHAR_START, selection.getOffset());
				marker.setAttribute(IMarker.CHAR_END, selection.getOffset() + selection.getLength());
				marker.setAttribute(IBreakpoint.ID, getModelIdentifier());
				marker.setAttribute(IMarker.MESSAGE, "Expression Breakpoint: " + resource.getName() + "offset: " + selection.getOffset() + "]");
			}
		};
		run(getMarkerRule(resource), runnable);
	}

	/**
	 * @return the selection
	 */
	public ITextSelection getSelection() {
		return selection;
	}

//	/**
//     * Determines if this breakpoint was hit and notifies the thread.
//     * 
//     * @param event breakpoint event
//     */
//    @Override
//	private void handleHit(DebugEvent event) {
//		ISourceLocation location = (ISourceLocation) event.getData();
//				
//		try {
//			/*
//			 * Location information of the breakpoint (in format
//			 * {@link IPath}) and location information of the
//			 * source location (in format {@link
//			 * ISourceLocation}) are both transformed to {@link
//			 * URI} instances of schmema type "project".
//			 */
//			URI uriBreakPointLocation = ProjectURIResolver.constructNonEncodedProjectURI(getResource().getFullPath());
//			URI uriSourceLocation     = getDebugTarget().getDebuggableURIResolverRegistry().getResourceURI(location.getURI());
//			
//			if (uriBreakPointLocation.equals(uriSourceLocation)
//					&& getCharStart() <= location.getOffset()
//					&& location.getOffset() + location.getLength() <= getCharEnd()) {
//				notifyThread();
//			}
//		} catch(IOException e) {
//			/* ignore; schema does not supported breakpoints */
//		} catch(CoreException e) {
//		}
//    }
		
}
