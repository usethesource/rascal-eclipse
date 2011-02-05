package org.rascalmpl.eclipse.library.vis;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.rascalmpl.library.vis.FigurePApplet;

public class FigureEditorInput implements IEditorInput {
	private final FigurePApplet figurePApplet;

	public FigureEditorInput(FigurePApplet figurePApplet) {
		this.figurePApplet = figurePApplet;
	}
	
	public boolean exists() {
		return figurePApplet != null;
	}

	public FigurePApplet getFigurePApplet() {
		// new Printer(figurePApplet.g.image.getGraphics().
		// new GC(new Printer()).drawImage(figurePApplet.g.image, x, y);
		return figurePApplet;
	}
	
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return  figurePApplet.getName();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return figurePApplet.getName();
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}

}
