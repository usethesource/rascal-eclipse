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
package org.rascalmpl.eclipse.library.lang.java.jdt.internal;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.values.ValueFactoryFactory;

public class RemoveMethods extends ASTVisitor {

	protected static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	protected static final TypeFactory TF = TypeFactory.getInstance();
	private IFile file;
	private ISourceLocation loc;
	private ISet methodOffsetsFromLoc;
	private ASTRewrite rewriter;

	public RemoveMethods() {
		super();
	}
	
	public void removeMethodsAtLocs(ISet methodOffsetsFromLoc, ISourceLocation loc, IFile file) {
		this.file = file;
		this.loc = loc;
		this.rewriter = null;
		this.methodOffsetsFromLoc = methodOffsetsFromLoc;
		
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
	public boolean visit(MethodDeclaration node) {
		if (methodOffsetsFromLoc.contains(VF.integer(node.getStartPosition()))) {
			ICompilationUnit icu = JavaCore.createCompilationUnitFrom(file);			
			try {
				IJavaElement methodDeclElement = icu.getElementAt(node.getStartPosition());
				if (methodDeclElement != null && methodDeclElement instanceof IMethod) {
					rewriter.remove(node, null);
				}
			} catch (JavaModelException e) {
				ISourceLocation pos = VF.sourceLocation(loc.getURI(), node.getStartPosition(), node.getLength());
				throw new Throw(VF.string("Error during method find visit: " + e.getMessage()), pos, null);
			}
		}
		return true;
	}
	
}
