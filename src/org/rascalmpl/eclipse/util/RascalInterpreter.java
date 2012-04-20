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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.ui.IEditorPart;
import org.rascalmpl.ast.AbstractAST;
import org.rascalmpl.ast.Expression;
import org.rascalmpl.ast.ImportedModule;
import org.rascalmpl.ast.Name;
import org.rascalmpl.ast.QualifiedName;
import org.rascalmpl.ast.Variable;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.outline.TreeModelBuilder;
import org.rascalmpl.interpreter.Configuration;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.JavaToRascal;

public class RascalInterpreter extends JavaToRascal {

	/**
	 * Inspects if abstractAST is an initialized variable.
	 * 
	 * @param abstractAST
	 * @return the name of that variable (null otherwise)
	 */
	static public String getEvaluableVariableName(Object abstractAST) {
		return getEvaluableVariableName(abstractAST, null);
	}

	/**
	 * Inspects if abstractAST is an initialized variable to which assigned is a
	 * term whose header is equal to headerSymbol.
	 * 
	 * @param abstractAST
	 * @param headerSymbol
	 * @return the name of that variable (null otherwise)
	 */
	static public String getEvaluableVariableName(Object abstractAST,
			String headerSymbol) {
		if (abstractAST == null || !(abstractAST instanceof AbstractAST))
			return null;
		if (abstractAST instanceof Variable
				&& ((Variable) abstractAST).isInitialized()
				&& ((Variable) abstractAST).hasName()
				&& ((Variable) abstractAST).getName() instanceof Name.Lexical) {
			if (headerSymbol != null) {
				Expression initial = (((Variable.Initialized) abstractAST))
						.getInitial();
				if (!(initial instanceof Expression.CallOrTree))
					return null;
				Expression.CallOrTree e = (Expression.CallOrTree) initial;
				Expression expression = e.getExpression();
				if (!(expression instanceof Expression.QualifiedName))
					return null;
				Expression.QualifiedName qn = (Expression.QualifiedName) expression;
				QualifiedName qualifiedName = qn.getQualifiedName();
				if (qualifiedName.getNames().size() != 1)
					return null;
				Name name = qualifiedName.getNames().get(0);
				if (!(name instanceof Name.Lexical))
					return null;
				if (((Name.Lexical) name).getString().equals(headerSymbol))
					return ((Name.Lexical) (((Variable) abstractAST).getName()))
							.getString();
				return null;
			}
			return ((Name.Lexical) (((Variable) abstractAST).getName()))
					.getString();
		}
		return null;
	}
	
	

	/**
	 * Inspects if abstractAST is an imported module.
	 * 
	 * @param abstractAST
	 * @return the name of that module (null otherwise)
	 */
	static public String getImportName(Object abstractAST) {
		if (abstractAST == null || !(abstractAST instanceof AbstractAST))
			return null;
		if (abstractAST instanceof ImportedModule
				&& ((ImportedModule) abstractAST).hasName()) {
			QualifiedName name = ((ImportedModule) abstractAST).getName();
			StringBuffer b = new StringBuffer();
			for (Name h : name.getNames()) {
				if (h instanceof Name.Lexical) {
					b.append("::");
					b.append(((Name.Lexical) h).getString());
				}
			}
			return b.substring(2);
		}
		return null;
	}

	/**
	 * Inspects if abstractAST is an imported (outline) group.
	 * 
	 * @param abstractAST
	 * @return the name of that group (null otherwise)
	 */
	@SuppressWarnings("rawtypes")
	static public String getGroupName(Object abstractAST) {
		if (abstractAST == null)
			return null;
		if (abstractAST instanceof TreeModelBuilder.Group)
			return ((TreeModelBuilder.Group) abstractAST).getName();
		return null;
	}
	
	/**
	 * Inspects if activeEditor belongs to a language.
	 * 
	 * @param activeEditor
	 * @return the name of the language belonging to activeEditor 
	 * (null otherwise)
	 */
	static public String getLanguage(IEditorPart activeEditor) {
		if (activeEditor instanceof UniversalEditor) {
			final UniversalEditor e = (UniversalEditor) activeEditor;
			return e.getLanguage().getName();
		}
		return null;
	}

	@Override
	public Evaluator getEvaluator() {
		return super.getEvaluator();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rascalmpl.interpreter.JavaToRascal#eval(java.lang.String)
	 */
	@Override
	public Object eval(String command) {
		return super.eval(command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rascalmpl.interpreter.JavaToRascal#intValue(java.lang.String)
	 */
	@Override
	public int intValue(String command) {
		return super.intValue(command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rascalmpl.interpreter.JavaToRascal#boolValue(java.lang.String)
	 */
	@Override
	public boolean boolValue(String command) {
		return super.boolValue(command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rascalmpl.interpreter.JavaToRascal#stringValue(java.lang.String)
	 */
	@Override
	public String stringValue(String command) {
		return super.stringValue(command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rascalmpl.interpreter.JavaToRascal#listValue(java.lang.String)
	 */
	@Override
	public Object[] listValue(String command) {
		return super.listValue(command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rascalmpl.interpreter.JavaToRascal#voidValue(java.lang.String)
	 */
	@Override
	public void voidValue(String command) {
		super.voidValue(command);
	}

	@Override
	public boolean isProcedureInModule(String moduleName, String procedureName, String procedureResultType, int arity) {
		return super.isProcedureInModule(moduleName, procedureName, procedureResultType, arity);
	}

	@Override
	public boolean isVariableInModule(String moduleName, String variableName, String variableType) {
		return super.isVariableInModule(moduleName, variableName, variableType);
	}

	static public String rascalBasename(IFile file) {
		return file.getName().substring(
				0,
				file.getName().length()
						- Configuration.RASCAL_FILE_EXT.length());
	}

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

	

	/* An example of use: */

	public static void test(IProject project) {
		// IProject project =
		// ResourcesPlugin.getWorkspace().getRoot().getProject("aap");
		final RascalInterpreter jr = new RascalInterpreter(project);
		System.out.println(jr.stringValue("import List;"));
		System.out.println(jr.stringValue("\"<2+3>\";"));
		System.out.println(jr.stringValue("\"aap:<size([2,3])>\";"));
		final IInteger d1 = vf.integer(1), d2 = vf.integer(2);
		final IList l = vf.list(d1, d2);
		System.out.println(jr.call("size", l));
	}
}
