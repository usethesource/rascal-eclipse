package org.meta_environment.rascal.eclipse;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.imp.runtime.PluginBase;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.osgi.framework.Bundle;

import sglr.SGLRInvoker;

public class Activator extends PluginBase {
	public static final String PLUGIN_ID = "rascal_eclipse";
	public static final String kLanguageName = "Rascal";
	
	private final static String RASCAL_BASE_LIBRARY_PATH = "baseLibraryPath";
	
	public Activator() {
		super();
		
		Activator.getInstance(); // Stupid ...
	}

	private static class InstanceKeeper {
		private final static Activator sInstance = new Activator();
		static{
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint point = registry.getExtensionPoint(PLUGIN_ID, RASCAL_BASE_LIBRARY_PATH);
			
			IExtension extensions[] = point.getExtensions();
			if(extensions.length > 0){
				IConfigurationElement[] binaryProviderElements = extensions[0].getConfigurationElements();
				IConfigurationElement ce = binaryProviderElements[0];
				String bundle = ce.getAttribute("bundle");
				String path = ce.getAttribute("path");
				
				String baseLibraryPath = getFile(Platform.getBundle(bundle), path);
				
				SGLRInvoker.setBaseLibraryPath(baseLibraryPath); // Set the base library path
			}
		}
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
		ImageDescriptor imageDescriptor = createImageDescriptor(getInstance().getBundle(), path);
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

	public static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path) {
		URL url = org.eclipse.core.runtime.FileLocator.find(bundle, path, null);
		if (url != null) {
			return org.eclipse.jface.resource.ImageDescriptor.createFromURL(url);
		}
		return null;
	}

	// Definitions for image management end
	
	public static String getFile(Bundle bundle, String path){
		String fileURL;
		try{
			fileURL = FileLocator.toFileURL(bundle.getEntry(path)).getPath();
		}catch(Exception ex){
			try{
				fileURL = FileLocator.toFileURL(bundle.getResource(path)).getPath();
			}catch(Exception ex2){
				fileURL = FileLocator.find(bundle, new Path(path), null).getPath();
			}
		}
		return fileURL;
	}
}
