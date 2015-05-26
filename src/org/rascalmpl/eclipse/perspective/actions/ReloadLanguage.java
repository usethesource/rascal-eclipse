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
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.plugins.LoadRascalPluginsFromProjects;
import org.rascalmpl.eclipse.terms.TermLanguageRegistry;
import org.rascalmpl.interpreter.Evaluator;

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

					LoadRascalPluginsFromProjects.registerTermLanguagePlugin(activeProject);

					for (IEditorReference editorRef: activePage.getEditorReferences()) {
						// mark all editor containing term language dirty
						IEditorPart editor = editorRef.getEditor(false);
						if (editor != null && editor instanceof UniversalEditor) {
							UniversalEditor uniEditor = (UniversalEditor)editor;
							
							if (TermLanguageRegistry.getInstance().getParser(uniEditor.getLanguage()) != null) {
								IDocumentProvider documentProvider = uniEditor.getDocumentProvider();
								IFileEditorInput edInput = (IFileEditorInput)uniEditor.getEditorInput() ;
								IDocument doc = documentProvider.getDocument(edInput);
								if (doc.getLength() > 0) {
									boolean wasDirty = uniEditor.isDirty();
									try {
										// TODO: make sure it's not in the undo
										doc.replace(0, 1, doc.get(0, 1));
									} catch (BadLocationException e) {
										// for now, ignore
									}
									if (!wasDirty) {
										uniEditor.doSave(new NullProgressMonitor());
									}
								}
							}
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
