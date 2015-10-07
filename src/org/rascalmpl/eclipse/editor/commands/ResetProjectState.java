package org.rascalmpl.eclipse.editor.commands;

import io.usethesource.impulse.editor.UniversalEditor;
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
