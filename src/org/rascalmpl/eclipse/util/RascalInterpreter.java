/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Bert  B. Lisser - Bert.Lisser@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.util;

import java.io.PrintWriter;

import org.eclipse.core.resources.IProject;
import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IList;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.interpreter.JavaToRascal;

public class RascalInterpreter extends JavaToRascal {
	
	public RascalInterpreter(IProject project, PrintWriter stdout,
			PrintWriter stderr) {
		super(stdout, stderr);
		ProjectEvaluatorFactory.getInstance().initializeProjectEvaluator(
				project, getEvaluator());
	}

	public RascalInterpreter(IProject project) {
		super(ProjectEvaluatorFactory.getInstance().createProjectEvaluator(
				project));
	}
		

/* Example of use: */
	
public static void test(IProject project) {
    	// IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("aap");
    	final RascalInterpreter jr = new RascalInterpreter(project);
    	System.out.println(jr.eval("import List;"));
    	System.out.println(jr.eval("\"<2+3>\";"));
    	System.out.println(jr.eval("\"aap:<size([2,3])>\";"));
    	final IInteger d1 = vf.integer(1), d2 = vf.integer(2);
    	final IList l = vf.list(d1, d2);
    	System.out.println(jr.call("size", l));
    }
}

