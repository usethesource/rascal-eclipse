package org.meta_environment.rascal.eclipse.outline;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.services.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.meta_environment.rascal.ast.AbstractAST;
import org.meta_environment.rascal.ast.Declaration;
import org.meta_environment.rascal.ast.FunctionDeclaration;
import org.meta_environment.rascal.ast.Module;
import org.meta_environment.uptr.TreeAdapter;

public class LabelProvider implements ILabelProvider, ILanguageService  {
	private Set<ILabelProviderListener> fListeners = new HashSet<ILabelProviderListener>();

//	private static ImageRegistry sImageRegistry = Activator.getInstance()
//			.getImageRegistry();
//
//	private static Image DEFAULT_IMAGE = sImageRegistry
//			.get(IRascalResources.RASCAL_DEFAULT_IMAGE);
//
//	@Override
//	public Image getImage(Object element) {
//		return DEFAULT_IMAGE;
//	}

	@Override
	public Image getImage(Object element) {
		return null;
	}
	
	public LabelProvider() {
	}
	
	@Override
	public String getText(Object element) {
		if (element instanceof ModelTreeNode) {
			ModelTreeNode node = (ModelTreeNode) element;
			
			Object node2 = node.getASTNode();
			
			if (node2 instanceof AbstractAST) {
				return getLabelFor((AbstractAST) node2);
			}
			else if (node2 instanceof IConstructor) {
				return getLabelFor((IConstructor) node2);
			}
		}
		else if (element instanceof IConstructor) {
			return getLabelFor((IConstructor) element);
		}
		return "***";
	}

	private String getLabelFor(AbstractAST node2) {
		String result;
		
		if (node2 instanceof Module) {
			result = ((Module) node2).getHeader().toString();
		}
		else if (node2 instanceof Declaration.Function) {
			result = ((Declaration.Function) node2).getFunctionDeclaration().getSignature().toString();
		}
		else if (node2 instanceof FunctionDeclaration) {
			result = ((FunctionDeclaration) node2).getSignature().toString();
		}
		else if (node2 instanceof Declaration.Variable) {
			result = node2.toString();
		}
		else if (node2 instanceof Declaration.Data) {
			result = ((Declaration.Data) node2).toString();
		}
		else if (node2 instanceof Declaration.Rule) {
			result = "rule " + ((Declaration.Rule) node2).getName().toString();
		}
		else {
		    result = node2.toString();
		}
		return result.replaceAll("\n", " ").trim();
	}

	private String getLabelFor(IConstructor node) {
		return new TreeAdapter(node).yield();
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		fListeners.add(listener);
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		fListeners.remove(listener);
	}
}
