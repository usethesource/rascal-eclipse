package org.meta_environment.rascal.eclipse.debug.ui.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.draw2d.GridData;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.meta_environment.rascal.eclipse.IRascalResources;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalDebugTarget;
import org.meta_environment.rascal.interpreter.env.Lambda;



public class FunctionView extends AbstractDebugView implements ISelectionListener {

	private RascalDebugTarget fTarget;
	private TableViewer viewer;

	// Set column names
	private String[] columnNames = new String[] { 
			"Name",
			"Headers"
	};


	/**
	 * InnerClass that acts as a proxy for the FunctionList
	 * providing content for the Table. It implements the FunctionListViewer 
	 * interface since it must register changeListeners with the 
	 * FunctionList 
	 */
	class ContentProvider implements IStructuredContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {}

		public void dispose() {}

		// Return the functions as an array of Objects
		public Object[] getElements(Object parent) {
			return fTarget.getFunctions().toArray();
		}

	}

	class FunctionLabelProvider extends LabelProvider implements ITableLabelProvider {


		private Image getImage(boolean isSelected) {
			return null;
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			String result = "";
			Entry<String, List<Lambda>> entry = (Entry<String, List<Lambda>>) element;
			switch (columnIndex) {
			case 0:  // NAME
				result = entry.getKey();
				break;
			case 1 : // LAMBDA HEADERS
				List l = new ArrayList();
				for (Lambda lambda: entry.getValue()) {
					l.add(lambda.getHeader());
				}
				result = l.toString();
				break;
			default :
				break; 	
			}
			return result;
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

	}

	@Override
	protected void configureToolBar(IToolBarManager tbm) {
		// no action associated to this view		
	}

	@Override
	protected void createActions() {
		// no action associated to this view
	}

	@Override
	protected Viewer createViewer(Composite parent) {
		Table table = new Table(parent,0);
		TableColumn nameColumn = new TableColumn(table, SWT.LEFT, 0);
		nameColumn.setText("Name");
		nameColumn.setWidth(100);
		TableColumn lambdaColumn = new TableColumn(table, SWT.LEFT, 1);
		lambdaColumn.setText("Headers");
		lambdaColumn.setWidth(400);
		viewer = new TableViewer(table);
		viewer.setUseHashlookup(true);
		viewer.setColumnProperties(columnNames);
		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new FunctionLabelProvider());
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		getSite().setSelectionProvider(viewer);
		return viewer;
	}

	@Override
	protected void fillContextMenu(IMenuManager menu) {
		// no action associated to this view
	}

	@Override
	protected String getHelpContextId() {
		return null;
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IAdaptable adaptable = DebugUITools.getDebugContext();
		fTarget = null;
		if (adaptable != null) {
			IDebugElement element = (IDebugElement) adaptable.getAdapter(IDebugElement.class);
			if (element != null) {
				if (element.getModelIdentifier().equals(IRascalResources.ID_RASCAL_DEBUG_MODEL)) {
					fTarget = (RascalDebugTarget) element.getDebugTarget();
				}
			}
		}        
		Object input = null;
		if (fTarget != null && fTarget.isSuspended()) {
			input = fTarget;
		}
		viewer.setInput(input);
		viewer.refresh();
	}

}
