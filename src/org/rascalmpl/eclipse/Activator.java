/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Bert Lisser - Bert.Lisser@cwi.nl (CWI)
 *   * Emilie Balland - (CWI)
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse;

import static org.rascalmpl.eclipse.IRascalResources.ID_RASCAL_ECLIPSE_PLUGIN;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.tools.ToolProvider;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.imp.runtime.PluginBase;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class Activator extends PluginBase {
	public static final String PLUGIN_ID = ID_RASCAL_ECLIPSE_PLUGIN;
	public static final String kLanguageName = "Rascal";
	private static Activator sInstance;
	
	public Activator() {
		super();
		Activator.getInstance(); // Stupid ...
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		sInstance = this;
		super.start(context);
	}
	
  public static ImageDescriptor getRascalImage() {
		return getInstance().getImageRegistry().getDescriptor(IRascalResources.RASCAL_DEFAULT_IMAGE);
	}
	
	public static Activator getInstance() {
		return sInstance;
	}

	public String getID() {
		return PLUGIN_ID;
	}
	
	@Override
	public String getLanguageID() {
		return kLanguageName;
	}

	// Definitions for image management

	public static final org.eclipse.core.runtime.IPath ICONS_PATH 
	  = new org.eclipse.core.runtime.Path("icons/"); //$NON-NLS-1$("icons/"); //$NON-NLS-1$

	protected void initializeImageRegistry(ImageRegistry reg) {
		IPath path = ICONS_PATH.append("rascal3D_2-32px.png");//$NON-NLS-1$
		ImageDescriptor imageDescriptor = createImageDescriptor(Platform.getBundle(PLUGIN_ID), path);
		reg.put(IRascalResources.RASCAL_DEFAULT_IMAGE, imageDescriptor);

		path = ICONS_PATH.append("rascal_default_outline_item.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(Platform.getBundle(PLUGIN_ID), path);
		reg.put(IRascalResources.RASCAL_DEFAULT_OUTLINE_ITEM, imageDescriptor);

		path = ICONS_PATH.append("rascal_file.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(Platform.getBundle(PLUGIN_ID), path);
		reg.put(IRascalResources.RASCAL_FILE, imageDescriptor);
		
		path = ICONS_PATH.append("ambidexter-a-16.png");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(Platform.getBundle(PLUGIN_ID), path);
		reg.put(IRascalResources.AMBIDEXTER, imageDescriptor);
		
		path = ICONS_PATH.append("rascal_default_image.gif");//$NON-NLS-1$
		imageDescriptor = createImageDescriptor(Platform.getBundle(PLUGIN_ID), path);
		reg.put(IRascalResources.COPY_TO_CONSOLE, imageDescriptor);

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
	
	public void checkRascalRuntimePreconditions() {
		checkRascalRuntimePreconditions(null);
	}
	
	public void checkRascalRuntimePreconditions(IProject project) {
		List<String> errors = new LinkedList<String>();
	
		if (ToolProvider.getSystemJavaCompiler() == null) {
			errors.add("Rascal needs a Java Development Kit (JDK), not just a Java Run-time Environment (JRE).\n\tPlease make sure Eclipse uses a JDK.");
		}
		
		if (!errors.isEmpty()) {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			StringBuilder message = new StringBuilder();
			for (String e : errors) {
				message.append(e + "\n");
			}
			MessageDialog.openError(shell, "Sorry, Rascal has some problems", message.toString());

			Activator.getInstance().logException("Rascal preconditions failed", new Exception(message.toString()));
		}
	}
	
	public static void log(String msg, Throwable t) {
		getInstance().logException(msg, t);
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
