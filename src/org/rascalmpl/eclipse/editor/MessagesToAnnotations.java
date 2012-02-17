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
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.editor;

import java.util.HashMap;

import org.eclipse.imp.editor.quickfix.IAnnotation;

public class MessagesToAnnotations extends MessagesTo{

	@SuppressWarnings("serial")
	static final HashMap<String,Integer> severities = new HashMap<String,Integer>(){{
	    put("info",   IAnnotation.INFO);
	    put("warning", IAnnotation.WARNING);
	    put("error",    IAnnotation.ERROR);
	}};
	
	public MessagesToAnnotations() {
		super(IAnnotation.INFO, severities);
	}

}
