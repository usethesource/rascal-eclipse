/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Various members of the Software Analysis and Transformation Group - CWI
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI  
 *******************************************************************************/
package org.rascalmpl.eclipse.editor;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;

public class SubMenu extends MenuManager {
	public SubMenu(IMenuManager parent, String title) {
		super(title, 
				Activator.getInstance().getImageRegistry()
				.getDescriptor(IRascalResources.RASCAL_DEFAULT_IMAGE),
				IRascalResources.ID_RASCAL_ECLIPSE_PLUGIN + "." + title);
		parent.add(this);
	}
}
