package org.dancingbear.graphbrowser.editor.ui.delegates;

import org.dancingbear.graphbrowser.editor.jface.action.ShowNodeAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPage;

public class ShowNodeActionDelegate extends AbstractObjectActionDelegate {

    @Override
    public IAction createAction() {
        IWorkbenchPage page = getActivePart().getSite().getPage();
        ShowNodeAction action = new ShowNodeAction(page);
        return action;
    }

}