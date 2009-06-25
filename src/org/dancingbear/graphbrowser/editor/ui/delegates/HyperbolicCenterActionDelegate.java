package org.dancingbear.graphbrowser.editor.ui.delegates;

import org.dancingbear.graphbrowser.editor.jface.action.HyperbolicCenterAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPage;

public class HyperbolicCenterActionDelegate  extends AbstractObjectActionDelegate {


    @Override
    public IAction createAction() {
        IWorkbenchPage page = getActivePart().getSite().getPage();
        HyperbolicCenterAction action = new HyperbolicCenterAction(page);
        return action;
    }
}
