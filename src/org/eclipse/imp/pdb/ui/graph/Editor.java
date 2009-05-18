package org.eclipse.imp.pdb.ui.graph;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

public class Editor extends EditorPart {
	public static final String EditorId = "org.eclipse.imp.pdb.ui.graph.editor";
	private Canvas canvas;
	private Graph graph;
	
	public Editor() {
	}

	public static void open(final IValue value) {
		if (value == null) {
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
							page.openEditor(new ValueEditorInput(value), Editor.EditorId);
						} catch (PartInitException e) {
							// TODO Auto-generated catch block
						}
					}
				});
			}
		}
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
	}

	private void initGraph(ValueEditorInput input) {
		GraphBuilder builder = new GraphBuilder(graph);
		builder.computeGraph(input.getValue());
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		canvas = new Canvas(parent, SWT.NONE);
		canvas.setLayout(new FillLayout());
		graph = new Graph(canvas, SWT.NONE);
		graph.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), false);
		graph.applyLayout();
		canvas.setVisible(true);
		canvas.pack();
		
		IEditorInput input = getEditorInput();
		if (input instanceof ValueEditorInput) {
			initGraph((ValueEditorInput) input);
		}
	}

	@Override
	public void setFocus() {
		canvas.setFocus();
	}
}
