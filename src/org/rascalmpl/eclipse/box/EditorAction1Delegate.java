package org.rascalmpl.eclipse.box;



import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public class EditorAction1Delegate extends org.eclipse.ui.actions.ActionDelegate implements
		IEditorActionDelegate {
    
	private BoxViewer b;
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// TODO Auto-generated method stub
	}

//	public void run(IAction action) {
//		// TODO Auto-generated method stub
//		b.getBoxPrinter().menuPrint();
//	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
