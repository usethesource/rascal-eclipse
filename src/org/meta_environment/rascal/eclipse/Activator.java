package org.meta_environment.rascal.eclipse;

import org.eclipse.imp.runtime.PluginBase;

public class Activator extends PluginBase {
	public static final String kPluginID = "rascal_eclipse";
	public static final String kLanguageName = "Rascal";

	private static class InstanceKeeper {
		static Activator sInstance = new Activator();
	}

	public static Activator getInstance() {
		return InstanceKeeper.sInstance;
	}

	public String getID() {
		return kPluginID;
	}

	@Override
	public String getLanguageID() {
		return kLanguageName;
	}

	// Definitions for image management

	public static final org.eclipse.core.runtime.IPath ICONS_PATH = new org.eclipse.core.runtime.Path(
			"icons/"); //$NON-NLS-1$("icons/"); //$NON-NLS-1$

	protected void initializeImageRegistry(
			org.eclipse.jface.resource.ImageRegistry reg) {
		org.eclipse.core.runtime.IPath path = ICONS_PATH
				.append("rascal_default_image.gif");//$NON-NLS-1$
		org.eclipse.jface.resource.ImageDescriptor imageDescriptor = createImageDescriptor(
				getInstance().getBundle(), path);
		reg.put(IRascalResources.RASCAL_DEFAULT_IMAGE, imageDescriptor);

		path = ICONS_PATH.append("rascal_default_outline_item.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(getInstance().getBundle(), path);
		reg.put(IRascalResources.RASCAL_DEFAULT_OUTLINE_ITEM, imageDescriptor);

		path = ICONS_PATH.append("rascal_file.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(getInstance().getBundle(), path);
		reg.put(IRascalResources.RASCAL_FILE, imageDescriptor);

		path = ICONS_PATH.append("rascal_file_warning.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(getInstance().getBundle(), path);
		reg.put(IRascalResources.RASCAL_FILE_WARNING, imageDescriptor);

		path = ICONS_PATH.append("rascal_file_error.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(getInstance().getBundle(), path);
		reg.put(IRascalResources.RASCAL_FILE_ERROR, imageDescriptor);
	}

	public static org.eclipse.jface.resource.ImageDescriptor createImageDescriptor(
			org.osgi.framework.Bundle bundle,
			org.eclipse.core.runtime.IPath path) {
		java.net.URL url = org.eclipse.core.runtime.FileLocator.find(bundle,
				path, null);
		if (url != null) {
			return org.eclipse.jface.resource.ImageDescriptor
					.createFromURL(url);
		}
		return null;
	}

	// Definitions for image management end

}
