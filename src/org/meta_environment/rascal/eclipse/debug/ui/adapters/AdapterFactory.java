package org.meta_environment.rascal.eclipse.debug.ui.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalDebugTarget;


public class AdapterFactory implements IAdapterFactory {
	
	private static IElementContentProvider fgTargetAdapter = new RascalDebugTargetContentProvider();
	private static IModelProxyFactory fgFactory = new ModelProxyFactory();

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IElementContentProvider.class.equals(adapterType)) {
			if (adaptableObject instanceof RascalDebugTarget) {
				return fgTargetAdapter;
			}
		}
		if (IModelProxyFactory.class.equals(adapterType)) {
			if (adaptableObject instanceof RascalDebugTarget) {
				return fgFactory;
			}
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[]{IElementContentProvider.class, IModelProxyFactory.class};
	}

}
