/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.jface.action;

import org.dancingbear.graphbrowser.layout.dot.DotLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Re-layout graph
 * 
 * @author Jeroen van Schagen
 * @date 03-03-2009
 */
public class DotlayoutAction extends LayoutAction {

	public DotlayoutAction(IWorkbenchPage page) {
		super(page);
	}

	/**
	 * Execute dot layout
	 */
	@Override
	public void run() {
		applyLayout(new DotLayout());
	}

	/**
	 * Get tooltip text of action
	 * 
	 * @return tooltipText
	 */
	@Override
	public String getToolTipText() {
		return "Apply Dot Layout on the graph";
	}

	/**
	 * Get text of action
	 * 
	 * @return text
	 */
	@Override
	public String getText() {
		return "Dot Layout";
	}

	/**
	 * Get image descriptor of action (icon)
	 * 
	 * @return imageDescriptor
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

}