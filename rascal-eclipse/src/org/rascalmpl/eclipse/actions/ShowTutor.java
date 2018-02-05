package org.rascalmpl.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.tutor.TutorView;

public class ShowTutor implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow activeWindow;

	@Override
	public void run(IAction action) {
		if (activeWindow != null) {
			try {
				activeWindow.getActivePage().showView(TutorView.ID);
			} catch (PartInitException e) {
				Activator.log("could not find tutor view", e);
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void init(IWorkbenchWindow window) {
		activeWindow = window;
	}

}
