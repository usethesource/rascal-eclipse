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
package org.rascalmpl.eclipse.library.lang.java.jdt.internal;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.values.ValueFactoryFactory;

public class FullyQualifyTypeNames extends ASTVisitor {

	protected static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	protected static final TypeFactory TF = TypeFactory.getInstance();
	private IFile file;
	private ISourceLocation loc;
	private ASTRewrite rewriter;
	private String qualifiedSourceText;
	
	public FullyQualifyTypeNames() {
		super();
	}
	
	public IValue fullyQualifyTypeNames(ISourceLocation loc, IFile file) {
		this.file = file;
		this.loc = loc;
		this.rewriter = null;
		this.qualifiedSourceText = "";
		
		visitCompilationUnit();
		
		return VF.string(this.qualifiedSourceText);
	}
	
	private void visitCompilationUnit() {
		ICompilationUnit icu = JavaCore.createCompilationUnitFrom(file);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(icu);
		parser.setResolveBindings(true);
		
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		IProblem[] problems = cu.getProblems();
		for (int i = 0; i < problems.length; i++) {
			if (problems[i].isError()) {
				int offset = problems[i].getSourceStart();
				int length = problems[i].getSourceEnd() - offset;
				int sl = problems[i].getSourceLineNumber();
				ISourceLocation pos = VF.sourceLocation(loc.getURI(), offset, length, sl, sl, -1, -1);
				throw new Throw(VF.string("Error(s) in compilation unit: " + problems[i].getMessage()), pos, null);
			}
		}
		
		rewriter = ASTRewrite.create(cu.getAST());
		
		cu.accept(this);

		try {
			IPath path = file.getFullPath();

			ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
			bufferManager.connect(path, LocationKind.IFILE, new NullProgressMonitor());

			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.IFILE);
			
			IDocument doc = textFileBuffer.getDocument();
			TextEdit te = rewriter.rewriteAST(doc,null);
			te.apply(doc);
			this.qualifiedSourceText = doc.get();
			
			textFileBuffer.revert(new NullProgressMonitor());
			
			bufferManager.disconnect(path, LocationKind.IFILE, new NullProgressMonitor());
		} catch (CoreException e) {
			throw new Throw(VF.string("Error(s) in rewrite of compilation unit: " + e.getMessage()), loc, null);
		} catch (MalformedTreeException e) {
			throw new Throw(VF.string("Error(s) in rewrite of compilation unit: " + e.getMessage()), loc, null);
		} catch (BadLocationException e) {
			throw new Throw(VF.string("Error(s) in rewrite of compilation unit: " + e.getMessage()), loc, null);
		}
		
//		try {
//			icu.becomeWorkingCopy(new NullProgressMonitor());
//			TextEdit te = rewriter.rewriteAST();
//			icu.applyTextEdit(te, new NullProgressMonitor());
////			icu.save(new NullProgressMonitor(), true);
//			icu.commitWorkingCopy(true, new NullProgressMonitor());
//		} catch (JavaModelException e) {
//			throw new Throw(VF.string("Error(s) in rewrite of compilation unit: " + e.getMessage()), this.loc, null);
//		} catch (IllegalArgumentException e) {
//			throw new Throw(VF.string("Error(s) in rewrite of compilation unit: " + e.getMessage()), this.loc, null);
//		}
		
	}

	@Override
	public boolean visit(SimpleType node) {
		ITypeBinding tb = node.resolveBinding();
		if (tb != null) {
			IPackageBinding pb = tb.getPackage();
			if (pb != null && !pb.isUnnamed()) {
				String qualifiedTypeName = "";
				if (tb.isNested()) {
					ITypeBinding parent = tb.getDeclaringClass();
					String parentString = parent.getName();
					while (parent != null && parent.isNested()) {
						parent = parent.getDeclaringClass();
						parentString = parent.getName() + "." + parentString;
					}
					qualifiedTypeName = pb.getName() + "." + parentString + ".";
				} else {
					qualifiedTypeName = pb.getName() + ".";					
				}
				Name stName = node.getName();
				if (stName.isQualifiedName()) {
					QualifiedName qn = (QualifiedName)stName;
					qualifiedTypeName = qualifiedTypeName + qn.getName().toString();
				} else {
					SimpleName sn = (SimpleName) stName;
					qualifiedTypeName = qualifiedTypeName + sn.toString();
				}
				SimpleType st = node.getAST().newSimpleType(node.getAST().newName(qualifiedTypeName));
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
			if (tb != null && (tb.isClass() || tb.isInterface())) {
				IPackageBinding pb = tb.getPackage();
				if (pb != null && !pb.isUnnamed()) {
					String qualifiedTypeName = "";
					if (tb.isNested()) {
						ITypeBinding parent = tb.getDeclaringClass();
						String parentString = parent.getName();
						while (parent != null && parent.isNested()) {
							parent = parent.getDeclaringClass();
							parentString = parent.getName() + "." + parentString;
						}
						qualifiedTypeName = pb.getName() + "." + parentString + ".";
					} else {
						qualifiedTypeName = pb.getName() + ".";					
					}
					qualifiedTypeName = qualifiedTypeName + sn.toString();
					Name n = node.getAST().newName(qualifiedTypeName);
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
					IPackageBinding pb = tb.getPackage();
					if (pb != null && !pb.isUnnamed()) {
						String qualifiedTypeName = "";
						if (tb.isNested()) {
							ITypeBinding parent = tb.getDeclaringClass();
							String parentString = parent.getName();
							while (parent != null && parent.isNested()) {
								parent = parent.getDeclaringClass();
								parentString = parent.getName() + "." + parentString;
							}
							qualifiedTypeName = pb.getName() + "." + parentString + ".";
						} else {
							qualifiedTypeName = pb.getName() + ".";					
						}
						qualifiedTypeName = qualifiedTypeName + sn.toString();
						Name n = node.getAST().newName(qualifiedTypeName);
						rewriter.replace(exp, n, null);
					}
				}
			}
		}
		return true;
	}

//	@Override
//	public boolean visit(QualifiedName node) {
//		Name qualifierHead = node.getQualifier();
//		return true;
//	}
//
//	@Override
//	public boolean visit(SimpleName node) {
//		if (node.getParent() instanceof QualifiedName && ( (QualifiedName) node.getParent()).getQualifier().equals(node)) {
//			ITypeBinding tb = node.resolveTypeBinding();
//			if (tb != null && (tb.isClass() || tb.isInterface())) {
//				IPackageBinding pb = tb.getPackage();
//				if (pb != null && !pb.isUnnamed()) {
//					String qualifiedTypeName = "";
//					if (tb.isNested()) {
//						ITypeBinding parent = tb.getDeclaringClass();
//						String parentString = parent.getName();
//						while (parent != null && parent.isNested()) {
//							parent = parent.getDeclaringClass();
//							parentString = parent.getName() + "." + parentString;
//						}
//						qualifiedTypeName = pb.getName() + "." + parentString + ".";
//					} else {
//						qualifiedTypeName = pb.getName() + ".";					
//					}
//					qualifiedTypeName = qualifiedTypeName + node.toString();
//					Name n = node.getAST().newName(qualifiedTypeName);
//					rewriter.replace(node, n, null);
//				}
//			}
//		}
//		return true;
//	}

}
