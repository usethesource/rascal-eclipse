package org.rascalmpl.eclipse.editor;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.imp.editor.IRegionSelectionService;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.rascalmpl.eclipse.Activator;

public class SourceLocationHyperlink implements IHyperlink {
	private final ISourceLocation loc;

	public SourceLocationHyperlink(ISourceLocation loc) {
		this.loc = loc;
	}
	
	public IRegion getHyperlinkRegion() {
		return new Region(loc.getOffset(), loc.getLength());
	}

	public String getHyperlinkText() {
		return null;
	}

	public String getTypeLabel() {
		return null;
	}

	public void open() {
		if (loc == null) {
			return;
		}
		
	 	IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

		if (win == null && wb.getWorkbenchWindowCount() != 0) {
			win = wb.getWorkbenchWindows()[0];
		}
		
		if (win != null) {
			final IWorkbenchPage page = win.getActivePage();

			if (page != null) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						try {
							IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(loc.getURI().getPath());
							IEditorPart part;
							
							if (desc != null) {
								part = page.openEditor(getEditorInput(loc.getURI()), desc.getId());
								ISelectionProvider sp = part.getEditorSite().getSelectionProvider();
								if (sp != null) {
									sp.setSelection(new TextSelection(loc.getOffset(), loc.getLength()));
								}
								else {
									Activator.getInstance().logException("no selection provider", new RuntimeException());
								}
							}
							else {
								IFileStore fileStore = EFS.getLocalFileSystem().getStore(loc.getURI());
							    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							    part = IDE.openEditorOnFileStore(page, fileStore);
							}

							if (part != null) {
								IRegionSelectionService ss = (IRegionSelectionService) part.getAdapter(IRegionSelectionService.class);
								ss.selectAndReveal(loc.getOffset(), loc.getLength());
							}
						} catch (PartInitException e) {
							Activator.getInstance().logException("failed to open editor for source loc:" + loc, e);
						}
					}

					private IEditorInput getEditorInput(URI uri) {
						String scheme = uri.getScheme();
						
						if (scheme.equals("project")) {
							IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.getHost());
							
							if (project != null) {
								return new FileEditorInput(project.getFile(uri.getPath()));
							}
							
							Activator.getInstance().logException("project " + uri.getHost() + " does not exist", new RuntimeException());
						}
						else if (scheme.equals("file")) {
							IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
							IFile[] cs = root.findFilesForLocationURI(uri);
							
							if (cs != null && cs.length > 0) {
								return new FileEditorInput(cs[0]);
							}
							
							Activator.getInstance().logException("file " + uri + " not found", new RuntimeException());
						}
						
						Activator.getInstance().logException("scheme " + uri.getScheme() + " not supported", new RuntimeException());
						return null;
					}
				});
			}
		}
	}

}
