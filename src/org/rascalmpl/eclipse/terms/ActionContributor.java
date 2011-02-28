package org.rascalmpl.eclipse.terms;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.services.ILanguageActionsContributor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Point;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.types.RascalTypeFactory;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.uptr.ProductionAdapter;
import org.rascalmpl.values.uptr.TreeAdapter;

public class ActionContributor implements ILanguageActionsContributor {
	private final static TypeFactory TF = TypeFactory.getInstance();
	private final static RascalTypeFactory RTF = RascalTypeFactory.getInstance();
	private final static IValueFactory VF = ValueFactoryFactory.getValueFactory();

	public void contributeToEditorMenu(UniversalEditor editor,
			IMenuManager menuManager) {
		ISet contribs = getContribs(editor);
		
		for (IValue contrib : contribs) {
			IConstructor node = (IConstructor) contrib;
			if (node.getName().equals("popup")) {
				contribute(menuManager, editor, (IConstructor) node.get("menu"));
			}
		}
	}

	private void contribute(IMenuManager menuManager, final UniversalEditor editor, IConstructor menu) {
		String label = ((IString) menu.get("label")).getValue();
		
		if (menu.getName().equals("action")) {
			contributeAction(menuManager, editor, menu, label);
		}
		else if (menu.getName().equals("group")) {
			menuManager.add(new Separator(label));
			for (IValue member : (IList) menu.get("members")) {
				contribute(menuManager, editor, (IConstructor) member);
			}
		}
		else if (menu.getName().equals("menu")) {
			MenuManager sub = new MenuManager(label);
			menuManager.add(sub);
			for (IValue member : (IList) menu.get("members")) {
				contribute(sub, editor, (IConstructor) member);
			}
		}
	}

	private void contributeAction(IMenuManager menuManager,
			final UniversalEditor editor, IConstructor menu, String label) {
		final ICallableValue func = (ICallableValue) menu.get("action");
		menuManager.add(new Action(label) {
			@Override
			public void run() {
				IConstructor tree = (IConstructor) editor.getParseController().getCurrentAst();
				Point selection = editor.getSelection();
				
				if (tree != null) {
					Type[] actualTypes = new Type[] { RTF.nonTerminalType(ProductionAdapter.getRhs(TreeAdapter.getProduction(tree))), TF.sourceLocationType() };
					ISourceLocation loc = TreeAdapter.getLocation(tree);
					IValue[] actuals = new IValue[] { tree, VF.sourceLocation(loc.getURI(), selection.x, selection.y, -1, -1, -1, -1)};
					try {
						IConstructor newTree = (IConstructor) func.call(actualTypes, actuals).getValue();
					
						if (newTree != null && newTree != tree) {
							try {
								String newText = TreeAdapter.yield(newTree);
								IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
								doc.replace(0, doc.getLength(), newText);
								if (selection.x < doc.getLength()) {
									editor.selectAndReveal(selection.x, 0);
								}
							} catch (BadLocationException e) {
								Activator.getInstance().logException("could not replace text", e);
							}
						}
					}
					catch (Throwable e) {
						Activator.getInstance().logException("error while executing action", e);
					}
				}
			}
		});
	}

	private ISet getContribs(UniversalEditor editor) {
		ISet result = TermLanguageRegistry.getInstance().getContributions(editor.getLanguage());
		if (result == null) {
			result = ValueFactoryFactory.getValueFactory().set();
		}
		return result;
	}

	public void contributeToMenuBar(UniversalEditor editor, IMenuManager menuManager) {
		ISet contribs = getContribs(editor);
		
		for (IValue contrib : contribs) {
			IConstructor node = (IConstructor) contrib;
			if (node.getName().equals("menu")) {
				contribute(menuManager, editor, (IConstructor) node.get("menu"));
			}
		}
	}

	public void contributeToStatusLine(UniversalEditor editor,
			IStatusLineManager statusLineManager) {

	}

	public void contributeToToolBar(UniversalEditor editor,
			IToolBarManager toolbarManager) {

	}

}
