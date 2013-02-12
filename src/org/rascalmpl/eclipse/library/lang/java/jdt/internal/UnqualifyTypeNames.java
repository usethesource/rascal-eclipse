/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Bas Basten - Bas.Basten@cwi.nl (CWI)
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
*******************************************************************************/
package org.rascalmpl.eclipse.library.lang.java.jdt.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.values.ValueFactoryFactory;

public class UnqualifyTypeNames extends ASTVisitor {

	protected static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	protected static final TypeFactory TF = TypeFactory.getInstance();
	private IFile file;
	private ISourceLocation loc;
	private ASTRewrite rewriter;
	
	// Information about which types we have statically imported -- we don't care about non-static
	// imports, since we are just throwing those away and recalculating that information from scratch.
	private HashSet<String> importedNames;
	private HashSet<String> importedFullNames;
	
	// Information about which types we have actually declared in this file. We don't want to
	// import anything with the same name, since it will clash with what we have locally.
	private HashSet<String> declaredNames;
	private HashSet<String> declaredFullNames;
	
	// Information about all the classes we have as "candidate imports", organized by the
	// "terminal" class name (the last in the sequence of dot-separated names). This provides
	// a way of deciding, for each name, what we should import.
	private HashMap<String,HashSet<String>> importsByClassName;
	
	// Indicates which imports we plan to add. We list these by import name, since we don't
	// use the information on the binding when we create the import declarations, just
	// the name.
	private HashSet<String> importsToAdd;
	
	// Indicates which types we have "unqualified", and what we have replaced them with.
	private HashMap<String,String> unqualifiedTypes;

	/** Internal visitor used to gather which types are already explicitly
	 *  imported. We will just keep the static imports and throw away the rest.
	 */
	private class GatherUsedPackages extends ASTVisitor {
		@Override
		public boolean visit(ImportDeclaration node) {
			IBinding binding = node.resolveBinding();
			if (binding != null) {
				if (node.isStatic()) {
					if (binding instanceof ITypeBinding) {
						ITypeBinding tb = (ITypeBinding)binding;
						
						// Adding to importedNames gives us a quick check against other names --
						// the presence of this name here means we cannot create an import for any
						// other type with this name
						String className = tb.getErasure().getName();
						importedNames.add(className);
						
						// Also add the full imported name, which we will use for comparisons as
						// we look through the code to see if we can erase the qualification
						ITypeBinding declaringClass = tb.getDeclaringClass();
						while (declaringClass != null) {
							className = declaringClass.getErasure().getName() + "." + className;
							declaringClass = declaringClass.getDeclaringClass();
						}
						
						IPackageBinding pb = tb.getPackage();
						if (pb != null && !pb.isUnnamed()) {
							className = pb.getName() + "." + className;
						}
						
						importedFullNames.add(className);
						
					}
				}
			}
			return true;
		}
		
		@Override
		public boolean visit(TypeDeclaration node) {
			ITypeBinding tb = node.resolveBinding();
			if (tb.isClass() || tb.isEnum() || tb.isInterface()) {
				// Adding this to declared names gives us a way to check to see which
				// names are declared locally -- we don't want to add imports for
				// these names, since they would clash with the ones present in this file
				String typeName = null;
				if (tb.getErasure() != null)
					typeName = tb.getErasure().getName();
				else
					typeName = tb.getName();
				declaredNames.add(typeName);

				// Also add the full declared name, which we will use for comparisons as
				// we look through the code to see if we can erase the qualification
				ITypeBinding declaringClass = tb.getDeclaringClass();
				while (declaringClass != null) {
					typeName = declaringClass.getErasure().getName() + "." + typeName;
					declaringClass = declaringClass.getDeclaringClass();
				}

				IPackageBinding pb = tb.getPackage();
				if (pb != null && !pb.isUnnamed()) {
					typeName = pb.getName() + "." + typeName;
				}

				declaredFullNames.add(typeName);
			}
			return true;
		}

		@Override
		public boolean visit(SimpleType node) {
			ITypeBinding tb = node.resolveBinding();
			if (tb != null) {
				IPackageBinding pb = tb.getPackage();
				if (pb != null && !pb.isUnnamed()) {
					addBindingToImports(tb);
				}
			}
			return true;		
		}

		@Override
		public boolean visit(FieldAccess node) {
			Expression exp = node.getExpression();
			if (exp instanceof SimpleName) {
				SimpleName sn = (SimpleName)exp;
				ITypeBinding tb = sn.resolveTypeBinding();
				if (tb != null && (tb.isClass() || tb.isInterface() || tb.isEnum())) {
					IPackageBinding pb = tb.getPackage();
					if (pb != null && !pb.isUnnamed()) {
						addBindingToImports(tb);
					}
				}
			}
			return true;
		}

		@Override
		public boolean visit(MethodInvocation node) {
			Expression exp = node.getExpression();
			if (exp instanceof SimpleName) {
				SimpleName sn = (SimpleName)exp;
				IBinding b = sn.resolveBinding();
				if (b.getKind() == IBinding.TYPE) {
					ITypeBinding tb = sn.resolveTypeBinding();
					if (tb != null && (tb.isClass() || tb.isInterface())) {
						IPackageBinding pb = tb.getPackage();
						if (pb != null && !pb.isUnnamed()) {
							addBindingToImports(tb);
						}
					}
				}
			}
			return true;
		}

		private void addBindingToImports(ITypeBinding tb) {
			String typeName = null;
			String fullTypeName = null;
			
			if (tb.getErasure() != null)
				typeName = tb.getErasure().getName();
			else
				typeName = tb.getName();
			
			ITypeBinding declaringClass = tb.getDeclaringClass();
			fullTypeName = typeName;
			while (declaringClass != null) {
				fullTypeName = declaringClass.getErasure().getName() + "." + fullTypeName;
				declaringClass = declaringClass.getDeclaringClass();
			}
			
			IPackageBinding pb = tb.getPackage();
			if (pb != null && !pb.isUnnamed()) {
				fullTypeName = pb.getName() + "." + fullTypeName;
			}

			if (importsByClassName.containsKey(typeName)) {
				importsByClassName.get(typeName).add(fullTypeName);
			} else {
				HashSet<String> hs = new HashSet<String>();
				hs.add(fullTypeName);
				importsByClassName.put(typeName, hs);
			}
		}		
	}
	
	public UnqualifyTypeNames() {
		super();
	}

	public void calculateImportsToAdd() {
		this.importsToAdd = new HashSet<String>();
		this.unqualifiedTypes = new HashMap<String,String>();

		for (String n : importsByClassName.keySet()) {
			if (! (importedNames.contains(n) || declaredNames.contains(n))) {
				// This is not one of the names statically imported or locally
				// declared. Find the longest name and import that, while leaving
				// the rest fully qualified.
				HashSet<String> bindings = importsByClassName.get(n);
				String longest = "";
				for (String s : bindings) {
					if (longest.length() < s.length()) longest = s;
				}
				importsToAdd.add(longest);
				unqualifiedTypes.put(longest, n);
			}
		}
	}
	
	public void unqualifyTypeNames(ISourceLocation loc, IFile file) {
		this.file = file;
		this.loc = loc;
		this.rewriter = null;

		this.importedNames = new HashSet<String>();
		this.importedFullNames = new HashSet<String>();

		this.declaredNames = new HashSet<String>();
		this.declaredFullNames = new HashSet<String>();

		this.importsByClassName = new HashMap<String,HashSet<String>>();

		// Now visit the compilation unit and actually perform the changes
		visitCompilationUnit();
	}
	
	private void visitCompilationUnit() {
		ICompilationUnit icu = JavaCore.createCompilationUnitFrom(file);
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setSource(icu);
		
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		IProblem[] problems = cu.getProblems();
		for (int i = 0; i < problems.length; i++) {
			if (problems[i].isError()) {
				int offset = problems[i].getSourceStart();
				int length = problems[i].getSourceEnd() - offset;
				int sl = problems[i].getSourceLineNumber();
				ISourceLocation pos = VF.sourceLocation(loc.getURI(), offset, length, sl, sl, 0, 0);
				throw new Throw(VF.string("Error(s) in compilation unit: " + problems[i].getMessage()), pos, null);
			}
		}

		// Figure out which imports we need to keep and/or need to add
		cu.accept(new GatherUsedPackages());
		calculateImportsToAdd();

		// Now, start the rewrite process, first with the imports, then with the various
		// types found in the code in the file
		rewriter = ASTRewrite.create(cu.getAST());
		ListRewrite lrw = rewriter.getListRewrite(cu, CompilationUnit.IMPORTS_PROPERTY);

		// Throw away the current imports
		List<?> imps = lrw.getOriginalList();
		for (int i = 0; i < imps.size(); ++i) {
			lrw.remove((ASTNode)imps.get(i), null);
		}
		
		// Add the statically imported types back in
		for (String s : importedFullNames) {
			ImportDeclaration id = cu.getAST().newImportDeclaration();
			id.setName(cu.getAST().newName(s));
			id.setStatic(true);
			lrw.insertLast(id, null);
		}
		
		// Add the new imports back in
		String[] whatever = { "A" };
		String[] sortedImports = importsToAdd.toArray(whatever);
		Arrays.sort(sortedImports);
		
		for (String s : sortedImports) {
			ImportDeclaration id = cu.getAST().newImportDeclaration();
			id.setName(cu.getAST().newName(s));
			id.setStatic(false);
			lrw.insertLast(id, null);
		}

		cu.accept(this);

		try {
			IPath path = file.getFullPath();

			ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
			bufferManager.connect(path, LocationKind.IFILE, new NullProgressMonitor());

			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.IFILE);
			
			IDocument doc = textFileBuffer.getDocument();
			TextEdit te = rewriter.rewriteAST(doc,null);
			te.apply(doc);
			textFileBuffer.commit(new NullProgressMonitor(), true);
			
			bufferManager.disconnect(path, LocationKind.IFILE, new NullProgressMonitor());
		} catch (CoreException e) {
			throw new Throw(VF.string("Error(s) in rewrite of compilation unit: " + e.getMessage()), loc, null);
		} catch (MalformedTreeException e) {
			throw new Throw(VF.string("Error(s) in rewrite of compilation unit: " + e.getMessage()), loc, null);
		} catch (BadLocationException e) {
			throw new Throw(VF.string("Error(s) in rewrite of compilation unit: " + e.getMessage()), loc, null);
		}		
	}

	@Override
	public boolean visit(SimpleType node) {
		ITypeBinding tb = node.resolveBinding();
		if (tb != null && node.getParent().getNodeType() != ASTNode.TYPE_DECLARATION) {
			
			String typeName = "";
			if (tb.getErasure() != null)
				typeName = tb.getErasure().getName();
			else
				typeName = tb.getName();
			
			ITypeBinding declaringClass = tb.getDeclaringClass();
			while (declaringClass != null) {
				typeName = declaringClass.getErasure().getName() + "." + typeName;
				declaringClass = declaringClass.getDeclaringClass();
			}
			
			IPackageBinding pb = tb.getPackage();
			if (pb != null && !pb.isUnnamed()) {
				typeName = pb.getName() + "." + typeName;
			}
			
			if (unqualifiedTypes.containsKey(typeName)) {
				SimpleType st = node.getAST().newSimpleType(node.getAST().newName(unqualifiedTypes.get(typeName)));
				rewriter.replace(node, st, null);
			}
		}
		return true;
	}

	@Override
	public boolean visit(FieldAccess node) {
		Expression exp = node.getExpression();
		if (exp instanceof SimpleName) {
			SimpleName sn = (SimpleName)exp;
			ITypeBinding tb = sn.resolveTypeBinding();
			if (tb != null && (tb.isClass() || tb.isInterface() || tb.isEnum())) {
				String typeName = "";
				if (tb.getErasure() != null)
					typeName = tb.getErasure().getName();
				else
					typeName = tb.getName();
				
				ITypeBinding declaringClass = tb.getDeclaringClass();
				while (declaringClass != null) {
					typeName = declaringClass.getErasure().getName() + "." + typeName;
					declaringClass = declaringClass.getDeclaringClass();
				}
				
				IPackageBinding pb = tb.getPackage();
				if (pb != null && !pb.isUnnamed()) {
					typeName = pb.getName() + "." + typeName;
				}
				
				if (unqualifiedTypes.containsKey(typeName)) {
					Name n = node.getAST().newName(unqualifiedTypes.get(typeName));
					rewriter.replace(exp, n, null);
				}
			}
		}
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		Expression exp = node.getExpression();
		if (exp instanceof SimpleName) {
			SimpleName sn = (SimpleName)exp;
			IBinding b = sn.resolveBinding();
			if (b.getKind() == IBinding.TYPE) {
				ITypeBinding tb = sn.resolveTypeBinding();
				if (tb != null && (tb.isClass() || tb.isInterface())) {
					String typeName = "";
					if (tb.getErasure() != null)
						typeName = tb.getErasure().getName();
					else
						typeName = tb.getName();
					
					ITypeBinding declaringClass = tb.getDeclaringClass();
					while (declaringClass != null) {
						typeName = declaringClass.getErasure().getName() + "." + typeName;
						declaringClass = declaringClass.getDeclaringClass();
					}

					IPackageBinding pb = tb.getPackage();
					if (pb != null && !pb.isUnnamed()) {
						typeName = pb.getName() + "." + typeName;
					}
					
					if (unqualifiedTypes.containsKey(typeName)) {
						Name n = node.getAST().newName(unqualifiedTypes.get(typeName));
						rewriter.replace(exp, n, null);
					}
				}
			}
		}
		return true;
	}

}
