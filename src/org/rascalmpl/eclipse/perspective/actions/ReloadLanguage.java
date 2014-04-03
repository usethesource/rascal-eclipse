package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.plugins.LoadRascalPluginsFromProjects;
import org.rascalmpl.eclipse.terms.TermLanguageRegistry;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.IEvaluatorContext;

public class ReloadLanguage  implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow activeWindow;

	@Override
	public void run(IAction action) {
		if (activeWindow != null) {
			IWorkbenchPage activePage = activeWindow.getActivePage();
			if (activePage != null) {
				IEditorPart activeEditor = activePage.getActiveEditor();
				if (activeEditor != null && activeEditor instanceof UniversalEditor) {
					UniversalEditor ued = (UniversalEditor) activeEditor;
					IFileEditorInput input = (IFileEditorInput)ued.getEditorInput() ;
					IFile file = input.getFile();
					IProject activeProject = file.getProject();

					Evaluator eval = ProjectEvaluatorFactory.getInstance().getEvaluator(activeProject);
					LoadRascalPluginsFromProjects.registerTermLanguagePlugin(activeProject, eval);

					// Mark the document dirty
					IDocumentProvider documentProvider = ued.getDocumentProvider();
					IDocument doc = documentProvider.getDocument(input);
					if (doc.getLength() > 0) {
						boolean wasDirty = ued.isDirty();
						try {
							doc.replace(0, 1, doc.get(0, 1));
						} catch (BadLocationException e) {
							// for now, ignore
						}
						if (!wasDirty) {
							ued.doSave(new NullProgressMonitor());
						}
					}
				}
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
