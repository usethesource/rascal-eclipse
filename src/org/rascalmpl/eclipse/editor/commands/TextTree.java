package org.rascalmpl.eclipse.editor.commands;

import io.usethesource.impulse.editor.UniversalEditor;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.rascalmpl.eclipse.editor.ParseController;
import org.rascalmpl.eclipse.library.util.ValueUI;
import org.rascalmpl.values.ValueFactoryFactory;

public class TextTree extends AbstractEditorAction {

	public TextTree(UniversalEditor editor) {
		super(editor, "Show textual parse tree");
	}
	
	@Override
	public void run() {
		IConstructor tree = (IConstructor) ((ParseController) editor.getParseController()).getCurrentAst();

		if (tree != null) {
			IValueFactory valueFactory = ValueFactoryFactory.getValueFactory();
			new ValueUI(valueFactory).text(tree, valueFactory.integer(2));
		}
	}
}
