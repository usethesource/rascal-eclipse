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

import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.rascalmpl.interpreter.IEvaluatorContext;

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
		EditorUtil.openAndSelectURI(to, eval.getResolverRegistry(), project);
	}

}
