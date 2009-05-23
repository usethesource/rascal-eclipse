package org.meta_environment.rascal.eclipse.debug.ui.adapters;

import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalDebugTarget;


public class ModelProxyFactory implements IModelProxyFactory {

	public IModelProxy createModelProxy(Object element, IPresentationContext context) {
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId())) {
			if (element instanceof RascalDebugTarget){
				return new RascalDebugTargetProxy((IDebugTarget) element);
			}
		}
		return null;
	}

}
