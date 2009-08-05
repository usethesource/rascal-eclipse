package org.dancingbear.graphbrowser.editor.ui.delegates;

import org.dancingbear.graphbrowser.editor.jface.action.ZestLayoutAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPage;

public class ZestLayoutActionDelegate extends AbstractObjectActionDelegate {

	@Override
	public IAction createAction() {
		IWorkbenchPage page = getActivePart().getSite().getPage();
		ZestLayoutAction action = new ZestLayoutAction(page);
		return action;
	}
	
}
