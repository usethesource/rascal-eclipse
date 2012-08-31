package org.rascalmpl.eclipse.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.util.ResourcesToModules;

public class RenameParticipant extends org.eclipse.ltk.core.refactoring.participants.RenameParticipant {
	private IFile file;
	
	public RenameParticipant() { }
	
	@Override
	protected boolean initialize(Object element) {
		if (element instanceof IFile) {
			file = (IFile) element;
			return true;
		}
		return false;
	}

	@Override
	public String getName() {
		return "Rename Rascal Module";
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws OperationCanceledException {
		return RefactoringStatus.create(Status.OK_STATUS);
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		
		try {
			String moduleName = ResourcesToModules.moduleFromFile(file);
			String newModuleName = new Path(getArguments().getNewName()).removeFileExtension().toString();
			  
			int last;
			if ((last = moduleName.indexOf("::")) != -1) {
				newModuleName = moduleName.substring(0, last + 2) + newModuleName;
			}
			
			TextFileChange change = new TextFileChange("Rename Rascal Module", file);
			change.setSaveMode(TextFileChange.LEAVE_DIRTY); // somehow essential to make sure edit is done in the renamed resource instead of the old resource
			String content = change.getCurrentContent(pm);

			int index = content.indexOf(moduleName);
			if (index != -1) {
				MultiTextEdit m = new MultiTextEdit();
				m.addChild(new DeleteEdit(index, moduleName.length()));
				m.addChild(new InsertEdit(index, newModuleName));
				change.setEdit(m);
			}

			return change;
		} 
		catch (CoreException e) {
			Activator.getInstance().logException("error during renaming", e);
			return new NullChange();
		}
	}
}
