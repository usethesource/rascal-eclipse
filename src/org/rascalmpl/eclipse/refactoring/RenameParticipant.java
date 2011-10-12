package org.rascalmpl.eclipse.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.text.edits.ReplaceEdit;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;

public class RenameParticipant extends org.eclipse.ltk.core.refactoring.participants.RenameParticipant {
	private IFile file;
	
	public RenameParticipant() {
		System.err.println("created rename participant!");
	}
	
	@Override
	protected boolean initialize(Object element) {
		file = (IFile) element;
		return true;
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
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		
		try {
			String moduleName = computeModuleName(file);
			String newModuleName = new Path(getArguments().getNewName()).removeFileExtension().toOSString();
			
			int last;
			if ((last = moduleName.indexOf("::")) != -1) {
				newModuleName = moduleName.substring(0, last + 2) + newModuleName;
			}
			
			TextFileChange change = new TextFileChange("Rename Rascal Module", file);
			change.setSaveMode(TextFileChange.LEAVE_DIRTY); // somehow essential to make sure edit is done in the renamed resource instead of the old resource
			String content = change.getCurrentContent(pm);
//			ProjectEvaluatorFactory evaluatorFactory = ProjectEvaluatorFactory.getInstance();
//			Evaluator eval = evaluatorFactory.getEvaluator(file.getProject());
//			
//			IConstructor tree = eval.parseModule(null, content.toCharArray(), URI.create("refactoring:///"), new ModuleEnvironment("***refactoring***", eval.getHeap()));
//			
//			if (tree != null) {
//				
//			}
			int index = content.indexOf(moduleName);
			if (index != -1) {
				change.setEdit(new ReplaceEdit(index, moduleName.length(), newModuleName));
			}

			return change;
		} 
		catch (CoreException e) {
			Activator.getInstance().logException("error during renaming", e);
			return new NullChange();
		}
	}


	private String computeModuleName(IFile file) {
		IProject proj = file.getProject();
		if (proj != null && proj.exists()) {
			IFolder srcFolder = proj.getFolder(IRascalResources.RASCAL_SRC);
			if (srcFolder != null && srcFolder.exists()) {
				if (srcFolder.getProjectRelativePath().isPrefixOf(file.getProjectRelativePath())) {
					String name = file.getProjectRelativePath().removeFirstSegments(1).removeFileExtension().toPortableString();
					return name.replaceAll("/", "::");
				}
			}
		}
		
		return null;
	}

}
