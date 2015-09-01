package org.rascalmpl.eclipse.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate;
import org.eclipse.tm.terminal.view.ui.launcher.LauncherDelegateManager;
import org.eclipse.ui.handlers.HandlerUtil;

public class ReplLaunchHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the current selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		String project = null;
		
		if (selection != null && selection instanceof StructuredSelection) {
		    StructuredSelection s = (StructuredSelection) selection;
		    Object r = s.getFirstElement();

		    if (r instanceof IResource) {
		        project = ((IResource) r).getProject().getName();
		    }
		}

		terminalForProject(project, "debug", null);

		return null;
	}

    public static void terminalForProject(String project, String mode, String module) {
        ILauncherDelegate delegate = LauncherDelegateManager.getInstance().getLauncherDelegate("org.rascalmpl.eclipse.rascal.launcher", false);

		if (delegate != null) {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(ITerminalsConnectorConstants.PROP_DELEGATE_ID, delegate.getId());
			properties.put("project", project);
			properties.put("mode", mode);
			properties.put("module", module);
			delegate.execute(properties, null);
		}
    }

}