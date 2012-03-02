/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.debug.ui.views;

import java.util.Map.Entry;

import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.rascalmpl.eclipse.debug.core.model.RascalStackFrame;
import org.rascalmpl.interpreter.result.OverloadedFunction;



public class FunctionView extends AbstractDebugView implements ISelectionListener {

	private RascalStackFrame frame;
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
	class FunctionContentProvider implements IStructuredContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {}

		public void dispose() {}

		// Return the functions as an array of Objects
		public Object[] getElements(Object parent) {
			return frame.getFunctions().toArray();
		}

	}

	class FunctionLabelProvider extends LabelProvider implements ITableLabelProvider {


		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		@SuppressWarnings("unchecked")
		public String getColumnText(Object element, int columnIndex) {
			String result = "";
			Entry<String, OverloadedFunction> entry = (Entry<String, OverloadedFunction>) element;
			switch (columnIndex) {
			case 0:  // NAME
				result = entry.getKey();
				break;
			case 1 : // LAMBDA HEADERS
				result = entry.toString();
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
		viewer.setContentProvider(new FunctionContentProvider());
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
		frame = null;
		if (selection instanceof TreeSelection) {
			Object element = ((TreeSelection) selection).getFirstElement();
			if (element instanceof RascalStackFrame) {
				frame = (RascalStackFrame) element;
			}
		}
		if (viewer.getContentProvider() != null)  {
			viewer.setInput(frame);
			viewer.refresh();
		}
	}

}
