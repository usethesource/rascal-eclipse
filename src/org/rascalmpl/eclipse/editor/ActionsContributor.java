package org.rascalmpl.eclipse.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.services.ILanguageActionsContributor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.rascalmpl.eclipse.perspective.actions.CopyToConsole;
import org.rascalmpl.eclipse.perspective.actions.LaunchConsoleAction;
import org.rascalmpl.eclipse.perspective.actions.ListAmbiguities;
import org.rascalmpl.eclipse.perspective.actions.RunAmbiDexter;

public class ActionsContributor implements ILanguageActionsContributor {

	@Override
	public void contributeToEditorMenu(UniversalEditor editor,
			IMenuManager menuManager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contributeToToolBar(final UniversalEditor editor,
			IToolBarManager toolbarManager) {
		
	}

	@Override
	public void contributeToStatusLine(UniversalEditor editor,
			IStatusLineManager statusLineManager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contributeToMenuBar(UniversalEditor editor, IMenuManager menu) {
		IProject rawProject = editor.getParseController().getProject().getRawProject();
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(rawProject.getFullPath().append(editor.getParseController().getPath()));
		
		menu = menu.findMenuUsingPath("Rascal");
		menu.add(new LaunchConsoleAction(rawProject, file));
		menu.add(new RunAmbiDexter(editor, rawProject, file));
		menu.add(new ListAmbiguities(editor, rawProject, file)); 
		menu.add(new CopyToConsole(editor));
	}

}
