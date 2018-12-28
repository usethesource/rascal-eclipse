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
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.WorkbenchJob;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.EditorUtil;
import org.rascalmpl.eclipse.editor.IDEServicesModelProvider;
import org.rascalmpl.eclipse.repl.EclipseIDEServices;
import org.rascalmpl.help.HelpManager;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.uri.ProjectURIResolver;
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
                        e.printStackTrace();
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
        m.refreshIndex(); // TODO pretty expensive but always up-to-date
        return Builder.getConceptURL("http", "localhost:" + m.getPort(), pcfg, concept);
    }
	
	private static class ConceptFileFinder implements IResourceProxyVisitor {
	    private IFile found = null;
        private final String parent;
        private final String concept;

	    public ConceptFileFinder(String parent, String concept) {
	        this.parent = parent;
	        this.concept = concept;
        }
	    
	    public boolean found() {
	        return found != null;
	    }
	    
	    public IFile getFile() {
	        return found;
	    }
	    
	    @Override
        public boolean visit(IResourceProxy proxy) throws CoreException {
            if (proxy.getName().equals(parent) && proxy.getType() == IResource.FOLDER) {
                IFolder conceptFolder = (IFolder) proxy.requestResource();

                // TODO fix hard-wired bin folder location
                if (conceptFolder.getProject().getFolder("bin").getFullPath().isPrefixOf(conceptFolder.getFullPath())) {
                    return false;
                }
                
                // TODO fix hard-wired bin folder location
                if (conceptFolder.getProject().getFolder("target").getFullPath().isPrefixOf(conceptFolder.getFullPath())) {
                    return false;
                }
                
                IFile conceptFile = concept == null 
                        ? conceptFolder.getFile(parent + ".concept")
                        : conceptFolder.getFolder(concept).getFile(concept + ".concept");
                    
                if (conceptFile != null && conceptFile.exists()) {
                    found = conceptFile;
                    return false;
                }
            }
            
            return true;
        }
	}
	
	private static IFile getConceptFile(String name) {
        String[] parts = name.split("-");
        
        if (parts.length == 0) {
            return null;
        }
        
        try {
            ConceptFileFinder finder = new ConceptFileFinder(parts[0], parts.length > 1 ? parts[1] : null);
            ResourcesPlugin.getWorkspace().getRoot().accept(finder,  IResource.NONE);
            if (finder.found()) {
                return finder.getFile();
            }
        } catch (CoreException e) {
            Activator.log("error while searching for concept file", e);
        }
	    
	    return null;
	}

    private static HelpManager getHelpManager(PathConfig pcfg, IProject project) throws IOException {
        synchronized (tutors) {
            HelpManager m = tutors.get(project);

            if (m == null) {
                PrintWriter out = new PrintWriter(RuntimePlugin.getInstance().getConsoleStream());
                PrintWriter err = new PrintWriter(RuntimePlugin.getInstance().getConsoleStream());
                ISourceLocation root = URIUtil.getChildLocation(pcfg.getBin(), "courses");
                m = new HelpManager(root, pcfg, out, err, new EclipseIDEServices(), true);
                tutors.put(project, m);
                
                // since it might fail I want to do this after caching the server
                try {
                    m.refreshIndex();
                }
                catch (IOException e) {
                    Activator.log("indexing the courses for " + project + " failed", e);
                }
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
	    int columns = 6;
	    GridLayout grid = new GridLayout(columns, true);
        parent.setLayout(grid);
	    
        Label currentConceptLabel = new Label(parent, SWT.NONE);
        currentConceptLabel.setText("Concept:");
        GridData ccLabel = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
        currentConceptLabel.setLayoutData(ccLabel);
        
	    Label currentConcept = new Label(parent, SWT.NONE);
	    currentConcept.setText("...");
	    currentConcept.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
	    
	    Button openConcept = new Button(parent, SWT.NONE);
	    openConcept.setText("Edit");
	    openConcept.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
	    openConcept.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IFile conceptFile = getConceptFile(currentConcept.getText());
                EditorUtil.openAndSelectURI(ProjectURIResolver.constructProjectURI(conceptFile.getFullPath()));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                /* nothing */
            }
	        
	    });
	    
	    Button back = new Button(parent, SWT.NONE);
	    back.setText("Back");
        back.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
        back.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (browser != null) {
                    browser.back();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                /* nothing */
            }
        });
        
        Button forward = new Button(parent, SWT.NONE);
        forward.setText("Forward");
        forward.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
        forward.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (browser != null) {
                    browser.forward();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                /* nothing */
            }
        });
        
        Button refresh = new Button(parent, SWT.NONE);
        refresh.setText("Refresh");
        refresh.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
        refresh.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (browser != null) {
                    browser.refresh();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                /* nothing */
            }
        });
	    
	    Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
	    separator.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, columns, 1));
	    
		browser = new Browser(parent, SWT.FILL);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, columns, 1));
		browser.setText("<html><body>Tutor preview is now ready for the first build result.</body></html>");
		
		browser.addLocationListener(new LocationListener() {
            @Override
            public void changing(LocationEvent event) { /*nothing*/ }

            @Override
            public void changed(LocationEvent event) {
                String[] split = event.location.split("#");
                
                if (split.length == 2) {
                    currentConcept.setText(split[1]);
                }
            }
		});
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
