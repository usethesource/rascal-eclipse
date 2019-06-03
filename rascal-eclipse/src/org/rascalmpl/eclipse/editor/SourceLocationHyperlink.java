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

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import io.usethesource.vallang.ISourceLocation;

public class SourceLocationHyperlink implements IHyperlink {
	private final ISourceLocation from;
	private final ISourceLocation to;
	private final String text;

	public SourceLocationHyperlink(ISourceLocation from, ISourceLocation to) {
		this.from = from;
		this.to = to;
		this.text = null;
	}

	public SourceLocationHyperlink(ISourceLocation from, ISourceLocation to, String text) {
		this.from = from;
		this.to = to;
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
		EditorUtil.openAndSelectURI(to);
	}

}
