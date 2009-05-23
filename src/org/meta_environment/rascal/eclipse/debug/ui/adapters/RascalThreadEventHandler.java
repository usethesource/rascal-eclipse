package org.meta_environment.rascal.eclipse.debug.ui.adapters;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalThread;

public class RascalThreadEventHandler extends DebugEventHandler {

	private IStackFrame fPrev = null;

	/**
	 * Constructs and event handler for a threads in the given viewer.
	 * 
	 * @param viewer
	 */
	public RascalThreadEventHandler(AbstractModelProxy proxy) {
		super(proxy);
	}

	protected void handleSuspend(DebugEvent event) {
		IThread thread = (IThread) event.getSource();
		int extras = IModelDelta.STATE;
		if (event.getDetail() == DebugEvent.BREAKPOINT | event.getDetail() == DebugEvent.CLIENT_REQUEST) {
			extras = IModelDelta.EXPAND;
		}
		fireDeltaUpdatingTopFrame(thread, IModelDelta.NO_CHANGE | extras);
	}

	private boolean isEqual(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null) {
			return false;
		}
		return o1.equals(o2);
	}

	protected void handleResume(DebugEvent event) {
		IThread thread = (IThread) event.getSource();
		fireDeltaAndClearTopFrame(thread, IModelDelta.STATE | IModelDelta.CONTENT);
	}

	protected void handleCreate(DebugEvent event) {
		fireDeltaAndClearTopFrame((IThread) event.getSource(), IModelDelta.ADDED);
	}

	protected void handleTerminate(DebugEvent event) {
		fireDeltaAndClearTopFrame((IThread) event.getSource(), IModelDelta.REMOVED);
	}

	protected void handleChange(DebugEvent event) {
		fireDeltaUpdatingTopFrame((IThread) event.getSource(), IModelDelta.STATE);
	}

	protected void handleLateSuspend(DebugEvent suspend, DebugEvent resume) {
		IThread thread = (IThread) suspend.getSource();
		fireDeltaUpdatingTopFrame(thread, IModelDelta.CONTENT | IModelDelta.EXPAND);
	}

	protected void handleSuspendTimeout(DebugEvent event) {
		IThread thread = (IThread) event.getSource();
		fireDeltaAndClearTopFrame(thread, IModelDelta.CONTENT);
	}

	private ModelDelta buildRootDelta() {
		return new ModelDelta(getLaunchManager(), IModelDelta.NO_CHANGE);
	}

	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	protected ModelDelta addTarget(ModelDelta delta, IThread thread) {
		ILaunch launch = thread.getLaunch();
		Object[] children = launch.getChildren();
		delta = delta.addNode(launch, indexOf(getLaunchManager().getLaunches(), launch), IModelDelta.NO_CHANGE, children.length);
		IDebugTarget debugTarget = thread.getDebugTarget();
		int numFrames = -1;
		try {
			numFrames = thread.getStackFrames().length;
		} catch (DebugException e) {
		}
		return delta.addNode(debugTarget, indexOf(children, debugTarget), IModelDelta.NO_CHANGE, numFrames);
	}

	private void fireDeltaAndClearTopFrame(IThread thread, int flags) {
		ModelDelta delta = buildRootDelta();
		ModelDelta node = addTarget(delta, thread);
		synchronized (this) {
			fPrev = null;
		}
		fireDelta(delta);
	}

	private void fireDeltaUpdatingTopFrame(IThread thread, int flags) {
		ModelDelta delta = buildRootDelta();
		ModelDelta node = addTarget(delta, thread);
		synchronized (this) {
			IStackFrame prev = fPrev;
			IStackFrame frame = null;
			try {
				frame = thread.getTopStackFrame();
			} catch (DebugException e) {
			}
			if (isEqual(frame, prev)) {
				node.setFlags(flags);
			} else {
				node.setFlags(flags | IModelDelta.CONTENT);
			}
			if (frame != null) {
				node.addNode(frame, 0, IModelDelta.STATE | IModelDelta.SELECT, 0);
			}
			fPrev = frame;
		}
		fireDelta(delta);
	}	

	protected boolean handlesEvent(DebugEvent event) {
		return event.getSource() instanceof RascalThread;
	}

}
