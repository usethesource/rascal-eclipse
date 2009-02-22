package org.meta_environment.rascal.eclipse.outline;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.services.ILabelProvider;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.meta_environment.rascal.eclipse.Activator;
import org.meta_environment.rascal.eclipse.IRascalResources;
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
			
			return getLabelFor((IConstructor) node.getASTNode());
		}
		else if (element instanceof IConstructor) {
			return getLabelFor((IConstructor) element);
		}
		return "***";
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
