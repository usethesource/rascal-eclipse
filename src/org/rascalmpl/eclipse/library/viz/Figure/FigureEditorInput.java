package org.rascalmpl.eclipse.library.viz.Figure;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.rascalmpl.library.viz.Figure.FigurePApplet;

public class FigureEditorInput implements IEditorInput {
	private final FigurePApplet vlpapplet;

	public FigureEditorInput(FigurePApplet vlpapplet) {
		this.vlpapplet = vlpapplet;
	}
	
	public boolean exists() {
		return vlpapplet != null;
	}

	public FigurePApplet getVLPApplet() {
		return vlpapplet;
	}
	
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return "XXX"; //vlapplet.getTitle().getText();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "XXX"; //vlapplet.getTitle().getText();
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}

}
