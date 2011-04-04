/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.library.vis;

import java.net.URI;
import java.util.Iterator;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.library.vis.FigureColorUtils;
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
	 * Local declarations for annotations and markers
	 */
	
	private final int LINE_HIGHLIGHT_LENGTH = 5;
	
	// This following list of annotations has to be in sync with the annotations declared in plugin.xml
	
	private final static String[] RASCAL_LINE_HIGHLIGHT = 
	{ "rascal.highlight0",
	  "rascal.highlight1",
	  "rascal.highlight2",
	  "rascal.highlight3",
	  "rascal.highlight4"
	};
	
	private final static String RASCAL_MARKER = "rascal.markerType.queryResult";
	
	/**
	 * Define a list of custom colors for line highlights in the editor
	 * @param colors	The list of colors
	 */
	public void setHighlightColors(final IList colors){
		
		FigureColorUtils.setHighlightColors(colors);
		
		IPreferenceStore prefStore = EditorsUI.getPreferenceStore();

		for(int i = 0; i < colors.length() && i < LINE_HIGHLIGHT_LENGTH; i++){
			int color = ((IInteger)colors.get(i)).intValue();
			String prefKey = "Highlight" + i + "ColorPreferenceKey";
			PreferenceConverter.setValue(prefStore, prefKey, FigureColorUtils.toRGB(color));
		}		
	}
	
	/**
	 * Start an editor for a given source location
	 * 
	 * @param loc		The source location to be edited
	 * @param lineInfo	A list of LineDecorations (see Render.rsc)
	 * 
	 * Code is cloned from SourceLocationHyperlink (but heavily adapted)
	 */
	
	public void edit(final ISourceLocation loc, final IList lineInfo) {

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
								IDocumentProvider documentProvider = editor.getDocumentProvider();
								IDocument document = documentProvider.getDocument(editor.getEditorInput());

								if (document != null) {

									// First delete old markers

									IEditorInput input = editor.getEditorInput();
									IResource inputResource = ResourceUtil.getResource(input);
									for(IMarker marker : inputResource.findMarkers(RASCAL_MARKER, true, IResource.DEPTH_INFINITE)){
										if(marker.exists())
											marker.delete();
									}

									// ... and old annotations

									IAnnotationModel annotationModel = documentProvider.getAnnotationModel(editor.getEditorInput());

									// Lock on the annotation model
									Object lockObject = ((ISynchronizable) annotationModel).getLockObject();
									synchronized(lockObject){
										Iterator<Annotation> iter = annotationModel.getAnnotationIterator();
										while(iter.hasNext()){
											Annotation anno = iter.next();
											if(anno.getType().startsWith("rascal.highlight"))
												annotationModel.removeAnnotation(anno);
										}
									}		

									for(IValue v : lineInfo){
										IConstructor lineDecor = (IConstructor) v;
										int lineNumber = ((IInteger)lineDecor.get(0)).intValue();
										String msg = ((IString)lineDecor.get(1)).getValue();
										int severity = 0;
										boolean useMarker = true;
										
										String name = lineDecor.getName();
										if(name.equals("info"))
											severity = IMarker.SEVERITY_INFO;
										else if(name.equals("warning"))
											severity = IMarker.SEVERITY_WARNING;
										else if(name.equals("error"))
											severity = IMarker.SEVERITY_ERROR;
										else {
											useMarker = false;
										}

										if(useMarker){ // Add a marker
											IMarker marker = inputResource.createMarker(RASCAL_MARKER);
											marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
											marker.setAttribute(IMarker.MESSAGE, msg);
											marker.setAttribute(IMarker.LOCATION, "line " + lineNumber);
											marker.setAttribute(IMarker.SEVERITY, severity);
										} else {	// Add an annotation
											int highlightKind = 0;
											
											if(lineDecor.arity() > 2){
												highlightKind = ((IInteger)lineDecor.get(2)).intValue();
												if(highlightKind < 0)
													highlightKind = 0;
												if(highlightKind >= LINE_HIGHLIGHT_LENGTH)
													highlightKind = LINE_HIGHLIGHT_LENGTH - 1;
											}
						
											String highlightName = RASCAL_LINE_HIGHLIGHT[highlightKind];

											try {
												// line count internally starts with 0, and not with 1 like in GUI
												IRegion lineInfo = document.getLineInformation(lineNumber - 1);
												synchronized(lockObject){
													Annotation currentLine = new Annotation(highlightName, true, msg);
													Position currentPosition = new Position(lineInfo.getOffset(), lineInfo.getLength());
													annotationModel.addAnnotation(currentLine, currentPosition);
												}
											} catch (org.eclipse.jface.text.BadLocationException e) {
												// Ignore, lineNumber may just not exist in file
											}
										}
									}
								}  
							}
						} catch (PartInitException e) {
							Activator.getInstance().logException("failed to open editor for source loc:" + loc, e);
						} catch (CoreException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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
}
