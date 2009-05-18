package org.meta_environment.rascal.eclipse.lib.jdt;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IMapWriter;
import org.eclipse.imp.pdb.facts.IRelationWriter;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.meta_environment.ValueFactoryFactory;
import org.meta_environment.rascal.interpreter.control_exceptions.Throw;

public class JDTImporter extends ASTVisitor {

	private static final IWorkspaceRoot ROOT = ResourcesPlugin.getWorkspace().getRoot();
    private static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	private static final TypeFactory TF = TypeFactory.getInstance();
	private static final Type locType = TF.tupleType(TF.integerType(), TF.integerType());
	private static final Type factTupleType = TF.tupleType(locType, TF.stringType());
	private static final Type factType = TF.relType(locType, TF.stringType());
	
	private IRelationWriter types;
	private IRelationWriter methods;
	private IRelationWriter constructors;
	private IRelationWriter fields;
	private IRelationWriter vars;
	
	public JDTImporter() {
		super();
	}
	
	public IMap importFacts(IFile file) {
		int i;
		
		ICompilationUnit icu = JavaCore.createCompilationUnitFrom(file);
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setSource(icu);
		
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		
		IProblem[] problems = cu.getProblems();
		for (i = 0; i < problems.length; i++) {
			if (problems[i].isError()) {
				throw new Throw(VF.string("Error(s) in compilation unit"), (ISourceLocation) null, null);
			}
			//System.out.println(problems[i].getMessage());
		}
		
		types = VF.relationWriter(factTupleType);
		methods = VF.relationWriter(factTupleType);
		constructors = VF.relationWriter(factTupleType);
		fields = VF.relationWriter(factTupleType);
		vars = VF.relationWriter(factTupleType);
		
		cu.accept(this);
		
		IMapWriter mw = VF.mapWriter(TF.stringType(), factType);
		mw.put(VF.string("types"), types.done());
		mw.put(VF.string("methods"), methods.done());
		mw.put(VF.string("constructors"), constructors.done());
		mw.put(VF.string("fields"), fields.done());
		mw.put(VF.string("vars"), vars.done());
		
		return mw.done();
	}
	
	public void preVisit(ASTNode n) {
	
		// type bindings
		ITypeBinding tb = null;
		
		if (n instanceof org.eclipse.jdt.core.dom.Type) {
			tb = ((org.eclipse.jdt.core.dom.Type)n).resolveBinding();
		} else if (n instanceof AbstractTypeDeclaration) {
			tb = ((AbstractTypeDeclaration) n).resolveBinding();
		} else if (n instanceof AnonymousClassDeclaration) {
			tb = ((AnonymousClassDeclaration) n).resolveBinding();			
		} else if (n instanceof Expression) {
			tb = ((Expression) n).resolveTypeBinding();
		} else if (n instanceof TypeDeclarationStatement) {
			tb = ((TypeDeclarationStatement) n).resolveBinding();
		} else if (n instanceof TypeParameter) {
			tb = ((TypeParameter) n).resolveBinding();
		}
		
		if (tb != null) {
			addFact(types, n, importTypeBinding(tb));
		}
		
		// method and constructor bindings
		IMethodBinding mb = null;
		IMethodBinding cb = null;
		
		if (n instanceof ClassInstanceCreation) {
			cb = ((ClassInstanceCreation) n).resolveConstructorBinding();
		} else if (n instanceof ConstructorInvocation) {
			cb = ((ConstructorInvocation) n).resolveConstructorBinding();
		} else if (n instanceof EnumConstantDeclaration) {
			cb = ((EnumConstantDeclaration) n).resolveConstructorBinding();
		} else if (n instanceof MethodDeclaration) {
			mb = ((MethodDeclaration) n).resolveBinding();
		} else if (n instanceof MethodInvocation) {
			mb = ((MethodInvocation) n).resolveMethodBinding();			
		} else if (n instanceof SuperConstructorInvocation) {
			cb = ((SuperConstructorInvocation) n).resolveConstructorBinding();
		} else if (n instanceof SuperMethodInvocation) {
			mb = ((SuperMethodInvocation) n).resolveMethodBinding();
		}
		
		if (mb != null) {
			addFact(methods, n, importMethodBinding(mb));
		}		
		if (cb != null) {
			addFact(constructors, n, importMethodBinding(cb));
		}
		
		// field and variable bindings
		IVariableBinding vb = null;
		IVariableBinding fb = null;
		
		if (n instanceof EnumConstantDeclaration) {
			fb = ((EnumConstantDeclaration) n).resolveVariable();
		} else if (n instanceof FieldAccess) {
			fb = ((FieldAccess) n).resolveFieldBinding();
		} else if (n instanceof SuperFieldAccess) {
			fb = ((SuperFieldAccess) n).resolveFieldBinding();
		} else if (n instanceof VariableDeclaration) {
			vb = ((VariableDeclaration) n).resolveBinding();
		} else if (n instanceof Name) {
			try {
				// local variable, parameter or field.
				vb = (IVariableBinding)((Name)n).resolveBinding();
				if (vb.getDeclaringMethod() == null) {
					fb = vb;
					vb = null;
				}
					
			} catch (Exception e) {}
		}
		
		if (fb != null) {
			addFact(fields, n, importVariableBinding(fb));
		}
		if (vb != null) {
			addFact(vars, n, importVariableBinding(vb));
		}
	
		
		// package bindings	
		// these only exists for package declarations, which must use the fully qualified name
		// therefore we skip these
	}
	
	
	String importTypeBinding(ITypeBinding tb) {
		return tb.getQualifiedName();
	}
	
	String importMethodBinding(IMethodBinding mb) {
		String s = mb.getDeclaringClass().getQualifiedName();
		s += "." + mb.getName() + "(";
		
		ITypeBinding[] tbs = mb.getParameterTypes();
		for (int i = 0; i < tbs.length; i++) {
			s += tbs[i].getQualifiedName();
			if (i < tbs.length-1) {
				s += ", ";
			}
		}
		
		s += ")";
		
		return s;
	}
	
	String importVariableBinding(IVariableBinding vb) {
		String s = "";
		IMethodBinding mb = vb.getDeclaringMethod();
		if (mb != null) {
			s += importMethodBinding(mb) + ".";
		} else {
			s += vb.getDeclaringClass().getQualifiedName() + ".";
		}
		s += vb.getName();

		return s;
	}

	void addFact(IRelationWriter rw, ASTNode n, String value) {				
		ITuple loc = VF.tuple(VF.integer(n.getStartPosition()), VF.integer(n.getLength()));
		ITuple fact = VF.tuple(loc, VF.string(value));
		
		rw.insert(fact);
	}
}
