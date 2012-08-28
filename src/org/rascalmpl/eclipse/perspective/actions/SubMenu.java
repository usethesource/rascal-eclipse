package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;

public class SubMenu extends MenuManager {
	public SubMenu(IMenuManager parent, String title) {
		super(title, Activator.getInstance().getImageRegistry().getDescriptor(IRascalResources.RASCAL_DEFAULT_IMAGE), "rascal_eclipse." + title);
		parent.add(this);
	}
}
