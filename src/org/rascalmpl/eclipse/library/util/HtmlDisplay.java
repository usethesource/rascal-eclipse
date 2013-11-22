/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bert Lisser    - Bert.Lisser@cwi.nl
 *******************************************************************************/
package org.rascalmpl.eclipse.library.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.rascalmpl.interpreter.IEvaluatorContext;

public class HtmlDisplay {
	
	@SuppressWarnings("unused")
	private final IValueFactory vf;

	public HtmlDisplay(IValueFactory values) {
		super();
		this.vf = values;
	}

	public static void browse(URL loc) {
		IWebBrowser browser;
		try {
			IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench()
					.getBrowserSupport();
			browser = browserSupport.createBrowser(
					IWorkbenchBrowserSupport.AS_EDITOR,
					"dotplugin.editors.DotEditor", loc.getFile(), null);
			browser.openURL(loc);
			// browser.close();
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private IFile getHtmlOutputLoc(ISourceLocation loc) throws IOException {
		IFile output = null;
		if (loc.getURI().getScheme().equals("project")) {
			IPath path = new Path(loc.getURI().getPath());
			if (path.getFileExtension() == null
					|| !path.getFileExtension().equals("html")
					&& !path.getFileExtension().equals("json"))
				path = path.append("index.html");
			String projName = loc.getURI().getAuthority();
			IProject p = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projName);
			output = p.getFile(path);

		}
		if (output == null)
			throw new IOException("Invalid uri:" + loc.getURI());
		return output;
	}

	public void htmlDisplay(ISourceLocation loc) throws IOException {
		final IFile output = getHtmlOutputLoc(loc);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					browse(output.getLocationURI().toURL());
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
	}

	private void htmlDisplay(ISourceLocation loc, String input,
			IEvaluatorContext ctx) throws IOException {
		final IFile output = getHtmlOutputLoc(loc);
		try {
			if (output.exists())
				output.delete(true, null);
			if (output.getParent().getType() == IResource.FOLDER) {
				IFolder f = (IFolder) output.getParent();
				if (!f.exists()) {
					f.create(true, false, null);
				}
			}
			InputStream is = new ByteArrayInputStream(input.getBytes("UTF-8"));
			output.create(is, true, null);
			htmlDisplay(loc);			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void htmlDisplay(ISourceLocation loc, IString input,
			IEvaluatorContext ctx) throws IOException {
		String s = input.getValue();
		htmlDisplay(loc, s, ctx);
	}

}
