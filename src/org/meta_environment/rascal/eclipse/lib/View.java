package org.meta_environment.rascal.eclipse.lib;

import org.dancingbear.graphbrowser.editor.ui.input.GraphEditorInput;
import org.dancingbear.graphbrowser.model.IModelGraph;
import org.dancingbear.graphbrowser.model.ModelGraphRegister;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.ui.graph.Editor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.meta_environment.rascal.eclipse.lib.graph.GraphBuilder;

public class View {
	public static void show(IValue v) {
		Editor.open(v);
	}
	
	public static void browse(IValue v) {
		Editor.open(v);
	}
	
	public static void dot(IValue v) {
		IModelGraph graph = ModelGraphRegister.getInstance().getModelGraph(v.getType().toString());
		new GraphBuilder(graph).computeGraph(v);
		final GraphEditorInput input = new GraphEditorInput(graph);
		
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

		if (win != null) {
			final IWorkbenchPage page = win.getActivePage();

			if (page != null) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						try {
							page.openEditor(input,"GraphBrowser.VisualEditor");
						} catch (PartInitException e) {
							// TODO Auto-generated catch block
						}
					}
				});
			}
		}
	}
}
 