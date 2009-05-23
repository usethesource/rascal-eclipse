package org.meta_environment.rascal.eclipse.debug.ui.adapters;

import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler;
import org.eclipse.debug.internal.ui.viewers.update.DebugTargetEventHandler;
import org.eclipse.debug.internal.ui.viewers.update.DebugTargetProxy;

public class RascalDebugTargetProxy extends DebugTargetProxy {

	/**
	 * @param target
	 */
	public RascalDebugTargetProxy(IDebugTarget target) {
		super(target);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DebugTargetProxy#createEventHandlers()
	 */
	protected DebugEventHandler[] createEventHandlers() {
		return new DebugEventHandler[] { new DebugTargetEventHandler(this), new RascalThreadEventHandler(this) };
	}

}
