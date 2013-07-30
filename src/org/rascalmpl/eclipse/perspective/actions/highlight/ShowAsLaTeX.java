package org.rascalmpl.eclipse.perspective.actions.highlight;

import org.eclipse.imp.editor.UniversalEditor;

public class ShowAsLaTeX extends HighlightAction {

	public ShowAsLaTeX(UniversalEditor editor) {
		super(editor, "Show as LaTeX", "lang::box::util::HighlightToLatex", "highlight2latex", "tex");
	}

}
