package org.dancingbear.graphbrowser.editor.ui.delegates;

import org.dancingbear.graphbrowser.editor.jface.action.CreateEdgeAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPage;

public class CreateEdgeActionDelegate extends AbstractObjectActionDelegate {

    @Override
    public IAction createAction() {
        IWorkbenchPage page = getActivePart().getSite().getPage();
        CreateEdgeAction action = new CreateEdgeAction(page);
        return action;
    }

}