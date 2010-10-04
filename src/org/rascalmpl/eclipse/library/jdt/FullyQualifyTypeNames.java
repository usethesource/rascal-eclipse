package org.rascalmpl.eclipse.library.jdt;

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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
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
	
	public FullyQualifyTypeNames() {
		super();
	}
	
	public void fullyQualifyTypeNames(ISourceLocation loc, IFile file) {
		this.file = file;
		this.loc = loc;
		this.rewriter = null;
		
		visitCompilationUnit();
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
		
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		IPath path = file.getFullPath();
		try {
			bufferManager.connect(path, LocationKind.IFILE, new NullProgressMonitor());
			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.IFILE);
			TextEdit te = rewriter.rewriteAST();
			te.apply(textFileBuffer.getDocument());
			textFileBuffer.commit(new NullProgressMonitor(), true);
			bufferManager.disconnect(path, LocationKind.IFILE, new NullProgressMonitor());
		} catch (CoreException e) {
			throw new Throw(VF.string("Error(s) in rewrite of compilation unit: " + e.getMessage()), this.loc, null);
		} catch (MalformedTreeException e) {
			throw new Throw(VF.string("Error(s) in rewrite of compilation unit: " + e.getMessage()), this.loc, null);
		} catch (BadLocationException e) {
			throw new Throw(VF.string("Error(s) in rewrite of compilation unit: " + e.getMessage()), this.loc, null);
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
				String qualifiedTypeName = pb.getName() + "." + node.getName();
				SimpleType st = (SimpleType) rewriter.createCopyTarget(node);
				st.setName(node.getAST().newName(qualifiedTypeName));
				rewriter.replace(node, st, null);
			}
		}
		return true;
	}

}
