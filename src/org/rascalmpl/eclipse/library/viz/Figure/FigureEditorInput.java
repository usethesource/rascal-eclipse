package org.rascalmpl.eclipse.library.viz.Figure;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.rascalmpl.library.viz.Figure.FigurePApplet;

public class FigureEditorInput implements IEditorInput {
	private final FigurePApplet figurePApplet;

	public FigureEditorInput(FigurePApplet figurePApplet) {
		this.figurePApplet = figurePApplet;
	}
	
	public boolean exists() {
		return figurePApplet != null;
	}

	public FigurePApplet getFigurePApplet() {
		return figurePApplet;
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
