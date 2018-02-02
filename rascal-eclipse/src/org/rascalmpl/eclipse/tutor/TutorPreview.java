/*******************************************************************************
 * Copyright (c) 2009-2018 NWO-I - CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *   * Various members of the Software Analysis and Transformation Group - CWI
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI  
 *******************************************************************************/
package org.rascalmpl.eclipse.tutor;

import static org.rascalmpl.eclipse.IRascalResources.ID_RASCAL_TUTOR_PREVIEW_PART;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.WorkbenchJob;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.IDEServicesModelProvider;
import org.rascalmpl.eclipse.repl.EclipseIDEServices;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.help.HelpManager;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.uri.URIUtil;

import io.usethesource.impulse.runtime.RuntimePlugin;
import io.usethesource.vallang.ISourceLocation;

public class TutorPreview extends ViewPart {
	public static final String ID = ID_RASCAL_TUTOR_PREVIEW_PART;
	private static final Map<IProject, HelpManager> tutors = new HashMap<>();
	private Browser browser;

	public TutorPreview() { 
	    // all views need a nullary constructor
	}
	
	public static void previewConcept(IFile concept) {
	    new WorkbenchJob("Loading concept " + concept) {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                try {
                    TutorPreview t = (TutorPreview) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(TutorPreview.ID);
                    
                    try {
                        t.gotoPage(getConceptPage(concept));
                    } 
                    catch (IOException | URISyntaxException e) {
                        t.setContent("<html><body>" + e.getMessage() + "</body></html>");   
                    }
                }
                catch (PartInitException e) {
                    Activator.getInstance().logException("Can not load concept " + concept, e);
                }
                
                return Status.OK_STATUS;
            }
        }.schedule();
    }

	private static URL getConceptPage(IFile concept) throws IOException, URISyntaxException {
	    PathConfig pcfg = IDEServicesModelProvider.getInstance().getPathConfig(concept.getProject());
        HelpManager m = getHelpManager(pcfg, concept.getProject());
        return Builder.getConceptURL("http", "localhost:" + m.getPort(), pcfg, concept);
    }

    private static HelpManager getHelpManager(PathConfig pcfg, IProject project) throws IOException {
        synchronized (tutors) {
            HelpManager m = tutors.get(project);

            if (m == null) {
                PrintWriter out = new PrintWriter(RuntimePlugin.getInstance().getConsoleStream());
                PrintWriter err = new PrintWriter(RuntimePlugin.getInstance().getConsoleStream());
                ISourceLocation root = URIUtil.getChildLocation(pcfg.getBin(), "courses");
                m = new HelpManager(root, pcfg, out, err, new EclipseIDEServices());
                tutors.put(project, m);
            }

            return m;
        }
    }

    private void setContent(String html) {
        new WorkbenchJob("Loading html") {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                browser.setText(html);
                return Status.OK_STATUS;
            }
        }.schedule();
    }
    
    public void gotoPage(URL page) {
        new WorkbenchJob("Loading concept page " + page) {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                browser.setUrl(page.toString());
                return Status.OK_STATUS;
            }
        }.schedule();
	}

	@Override
	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
		browser.setText("<html><body>Tutor preview is now ready for the first build result.</body></html>");
	}

	@Override
	public void setFocus() {
		browser.setFocus();
	}
	
	@Override
	public void dispose() {
	    for (HelpManager m : tutors.values()) {
	        m.stopServer();
	    }
	}
}
