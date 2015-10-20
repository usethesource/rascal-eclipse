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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.uri.URIResourceResolver;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.value.IString;
import org.rascalmpl.value.IValueFactory;
import org.rascalmpl.values.ValueFactoryFactory;

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
					"htmldisplay", loc.getFile(), null);
			browser.openURL(loc);	
			// browser.close();
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private URI getHtmlOutputLoc(ISourceLocation loc, String input, IEvaluatorContext ctx)
			throws IOException {
		IFile output = null;
		URI inputUri = loc.getURI();
		
		if (inputUri.getScheme().equals("http")) {
			return inputUri;
		}
		
		URI resourceUri = loc.getURI();
		
		URI uri = inputUri.getScheme().equals("project")?inputUri:resourceUri;
		IPath path = new Path(uri.getPath());
		if (path.getFileExtension() == null
				|| !path.getFileExtension().equals("html")
				&& !path.getFileExtension().equals("json"))
			path = path.append("index.html");
		try {
			uri = URIUtil.changePath(uri, path.toString());
		} catch (URISyntaxException e1) {
			throw new IOException("Invalid uri:" + uri);
		}
		// System.err.println("getHtmlOutputLoc:"+path);
		if (uri.getScheme().equals("project")) {
			try {
				IResource res = URIResourceResolver.getResource(ValueFactoryFactory.getValueFactory().sourceLocation(uri));
				// System.err.println("Test File:"+res);
				if (res==null || res.getType()!=IResource.FILE) {
					throw new IOException("Invalid uri:" + loc.getURI());
				}
				output = (IFile) res;			
				if (input != null) {
					if (output.exists())
						output.delete(true, null);
					if (output.getParent().getType() == IResource.FOLDER) {
						IFolder f = (IFolder) output.getParent();
						if (!f.exists()) {
							System.err.println("Create");
							f.create(true, false, null);
						}
						InputStream is = new ByteArrayInputStream(
								input.getBytes("UTF-8"));
						output.create(is, true, null);
					}			
				}
				return output.getLocationURI();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}
		if (resourceUri!=null && resourceUri.getScheme().equals("file")) {
			File f = new File(path.toOSString());
			if (input != null) {
				f.getParentFile().mkdir();
				FileWriter w = new FileWriter(f);
				w.write(input);
				w.close();			
			}
			return f.toURI();
		}
		return null;	
	}

	public void htmlDisplay(ISourceLocation loc, IEvaluatorContext ctx) throws IOException {
		final URI output = getHtmlOutputLoc(loc, null, ctx);
		// System.err.println("htmlDisplay browse2:"+output);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					browse(output.toURL());
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
		final URI uri = getHtmlOutputLoc(loc, input, ctx);
		// System.err.println("htmlDisplay browse:"+uri);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					browse(uri.toURL());
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
	}

	public void _htmlDisplay(ISourceLocation loc, IString input,
			IEvaluatorContext ctx) throws IOException {
		String s = input.getValue();
		htmlDisplay(loc, s, ctx);
	}

}
