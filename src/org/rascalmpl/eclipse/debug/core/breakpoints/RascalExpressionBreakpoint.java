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
 */
public class RascalExpressionBreakpoint extends RascalLineBreakpoint {
	
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
				IMarker marker = resource.createMarker("rascal.markerType.lineBreakpoint");
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

}
