package org.dancingbear.graphbrowser.editor.ui.delegates;

import org.dancingbear.graphbrowser.editor.jface.action.CreateNodeAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPage;

public class CreateNodeActionDelegate extends AbstractObjectActionDelegate {

    @Override
    public IAction createAction() {
        IWorkbenchPage page = getActivePart().getSite().getPage();
        CreateNodeAction action = new CreateNodeAction(page);
        return action;
    }

}