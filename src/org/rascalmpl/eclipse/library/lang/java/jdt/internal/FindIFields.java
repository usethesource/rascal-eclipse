package org.rascalmpl.eclipse.library.lang.java.jdt.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.values.ValueFactoryFactory;

public class FindIFields extends ASTVisitor {

	protected static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	protected static final TypeFactory TF = TypeFactory.getInstance();
	private IFile file;
	private ISourceLocation loc;
	private Set<IField> fieldSet;
	private ISet fieldOffsetsFromLoc;
	
	public FindIFields() {
		super();
	}
	
	public Set<IField> findFieldsAtLocs(ISet fieldOffsetsFromLoc, ISourceLocation loc, IFile file) {
		this.file = file;
		this.loc = loc;
		this.fieldOffsetsFromLoc = fieldOffsetsFromLoc;
		
		fieldSet = new HashSet<IField>();
		
		visitCompilationUnit();
		
		return fieldSet;
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
				ISourceLocation pos = VF.sourceLocation(loc.getURI(), offset, length, sl, sl, -1, -1);
				throw new Throw(VF.string("Error(s) in compilation unit: " + problems[i].getMessage()), pos, null);
			}
		}
		
		cu.accept(this);
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		if (fieldOffsetsFromLoc.contains(VF.integer(node.getParent().getStartPosition()))) {
			ICompilationUnit icu = JavaCore.createCompilationUnitFrom(file);			
			try {
				IJavaElement fieldElement = icu.getElementAt(node.getStartPosition());
				if (fieldElement != null && fieldElement instanceof IField) {
					fieldSet.add((IField)fieldElement);
				}
			} catch (JavaModelException e) {
				ISourceLocation pos = VF.sourceLocation(loc.getURI(), node.getStartPosition(), node.getLength(), -1, -1, -1, -1);
				throw new Throw(VF.string("Error during field find visit: " + e.getMessage()), pos, null);
			}
		}
		return true;
	}
}
