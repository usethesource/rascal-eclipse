/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Bert Lisser - Bert.Lisser@cwi.nl (CWI)
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI
*******************************************************************************/
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
