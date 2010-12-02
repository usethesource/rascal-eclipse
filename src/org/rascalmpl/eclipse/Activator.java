package org.rascalmpl.eclipse;

import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.imp.runtime.PluginBase;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class Activator extends PluginBase {
	public static final String PLUGIN_ID = "rascal_eclipse";
	public static final String kLanguageName = "Rascal";
	
	public Activator() {
		super();
		Activator.getInstance(); // Stupid ...
	}

	private static class InstanceKeeper {
		private final static Activator sInstance = new Activator();
	}

	public static Activator getInstance() {
		return InstanceKeeper.sInstance;
	}

	public String getID() {
		return PLUGIN_ID;
	}

	@Override
	public String getLanguageID() {
		return kLanguageName;
	}

	// Definitions for image management

	public static final org.eclipse.core.runtime.IPath ICONS_PATH = new org.eclipse.core.runtime.Path(
			"icons/"); //$NON-NLS-1$("icons/"); //$NON-NLS-1$

	protected void initializeImageRegistry(ImageRegistry reg) {
		IPath path = ICONS_PATH.append("rascal_default_image.gif");//$NON-NLS-1$
		ImageDescriptor imageDescriptor = createImageDescriptor(Platform.getBundle(PLUGIN_ID), path);
		reg.put(IRascalResources.RASCAL_DEFAULT_IMAGE, imageDescriptor);

		path = ICONS_PATH.append("rascal_default_outline_item.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(Platform.getBundle(PLUGIN_ID), path);
		reg.put(IRascalResources.RASCAL_DEFAULT_OUTLINE_ITEM, imageDescriptor);

		path = ICONS_PATH.append("rascal_file.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(Platform.getBundle(PLUGIN_ID), path);
		reg.put(IRascalResources.RASCAL_FILE, imageDescriptor);

		path = ICONS_PATH.append("rascal_file_warning.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(Platform.getBundle(PLUGIN_ID), path);
		reg.put(IRascalResources.RASCAL_FILE_WARNING, imageDescriptor);

		path = ICONS_PATH.append("rascal_file_error.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(Platform.getBundle(PLUGIN_ID), path);
		reg.put(IRascalResources.RASCAL_FILE_ERROR, imageDescriptor);
	}

	public static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path) {
		URL url = org.eclipse.core.runtime.FileLocator.find(bundle, path, null);
		if (url != null) {
			return org.eclipse.jface.resource.ImageDescriptor.createFromURL(url);
		}
		return null;
	}
	
	public void logException(String msg, Throwable t) {
		if (msg == null) {
			if (t == null || t.getMessage() == null)
				msg = "No message given";
			else
				msg = t.getMessage();
		}

		Status status= new Status(IStatus.ERROR, PLUGIN_ID, 0, msg, t);

		getLog().log(status);
	}
}
