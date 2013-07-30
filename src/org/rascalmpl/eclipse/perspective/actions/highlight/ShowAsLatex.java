package org.rascalmpl.eclipse.perspective.actions.highlight;

import org.eclipse.imp.editor.UniversalEditor;

public class ShowAsLatex extends HighlightAction {

	public ShowAsLatex(UniversalEditor editor) {
		super(editor, "Show as LaTeX", "highlight2latex", "tex");
	}

}
