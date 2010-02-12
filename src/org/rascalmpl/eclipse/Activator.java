package org.rascalmpl.eclipse;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.imp.runtime.PluginBase;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.osgi.framework.Bundle;
import org.rascalmpl.eclipse.console.ProjectURIResolver;
import org.rascalmpl.eclipse.editor.SDFParseController;
import org.rascalmpl.uri.URIResolverRegistry;

import sglr.LegacySGLRInvoker;
import sglr.SGLRInvoker;

public class Activator extends PluginBase {
	public static final String PLUGIN_ID = "rascal_eclipse";
	public static final String kLanguageName = "Rascal";
	
	private final static String RASCAL_BASE_LIBRARY_PATH = "baseLibraryPath";
	private final static String RASCAL_BASE_BINARY_PATH = "baseBinaryPath";
	
	public Activator() {
		super();
		Activator.getInstance(); // Stupid ...
	}

	private static class InstanceKeeper {
		private final static Activator sInstance = new Activator();
		// Base Paths
		static{
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint baseLibraryPoint = registry.getExtensionPoint(PLUGIN_ID, RASCAL_BASE_LIBRARY_PATH);
			
			IExtension baseLibraryxtensions[] = baseLibraryPoint.getExtensions();
			if(baseLibraryxtensions.length > 0){
				IConfigurationElement[] binaryProviderElements = baseLibraryxtensions[0].getConfigurationElements();
				IConfigurationElement ce = binaryProviderElements[0];
				String bundle = ce.getAttribute("bundle");
				String path = ce.getAttribute("path");
				
				String baseLibraryPath = getFile(Platform.getBundle(bundle), path);
				
				SGLRInvoker.setBaseLibraryPath(baseLibraryPath); // Set the base library path
			}
			
			IExtensionPoint baseBinaryPoint = registry.getExtensionPoint(PLUGIN_ID, RASCAL_BASE_BINARY_PATH);
			
			IExtension baseBinaryExtensions[] = baseBinaryPoint.getExtensions();
			if(baseBinaryExtensions.length > 0){
				IConfigurationElement[] binaryProviderElements = baseBinaryExtensions[0].getConfigurationElements();
				IConfigurationElement ce = binaryProviderElements[0];
				String bundle = ce.getAttribute("bundle");
				String path = ce.getAttribute("path");
				
				String baseBinaryPath = getFile(Platform.getBundle(bundle), path);
				
				LegacySGLRInvoker.setBaseBinaryPath(baseBinaryPath); // Set the base binary path
			}
		}
		// Rascal Paths
		static{
			Bundle rascalPluginBundle = Platform.getBundle("rascal_plugin");
			
			System.setProperty("rascal.parsetable.default.file", getFile(rascalPluginBundle, "installed/share/rascal-grammar/rascal.tbl"));
			System.setProperty("rascal.parsetable.header.file", getFile(rascalPluginBundle, "installed/share/rascal-grammar/rascal-header.tbl"));
			System.setProperty("rascal.rascal2table.command", getFile(rascalPluginBundle, "installed/bin/rascal2table"));
			System.setProperty("rascal.rascal2table.dir", getFile(rascalPluginBundle, "installed/bin"));

			System.setProperty("rascal.sdf.library.dir", getFile(rascalPluginBundle, "installed/share/sdf-library/library"));

			System.setProperty("rascal.parsetable.cache.dir", System.getProperty("java.io.tmpdir"));
			
			// SDF parse table
			SDFParseController.setParseTable(getFile(rascalPluginBundle, "installed/share/pgen/Sdf2.saf"));
			
			// project scheme
			ProjectURIResolver resolver = new ProjectURIResolver();
			URIResolverRegistry registry = URIResolverRegistry.getInstance();
			registry.registerInput(resolver.scheme(), resolver);
			registry.registerOutput(resolver.scheme(), resolver);
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

	// Definitions for image management end
	
	public static String getFile(Bundle bundle, String path){
		String fileURL;
		try{
			fileURL = FileLocator.toFileURL(bundle.getEntry(path)).getPath();
		}catch(Exception ex){
			try{
				return FileLocator.toFileURL(bundle.getResource(path)).getPath();
			}catch(Exception ex2){
				try{
					URL file = FileLocator.find(bundle, new Path(path), null);
					
					if (file != null) {
						fileURL = file.getPath();
					}
					else {
						RuntimeException fnf = new RuntimeException("Can't find:" + bundle+" / "+path);
						getInstance().logException(fnf.getMessage(), fnf);
						throw fnf;
					}
				}catch(RuntimeException rex){
					getInstance().logException("Can't find: "+bundle+" / "+path, rex);
					throw rex;
				}
			}
		}
		return fileURL;
	}
}
