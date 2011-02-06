package org.rascalmpl.eclipse.library.vis;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.imp.editor.IRegionSelectionService;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.jface.text.TextSelection;
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
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.library.vis.FigurePApplet;

public class FigureLibrary {
	
	public FigureLibrary(IValueFactory values){
		super();
	}
	
	public void render(IConstructor fig, IEvaluatorContext ctx){
		FigurePApplet vlp = new FigurePApplet(fig, ctx);
		FigureViewer.open(vlp);
	}
	
	public void render(IString name, IConstructor fig, IEvaluatorContext ctx){
		FigurePApplet vlp = new FigurePApplet(name, fig, ctx);
		FigureViewer.open(vlp);
	}
	
	/*
	 * Start an editor for the given source location
	 * Code is cloned from SourceLocationHyperlink
	 */
	
	public void edit(final ISourceLocation loc, IEvaluatorContext ctx) {
		
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
						else if (scheme.equals("rascal-library")) {
							IFile [] files =ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
							if (files.length > 0) {
								return new FileEditorInput(files[0]);
							}
						}
						
						Activator.getInstance().logException("scheme " + uri.getScheme() + " not supported", new RuntimeException());
						return null;
					}
				});
			}
		}
	}

	
//	public void edit(ISourceLocation loc, IEvaluatorContext ctx){
//		File fileToOpen = new File("/Users/paulklint/test.txt");
//		 
//		if (fileToOpen.exists() && fileToOpen.isFile()) {
//			System.err.println("It exists");
//		    IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
//		    System.err.println("filestor made: " + fileStore);
//		    
//		    IWorkbench wb = PlatformUI.getWorkbench();
//			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
//
//			if (win == null && wb.getWorkbenchWindowCount() != 0) {
//				win = wb.getWorkbenchWindows()[0];
//			}
//
//			if (win != null){
//				final IWorkbenchPage page = win.getActivePage();
//		    
//				System.err.println("page found: " + page);
//				try {
//					IDE.openEditorOnFileStore( page, fileStore );
//				} catch ( PartInitException e ) {
//					e.printStackTrace();
//					throw RuntimeExceptionFactory.io(ctx.getValueFactory().string("Cannot open editor"),ctx.getCurrentAST(), ctx.getStackTrace());
//				}
//			}
//		} else
//			throw RuntimeExceptionFactory.pathNotFound(loc,ctx.getCurrentAST(), ctx.getStackTrace());
//	}
}
