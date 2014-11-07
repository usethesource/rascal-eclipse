/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
*******************************************************************************/
package org.rascalmpl.eclipse.editor;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.imp.editor.IRegionSelectionService;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.hyperlink.IHyperlink;
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
import org.rascalmpl.eclipse.terms.TermLanguageRegistry;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.uri.URIUtil;

public class SourceLocationHyperlink implements IHyperlink {
	private final ISourceLocation from;
	private ISourceLocation to;
	private String text = null;
	private IEvaluatorContext eval;
	private String project;

	public SourceLocationHyperlink(ISourceLocation from, ISourceLocation to, IEvaluatorContext eval, String project) {
		this.from = from;
		this.to = to;
		this.eval = eval;
		this.project = project;
	}

	public SourceLocationHyperlink(ISourceLocation from, ISourceLocation to, String text, IEvaluatorContext eval, String project) {
		this(from, to, eval, project);
		this.text  = text;
	}

	public IRegion getHyperlinkRegion() {
		return new Region(from.getOffset(), from.getLength());
	}

	public String getHyperlinkText() {
		if (text == null) {
			return to.toString();
		}
		return text;
	}

	public String getTypeLabel() {
		return null;
	}

	public void open() {
		if (to == null) {
			return;
		}
		EditorUtil.openAndSelectURI(to, eval, project);
	}

}
