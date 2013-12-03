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
 *   * Davy Landman - Davy.Landman@cwi.nl - CWI
 *******************************************************************************/
package org.rascalmpl.eclipse.library.util;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
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
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
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
import org.rascalmpl.eclipse.util.RascalInvoker;
import org.rascalmpl.interpreter.IEvaluator;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.result.Result;
import org.rascalmpl.library.vis.util.FigureColorUtils;
import org.rascalmpl.values.ValueFactoryFactory;

public class Editors {
	/*
	 * Code is cloned from SourceLocationHyperlink (but heavily adapted)
	 */
	private abstract class DecoratorRunnerBase implements Runnable {
		private final ISourceLocation loc;
		private final IWorkbenchPage page;

		protected abstract IList getLineInfo();

		private DecoratorRunnerBase(ISourceLocation loc, IWorkbenchPage page) {
			this.loc = loc;
			this.page = page;
		}

		private IEditorPart cachedEditorPart = null;

		protected IEditorPart getEditorPart() throws PartInitException {
			// we cache the editor part because each openEditorOnFileStore /
			// openEditor will reactive that editor part
			// to avoid looping due to events fired by a editor part activation
			// we want to avoid that except for the first time.
			if (cachedEditorPart == null) {
				IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(loc.getURI().getPath());

				if (desc != null) {
					cachedEditorPart = page.openEditor(getEditorInput(loc.getURI()), desc.getId());
				} else {
					IFileStore fileStore = EFS.getLocalFileSystem().getStore(loc.getURI());
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					cachedEditorPart = IDE.openEditorOnFileStore(page, fileStore);
				}
			}
			return cachedEditorPart;
		}

		private IList previousList = null;
		private boolean firstTime = true;

		public void run() {
			try {
				IEditorPart editorPart = getEditorPart();
				if (editorPart != null && editorPart instanceof ITextEditor) {
					ITextEditor editor = (ITextEditor) editorPart;
					IDocumentProvider documentProvider = editor.getDocumentProvider();
					IDocument document = documentProvider.getDocument(editor.getEditorInput());
					if (document != null) {
						IList lines = getLineInfo();
						if (previousList != null && previousList.equals(lines)) {
							return; // nothing has changed, so we can avoid
									// removing and re-adding everything
						} else { //
							previousList = lines;
						}
						// First delete old markers

						IEditorInput input = editor.getEditorInput();
						IResource inputResource = ResourceUtil.getResource(input);
						for (IMarker marker : inputResource.findMarkers(RASCAL_MARKER, true, IResource.DEPTH_INFINITE)) {
							if (marker.exists()) {
								marker.delete();
							}
						}

						// ... and old annotations
						IAnnotationModel annotationModel = documentProvider.getAnnotationModel(editor.getEditorInput());

						// Lock on the annotation model
						Object lockObject = ((ISynchronizable) annotationModel).getLockObject();
						synchronized (lockObject) {
							@SuppressWarnings("unchecked")
              Iterator<Annotation> iter = annotationModel.getAnnotationIterator();
							while (iter.hasNext()) {
								Annotation anno = iter.next();
								if (anno.getType().startsWith("rascal.highlight")) {
									annotationModel.removeAnnotation(anno);
								}
							}
						}

						for (IValue v : lines) {
							IConstructor lineDecor = (IConstructor) v;
							int lineNumber = ((IInteger) lineDecor.get(0)).intValue();
							String msg = ((IString) lineDecor.get(1)).getValue();
							int severity = 0;
							boolean useMarker = true;

							String name = lineDecor.getName();
							if (name.equals("info")) {
								severity = IMarker.SEVERITY_INFO;
							}
							else if (name.equals("warning")) {
								severity = IMarker.SEVERITY_WARNING;
							}
							else if (name.equals("error")) {
								severity = IMarker.SEVERITY_ERROR;
							}
							else {
								useMarker = false;
							}

							if (useMarker) { // Add a marker
								IMarker marker = inputResource.createMarker(RASCAL_MARKER);
								marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
								marker.setAttribute(IMarker.MESSAGE, msg);
								marker.setAttribute(IMarker.LOCATION, "line " + lineNumber);
								marker.setAttribute(IMarker.SEVERITY, severity);
							} else { // Add an annotation
								int highlightKind = 0;

								if (lineDecor.arity() > 2) {
									highlightKind = ((IInteger) lineDecor.get(2)).intValue();
									if (highlightKind < 0) {
										highlightKind = 0;
									}
									if (highlightKind >= LINE_HIGHLIGHT_LENGTH) {
										highlightKind = LINE_HIGHLIGHT_LENGTH - 1;
									}
								}

								String highlightName = RASCAL_LINE_HIGHLIGHT[highlightKind];
								try {
									// line count internally starts with 0, and
									// not with 1 like in GUI
									IRegion lineInfo = document.getLineInformation(lineNumber - 1);
									synchronized (lockObject) {
										Annotation currentLine = new Annotation(highlightName, true, msg);
										Position currentPosition = new Position(lineInfo.getOffset(), lineInfo.getLength());
										annotationModel.addAnnotation(currentLine, currentPosition);
									}
								} catch (org.eclipse.jface.text.BadLocationException e) {
									// Ignore, lineNumber may just not exist in file
								}
							}
						}
						if (firstTime) {
							firstTime = false;
							if (loc.hasOffsetLength() && editorPart instanceof ITextEditor) {
								((ITextEditor)editorPart).selectAndReveal(loc.getOffset(), loc.getLength());
							}
						}
					}
				}
			} catch (PartInitException e) {
				Activator.getInstance().logException("failed to open editor for source loc:" + loc, e);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		private IEditorInput getEditorInput(URI uri) {
			String scheme = uri.getScheme();

			if (scheme.equals("project")) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.getAuthority());

				if (project != null) {
					return new FileEditorInput(project.getFile(uri.getPath()));
				}

				Activator.getInstance().logException("project " + uri.getAuthority() + " does not exist", new RuntimeException());
			} else if (scheme.equals("file")) {
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IFile[] cs = root.findFilesForLocationURI(uri);

				if (cs != null && cs.length > 0) {
					return new FileEditorInput(cs[0]);
				}

				Activator.getInstance().logException("file " + uri + " not found", new RuntimeException());
			} else if (scheme.equals("rascal-library")) {
				IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
				if (files.length > 0) {
					return new FileEditorInput(files[0]);
				}
			}

			Activator.getInstance().logException("scheme " + uri.getScheme() + " not supported", new RuntimeException());
			return null;
		}
	}

	private class OneTimeDecoratorRunner extends DecoratorRunnerBase {
		private final IList lineInfo;

		public OneTimeDecoratorRunner(ISourceLocation loc, IList lineInfo,
				IWorkbenchPage page) {
			super(loc, page);
			this.lineInfo = lineInfo;
		}
		
		protected IList getLineInfo() {
			return lineInfo;
		}
	}

	private class RepeatableDecoratorRunner extends DecoratorRunnerBase {
		private final ICallableValue fun;

		public RepeatableDecoratorRunner(ISourceLocation loc, ICallableValue fun, IWorkbenchPage page) {
			super(loc, page);
			if (!Thread.currentThread().equals(Display.getDefault().getThread())) {
				throw new RuntimeException("Initialization should be run from UI thread!");
			}
			this.fun = fun;
			IEditorPart editorPart = null;
			try {
				 editorPart = getEditorPart();
			} catch (PartInitException e) {
				throw new RuntimeException("Init failure with part?", e);
			}
			addRunnableToAnnotationListner(editorPart);
		}
		
		protected IList getLineInfo() {
			if (Thread.currentThread().equals(Display.getDefault().getThread())) {
				throw new RuntimeException("The repeatable runner should not be run from inside the UI thread");
			}
			return (IList) fun.call(new Type[0], new IValue[0], null).getValue();
		}

		
		private void addRunnableToAnnotationListner(IEditorPart editorPart) {
			annotationRunners.put(editorPart, this);
			annotationRunnerEvaluators.put(editorPart, fun.getEval());
			String fileName = editorPart.getEditorInput().getName();
			int dotPosition = fileName.lastIndexOf('.');
			if (dotPosition != -1) {
				String extension = fileName.substring(dotPosition).toLowerCase();
				Set<IWorkbenchPart> extensionList = partsProvided.get(extension);
				if (extensionList != null) {
					extensionList.add(editorPart);
				}
			}
		}
	}

	IValueFactory values;

	public Editors(IValueFactory values) {
		this.values = values;
	}

	/*
	 * Local declarations for annotations and markers
	 */

	private final int LINE_HIGHLIGHT_LENGTH = 5;

	// This following list of annotations has to be in sync with the annotations
	// declared in plugin.xml

	private final static String[] RASCAL_LINE_HIGHLIGHT = {
			"rascal.highlight0", "rascal.highlight1", "rascal.highlight2",
			"rascal.highlight3", "rascal.highlight4" };

	private final static String RASCAL_MARKER = "rascal.markerType.queryResult";

	/**
	 * Define a list of custom colors for line highlights in the editor
	 * 
	 * @param colors
	 *            The list of colors
	 */
	public void setHighlightColors(final IList colors) {
		FigureColorUtils.setHighlightColors(colors);

		IPreferenceStore prefStore = EditorsUI.getPreferenceStore();
		for (int i = 0; i < colors.length() && i < LINE_HIGHLIGHT_LENGTH; i++) {
			int color = ((IInteger) colors.get(i)).intValue();
			String prefKey = "Highlight" + i + "ColorPreferenceKey";
			PreferenceConverter.setValue(prefStore, prefKey, FigureColorUtils.toRGB(color));
		}
	}

	

	/**
	 * Define a list of custom colors for line highlights in the editor
	 * 
	 * @param colors
	 *            The list of colors
	 */
	public void setErrorColors(final IList colors) {
		FigureColorUtils.setErrorColors(colors);

		IPreferenceStore prefStore = EditorsUI.getPreferenceStore();

		for (int i = 0; i < colors.length() && i < LINE_HIGHLIGHT_LENGTH; i++) {
			int color = ((IInteger) colors.get(i)).intValue();
			// TODO: is this right?
			String prefKey = "Error" + i + "ColorPreferenceKey";
			PreferenceConverter.setValue(prefStore, prefKey, FigureColorUtils.toRGB(color));
		}
	}
	/**
	 * Start an editor for a given source location
	 * 
	 * @param loc
	 *            The source location to be edited
	 * @param lineInfo
	 *            A list of LineDecorations (see Render.rsc)
	 * 
	 *            Code is cloned from SourceLocationHyperlink (but heavily
	 *            adapted)
	 */
	public void edit( ISourceLocation loc, final IList lineInfo, IEvaluatorContext ctx) {
	  final ISourceLocation rloc = ctx != null ? ctx.getHeap().resolveSourceLocation(loc) : loc;
		IWorkbenchWindow win = getWorkbenchWindow();
		if (win != null) {
			IWorkbenchPage page = win.getActivePage();
			if (page != null) {
				Display.getDefault().asyncExec(new OneTimeDecoratorRunner(rloc, lineInfo, page));
			}
		}

	}

	/**
	 * Start an editor for a given source location
	 * 
	 * @param loc
	 *            The source location to be edited
	 * @param lineInfo
	 *            A list of LineDecorations (see Render.rsc) or a Computed list
	 *            of LineDecorations
	 * 
	 *            Code is cloned from SourceLocationHyperlink (but heavily
	 *            adapted)
	 */
	public void edit(ISourceLocation loc, final IValue lineInfo, IEvaluatorContext ctx) {
	  final ISourceLocation rloc = ctx != null ? ctx.getHeap().resolveSourceLocation(loc) : loc;
	  
		if (lineInfo instanceof ICallableValue) {
			final ICallableValue lineInfoFunc = (ICallableValue) lineInfo;

			IWorkbenchWindow win = getWorkbenchWindow();
			if (win != null) {
				final IWorkbenchPage page = win.getActivePage();
				if (page != null) {
					page.addPartListener(annotationListener);
					// The Decorator was to be constructed on the UI thread due to initialisation  
					// afterwards we can run it outside the UI thread (and we should)
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							// and only then schedule it on a non UI thread for the rascal callbacks
							RascalInvoker.invokeAsync(new RepeatableDecoratorRunner(rloc, lineInfoFunc, page), lineInfoFunc.getEval());
						}
					});
				}
			}
		}
	}

	private IWorkbenchWindow getWorkbenchWindow() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

		if (win == null && wb.getWorkbenchWindowCount() != 0) {
			win = wb.getWorkbenchWindows()[0];
		}
		return win;
	}

	public void provideDefaultLineDecorations(IString extension,
			IValue handleNewFile) {
		if (handleNewFile instanceof ICallableValue) {
			IWorkbenchWindow win = getWorkbenchWindow();

			if (win != null) {
				IWorkbenchPage page = win.getActivePage();
				page.addPartListener(annotationListener);
				if (partsProvided.containsKey(extension.getValue())) {
					// okay, we have to remove the old provided LineDecorators
					for (IWorkbenchPart part : partsProvided.get(extension.getValue())) {
						annotationRunners.remove(part);
						annotationRunnerEvaluators.remove(part);
					}
				}
				partsProvided.put(extension.getValue(), new HashSet<IWorkbenchPart>());
				defaultProviders.put(extension.getValue(), (ICallableValue) handleNewFile);
			}
		}
	}

	/*
	 * This is the storage of WorkbenchPart and their associated annotation
	 * runners.
	 */
	private final static Map<IWorkbenchPart, Runnable> annotationRunners = new ConcurrentHashMap<IWorkbenchPart, Runnable>();
	private final static Map<IWorkbenchPart, IEvaluator<Result<IValue>>> annotationRunnerEvaluators = new ConcurrentHashMap<IWorkbenchPart, IEvaluator<Result<IValue>>>();
	private final static Map<String, Set<IWorkbenchPart>> partsProvided = new ConcurrentHashMap<String, Set<IWorkbenchPart>>();
	private final static Map<String, ICallableValue> defaultProviders = new ConcurrentHashMap<String, ICallableValue>();
	private final static TypeFactory TF = TypeFactory.getInstance();
	private final static IValueFactory VF = ValueFactoryFactory
			.getValueFactory();
	private final static IPartListener annotationListener = new IPartListener() {
		
		public void partActivated(IWorkbenchPart part) {
			Runnable runAgain = annotationRunners.get(part);
			if (runAgain != null) {
				IEvaluator<Result<IValue>> eval = annotationRunnerEvaluators.get(part);
				RascalInvoker.invokeAsync(runAgain, eval);
			} else if (part instanceof ITextEditor) { 
				// only try to run the callback if there wasn't another runner associated
				IEditorInput editorInput = part.getSite().getPage().getActiveEditor().getEditorInput();
				String fileName = editorInput.getName();
				int dotPosition = fileName.lastIndexOf('.');
				if (dotPosition != -1) {
					String extension = fileName.substring(dotPosition).toLowerCase();
					final ICallableValue defaultProvider = defaultProviders.get(extension);
					if (defaultProvider != null) {
						partsProvided.get(extension).add(part);
						final ISourceLocation fileLoc = new Resources(VF).makeFile(editorInput);
						Thread callBackThread = new Thread(new Runnable() {
							public void run() {
								Result<IValue> result;
								synchronized (defaultProvider.getEval()) {
									result = defaultProvider.call(new Type[] { TF.sourceLocationType() }, new IValue[] { fileLoc }, null);
								}
								new Editors(VF).edit(fileLoc, result.getValue(), null);
							}
						});
						// execute the callback on a separate thread to avoid slowing down the page switches
						callBackThread.start(); 
					}
				}

			}
		}
		
		public void partClosed(IWorkbenchPart part) {
			annotationRunners.remove(part);
			annotationRunnerEvaluators.remove(part);
			for (String ext : partsProvided.keySet()) {
				partsProvided.get(ext).remove(part);
			}
		}
		
		public void partBroughtToTop(IWorkbenchPart part) {
		}
		
		public void partDeactivated(IWorkbenchPart part) {
		}
		
		public void partOpened(IWorkbenchPart part) {
		}
	};


}
