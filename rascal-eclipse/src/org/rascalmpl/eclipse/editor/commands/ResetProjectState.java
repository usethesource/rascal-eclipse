package org.rascalmpl.eclipse.editor.commands;

import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;

import io.usethesource.impulse.editor.UniversalEditor;

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
