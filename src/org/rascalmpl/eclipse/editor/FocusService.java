package org.rascalmpl.eclipse.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.editor.EditorUtility;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationTypeLookup;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.rascalmpl.values.uptr.TreeAdapter;


public class FocusService implements IModelListener {
	private final static String FOCUS_ANNOTATION = "rascal.focus";
	
	private final SelectionChangeListener selectionChangeListener;
	private Annotation currentFocus;
	
	private volatile String focussedSort;
	private volatile Position focussedPosition;

	private IWindowListener windowListener;
	
	public FocusService() {
		selectionChangeListener = new SelectionChangeListener(this);
		currentFocus = null;
		init();
	}

	public AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.SYNTACTIC_ANALYSIS;
	}

	public void update(IParseController parseController,
			IProgressMonitor monitor) {
	}

	private void init(){
		IWorkbench workbench = PlatformUI.getWorkbench();
		windowListener = new IWindowListener(){
			public void windowActivated(IWorkbenchWindow window){
				ISelectionService selectionService = window.getSelectionService();
				selectionService.addPostSelectionListener(selectionChangeListener);
			}

			public void windowClosed(IWorkbenchWindow window){
				ISelectionService selectionService = window.getSelectionService();
				selectionService.removePostSelectionListener(selectionChangeListener);
			}

			public void windowDeactivated(IWorkbenchWindow window){
				ISelectionService selectionService = window.getSelectionService();
				selectionService.removePostSelectionListener(selectionChangeListener);
			}

			public void windowOpened(IWorkbenchWindow window){
				ISelectionService selectionService = window.getSelectionService();
				selectionService.addPostSelectionListener(selectionChangeListener);
			}
		};
		workbench.addWindowListener(windowListener);
	}
	
	public String getFocussedSort(){
		return focussedSort;
	}
	
	public Position getFocussedPosition(){
		return focussedPosition;
	}
	
	public void updateFocus(String sort, IConstructor focus, UniversalEditor editor, boolean scrollToFocus){
		clearFocusAnnotation(editor);
		updateSort(sort, editor);
		updateAnnotation(focus, sort, editor, scrollToFocus);
	}
	
	private void updateSort(String sort, UniversalEditor editor){
		IStatusLineManager statusLine = editor.getEditorSite().getActionBars().getStatusLineManager();
		statusLine.setMessage("Sort: "+sort);
	}
	
	private void updateAnnotation(IConstructor focus, String sort, UniversalEditor editor, boolean scrollToFocus){
		int focusOffset = TreeAdapter.getLocation(focus).getOffset();
		int focusLength = TreeAdapter.getLocation(focus).getLength();
		
		setFocusAnnotation(focusOffset, focusLength, sort, editor);
		if (scrollToFocus){
			editor.selectAndReveal(focusOffset, 0);
		}
	}
	
	private void clearFocusAnnotation(UniversalEditor editor){
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		IAnnotationModel annotationModel = documentProvider.getAnnotationModel(editor.getEditorInput());
		
		// Lock on the annotation model
		Object lockObject = ((ISynchronizable) annotationModel).getLockObject();
		synchronized(lockObject){
			if(currentFocus != null) annotationModel.removeAnnotation(currentFocus);
		}
	}
	
	private void setFocusAnnotation(int focusOffset, int focusLength, String sort, UniversalEditor editor){
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		IAnnotationModel annotationModel = documentProvider.getAnnotationModel(editor.getEditorInput());
		
		// Lock on the annotation model
		Object lockObject = ((ISynchronizable) annotationModel).getLockObject();
		synchronized(lockObject){
			if(currentFocus != null) annotationModel.removeAnnotation(currentFocus);
			
			currentFocus = new Annotation(FOCUS_ANNOTATION, false, sort);
			focussedPosition = new Position(focusOffset, focusLength);
			annotationModel.addAnnotation(currentFocus, focussedPosition);
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.removeWindowListener(windowListener);
	}
	
	private static class SelectionChangeListener implements ISelectionListener{
		private final FocusService selectionTrackerTool;
		
		public SelectionChangeListener(FocusService selectionTrackerTool){
			super();
			this.selectionTrackerTool = selectionTrackerTool;
		}

		public void selectionChanged(final IWorkbenchPart part, ISelection selection){
			if (selection instanceof ITextSelection && part instanceof UniversalEditor){
				UniversalEditor editor = (UniversalEditor) part;
				
				selectionTrackerTool.clearFocusAnnotation(editor);
				
				ITextSelection textSelection = (ITextSelection) selection;
				if(textSelection.getLength() != 0) return;
				
				IParseController parseController = editor.getParseController();
				IConstructor tree = (IConstructor) parseController.getCurrentAst();

				if (tree != null) {
					IConstructor focussed = TreeAdapter.locateDeepestContextFreeNode(tree, textSelection.getOffset());
					if (focussed != null) {
						selectionTrackerTool.updateFocus(TreeAdapter.getSortName(focussed), focussed, editor, false);
					}
				}
			}
		}
	}
}
