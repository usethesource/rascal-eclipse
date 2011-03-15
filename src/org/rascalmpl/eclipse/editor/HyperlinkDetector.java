package org.rascalmpl.eclipse.editor;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.services.ISourceHyperlinkDetector;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rascalmpl.values.uptr.TreeAdapter;

public class HyperlinkDetector implements ISourceHyperlinkDetector {

	public IHyperlink[] detectHyperlinks(IRegion region, ITextEditor editor,
			ITextViewer textViewer, IParseController parseController) {
		if (parseController == null) {
			return null;
		}
		
		IConstructor tree = (IConstructor) parseController.getCurrentAst();
		
		if (tree != null) {
			return getTreeLinks(tree, region);
		}
		
		return null;
	}

	private IHyperlink[] getTreeLinks(IConstructor tree, IRegion region) {
		IConstructor ref = TreeAdapter.locateAnnotatedTree(tree, "link", region.getOffset());
		
		if (ref != null) {
			IValue link = ref.getAnnotation("link");
			
			if (link != null && link.getType().isSourceLocationType()) { 
				return new IHyperlink[] { new SourceLocationHyperlink(TreeAdapter.getLocation(ref), (ISourceLocation) link) };
			}
			
			
		}
		
		ref = TreeAdapter.locateAnnotatedTree(tree, "links", region.getOffset());
		if (ref != null) {
			IValue links = ref.getAnnotation("links");
			if (links != null && links.getType().isSetType() && links.getType().getElementType().isSourceLocationType()) {
				IHyperlink[] a = new IHyperlink[((ISet) links).size()];
				int i = 0;
				for (IValue l : ((ISet) links)) {
					a[i++] = new SourceLocationHyperlink(TreeAdapter.getLocation(ref), (ISourceLocation) l);
				}
				return a;
			}
		}
		
		return null;
	}
}
