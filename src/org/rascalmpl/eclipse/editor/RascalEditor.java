/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Bert Lisser - Bert.Lisser@cwi.nl (CWI)
 *   * Emilie Balland - (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *******************************************************************************/
package org.rascalmpl.eclipse.editor;

import org.eclipse.imp.editor.IMPOutlinePage;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class RascalEditor extends UniversalEditor {

	ISelectionChangedListener listener;

	public RascalEditor() {
		super();
		// used for the expression breakpoints management
		setEditorContextMenuId("rascal.editor.contextMenu");
	}

	protected void createActions() {
		super.createActions();
		IAction action = new Action() {/* Nothing. */
		};
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SHOW_INFORMATION);

		setAction(ITextEditorActionConstants.SHOW_INFORMATION, action);
	}

	public StyledText getTextWidget() {
		return this.getSourceViewer().getTextWidget();
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		Object pgo = this.getAdapter(IContentOutlinePage.class);
		if (pgo != null && pgo instanceof IMPOutlinePage) {
			final IMPOutlinePage pg = (IMPOutlinePage) pgo;
			listener = new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					final IPageSite site = pg.getSite();
					if (site != null) {
						final IActionBars actionBars = site.getActionBars();
						final IMenuManager menuManager = actionBars
								.getMenuManager();
						final MenuManager m = new MenuManager("Export",
								menuManager.getId());
						menuManager.add(m);
						m.setRemoveAllWhenShown(true);
						site.registerContextMenu("rascal.outline", m, null);
						pg.removeSelectionChangedListener(listener);
					}
				}
			};
			pg.addSelectionChangedListener(listener);
		}
	}

}
