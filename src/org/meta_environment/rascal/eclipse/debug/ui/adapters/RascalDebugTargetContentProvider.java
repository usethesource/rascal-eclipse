package org.meta_environment.rascal.eclipse.debug.ui.adapters;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.model.elements.ElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalDebugTarget;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalThread;


public class RascalDebugTargetContentProvider extends ElementContentProvider {


	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getChildCount(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	protected int getChildCount(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		RascalThread thread = ((RascalDebugTarget) element).getThread();
		if (thread != null) {
			if (thread.hasStackFrames()) {
				return thread.getStackFrames().length;
			}
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#hasChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	protected boolean hasChildren(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		RascalThread thread = ((RascalDebugTarget) element).getThread();
		if (thread != null) {
			return thread.hasStackFrames();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getChildren(java.lang.Object, int, int, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	protected Object[] getChildren(Object parent, int index, int length, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		if (context.getId().equals(IDebugUIConstants.ID_DEBUG_VIEW)) {
			RascalThread thread = ((RascalDebugTarget) parent).getThread();
			if (thread != null) {
				return getElements(thread.getStackFrames(), index, length);
			}
		}
		return EMPTY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#supportsContextId(java.lang.String)
	 */
	protected boolean supportsContextId(String id) {
		return IDebugUIConstants.ID_DEBUG_VIEW.equals(id);
	}

}
