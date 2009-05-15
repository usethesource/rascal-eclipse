package org.meta_environment.rascal.eclipse.perspective;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.meta_environment.rascal.eclipse.console.ConsoleFactory;

public class Factory implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();

		IFolderLayout folder = layout.createFolder("left", IPageLayout.LEFT, (float) 0.25, editorArea); 
		folder.addView(JavaUI.ID_PACKAGES);
		folder.addPlaceholder(IPageLayout.ID_RES_NAV);

		IFolderLayout outputfolder = layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.75, editorArea); 
		outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);
		outputfolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);

		IFolderLayout outlineFolder = layout.createFolder("outline", IPageLayout.RIGHT, (float) 0.75, editorArea);
		outlineFolder.addView(IPageLayout.ID_OUTLINE);

		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);

		layout.addShowViewShortcut(JavaUI.ID_PACKAGES);
		layout.addShowViewShortcut(JavaUI.ID_SOURCE_VIEW);
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);

		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IPageLayout.ID_PROGRESS_VIEW);

		// new actions - Java project creation wizard
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");
		
		new ConsoleFactory().openConsole();
	}

}
