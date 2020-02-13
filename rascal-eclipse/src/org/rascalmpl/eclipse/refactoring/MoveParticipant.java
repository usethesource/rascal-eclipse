package org.rascalmpl.eclipse.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
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
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.util.ResourcesToModules;

public class MoveParticipant extends
		org.eclipse.ltk.core.refactoring.participants.MoveParticipant {

	private IFile file;

	public MoveParticipant() {
	}

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
		return "Rascal move refactoring";
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws OperationCanceledException {
		return RefactoringStatus.create(Status.OK_STATUS);
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {

		try {
			String moduleName = ResourcesToModules.moduleFromFile(file);
			Object destination = getArguments().getDestination();
			
			if (destination instanceof IFolder) {
				IFolder folder = (IFolder) destination;
				String lastName = moduleName;
				int last;

				if ((last = moduleName.lastIndexOf("::")) != -1) {
					lastName = moduleName.substring(last + "::".length());
				}
				
				IFile newFile = folder.getFile(lastName + "." + IRascalResources.RASCAL_EXT);
				String newModuleName = ResourcesToModules.moduleFromFile(newFile);
				
				TextFileChange change = new TextFileChange("Move Rascal Module", file);
				// somehow essential to make sure edit is done in the renamed resource instead of the old resource
				change.setSaveMode(TextFileChange.LEAVE_DIRTY); 
				String content = change.getCurrentContent(pm);

				int index = content.indexOf(moduleName);
				if (index != -1) {
					MultiTextEdit m = new MultiTextEdit();
					m.addChild(new InsertEdit(index + moduleName.length(), newModuleName));
					m.addChild(new DeleteEdit(index, moduleName.length()));
					change.setEdit(m);
				}
				
				return change;
			}
		} 
		catch (CoreException e) {
			Activator.getInstance().logException("error during renaming", e);
		}
		
		return new NullChange();
	}

}
