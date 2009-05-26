package org.meta_environment.rascal.eclipse.debug.core.model;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.meta_environment.rascal.eclipse.IRascalResources;

/**
 * Common function for  debug elements.
 */
public class RascalDebugElement extends DebugElement {

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
	protected RascalDebugTarget getRascalDebugTarget() {
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
	
}
