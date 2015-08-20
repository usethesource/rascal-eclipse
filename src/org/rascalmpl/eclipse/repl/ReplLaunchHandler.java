package org.rascalmpl.eclipse.repl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate;
import org.eclipse.tm.terminal.view.ui.launcher.LauncherDelegateManager;
import org.eclipse.ui.handlers.HandlerUtil;

public class ReplLaunchHandler  extends AbstractHandler implements IHandler, IHandler2 {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the current selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		// Get all applicable launcher delegates for the current selection
		ILauncherDelegate[] delegates = LauncherDelegateManager.getInstance().getApplicableLauncherDelegates(selection);

		// Find the local terminal launcher delegate
		ILauncherDelegate delegate = null;
		for (ILauncherDelegate candidate : delegates) {
			if ("org.rascalmpl.eclipse.repl.launcher".equals(candidate.getId())) { 
				delegate = candidate;
				break;
			}
		}

		// Launch the local terminal
		if (delegate != null) {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(ITerminalsConnectorConstants.PROP_DELEGATE_ID, delegate.getId());
			properties.put(ITerminalsConnectorConstants.PROP_SELECTION, selection);
			delegate.execute(properties, null);
		}

		return null;
	}

}