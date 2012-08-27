package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.imp.editor.UniversalEditor;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;

public class ResetProjectState extends AbstractEditorAction {
	public ResetProjectState(UniversalEditor editor) {
		super(editor, "Reset project state");
	}

	@Override
	public void run() {
		if (project != null) {
			ProjectEvaluatorFactory.getInstance().resetParser(project);
		}
	}
}
