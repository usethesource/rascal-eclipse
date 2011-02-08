package org.rascalmpl.eclipse.library.vis;

import java.net.URI;

import javax.swing.text.BadLocationException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.imp.editor.IRegionSelectionService;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
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
import org.eclipse.ui.texteditor.ITextEditor;
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
	
	public void edit1(final ISourceLocation loc, IEvaluatorContext ctx) {
	
		org.eclipse.imp.pdb.ui.text.Editor.edit(loc);
	}

	
	/*
	 * Start an editor for the given source location
	 * Code is cloned from SourceLocationHyperlink (but heavily adapted)
	 */
	
	public void edit(final ISourceLocation loc, final IMap coloredLines, IEvaluatorContext ctx) {
		
	 	IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

		if (win == null && wb.getWorkbenchWindowCount() != 0) {
			win = wb.getWorkbenchWindows()[0];
		}
		
		if (win != null) {
			final IWorkbenchPage page = win.getActivePage();

			if (page != null) {
				System.err.println("loc = " + loc);
				System.err.println("URI = " + loc.getURI());
				System.err.println("Path = " + loc.getURI().getPath());
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						try {
							IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(loc.getURI().getPath());
							IEditorPart editorPart;
							
							if (desc != null) {
								editorPart = page.openEditor(getEditorInput(loc.getURI()), desc.getId());
							} else {
								IFileStore fileStore = EFS.getLocalFileSystem().getStore(loc.getURI());
							    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							    editorPart = IDE.openEditorOnFileStore(page, fileStore);
							}

							if (editorPart != null) {
								
								 if (!(editorPart instanceof ITextEditor)) {
									    return;
								  }
								  ITextEditor editor = (ITextEditor) editorPart;
								  IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
								  if (document != null) {
									  for(IValue v : coloredLines){
											int lineNumber = ((IInteger) v).intValue();
											int color = ((IInteger) coloredLines.get(v)).intValue();
									  
											IRegion lineInfo = null;
											try {
											// line count internally starts with 0, and not with 1 like in GUI
											lineInfo = document.getLineInformation(lineNumber - 1);
											} catch (org.eclipse.jface.text.BadLocationException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
											if (lineInfo != null) {
												editor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
											}
									  }							
								}
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
							IFile [] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
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
	
	/*
	 * Edit without map of coloredLines.
	 */
	
	public void edit(final ISourceLocation loc, IEvaluatorContext ctx) {
		TypeFactory tf = TypeFactory.getInstance();
		edit(loc, ctx.getValueFactory().map(tf.integerType(), tf.integerType()), ctx);
	}
}
