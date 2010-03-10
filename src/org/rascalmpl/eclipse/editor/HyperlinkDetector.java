package org.rascalmpl.eclipse.editor;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.services.ISourceHyperlinkDetector;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rascalmpl.values.uptr.ParsetreeAdapter;
import org.rascalmpl.values.uptr.TreeAdapter;

public class HyperlinkDetector implements ISourceHyperlinkDetector {
	private static final IHyperlink[] empty = new IHyperlink[0];

	public IHyperlink[] detectHyperlinks(IRegion region, ITextEditor editor,
			ITextViewer textViewer, IParseController parseController) {
		IConstructor tree = (IConstructor) parseController.getCurrentAst();
		
		if (tree != null) {
			IHyperlink link = getTreeLinks(ParsetreeAdapter.getTop(tree), region);
			
			if (link != null) {
				return new IHyperlink[] { link };
			}
		}
		
		return empty;
	}

	private IHyperlink getTreeLinks(IConstructor tree, IRegion region) {
		IConstructor lexical = TreeAdapter.locateLexical(tree, region.getOffset());
		
		if (lexical != null) {
			IValue link = lexical.getAnnotation("link");
			
			if (link != null && link.getType().isSourceLocationType()) { 
				return new SourceLocationHyperlink((ISourceLocation) link);
			}
		}
		
		return null;
	}
}
