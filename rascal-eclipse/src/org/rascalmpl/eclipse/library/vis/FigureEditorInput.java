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
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.library.vis;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.rascalmpl.interpreter.IEvaluatorContext;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.IValue;

public class FigureEditorInput implements IEditorInput {
	
	final private IValue fig;  
	final private IEvaluatorContext ctx;
	final private IString name;
	
	public IValue getFig() {
		return fig;
	}	

	public IEvaluatorContext getCtx() {
		return ctx;
	}

	public FigureEditorInput(IString name, IValue fig,  IEvaluatorContext ctx) {
		this.fig = fig;
		this.ctx = ctx;
		this.name = name;
	}
	
	public boolean exists() {
		return fig != null;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public IString getIString() {
		return  name;
	}
	
	public String getName() {
		return name.getValue();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return name.getValue();
	}

	@SuppressWarnings("rawtypes")
    @Override
	public Object getAdapter(Class adapter) {
		return null;
	}
	
	@Override
	public boolean equals(Object b){
		if(b instanceof FigureEditorInput){
			return ((FigureEditorInput)b).name.equals(name);
		}
		return false;
	}

}
