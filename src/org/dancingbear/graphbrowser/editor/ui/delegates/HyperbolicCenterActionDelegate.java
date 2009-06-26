package org.dancingbear.graphbrowser.editor.ui.delegates;

import org.dancingbear.graphbrowser.editor.jface.action.FisheyeAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPage;

public class HyperbolicCenterActionDelegate  extends AbstractObjectActionDelegate {


    @Override
    public IAction createAction() {
        IWorkbenchPage page = getActivePart().getSite().getPage();
        FisheyeAction action = new FisheyeAction(page);
        return action;
    }
}
