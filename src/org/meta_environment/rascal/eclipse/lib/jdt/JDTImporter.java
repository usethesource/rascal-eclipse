package org.meta_environment.rascal.eclipse.lib.jdt;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IMapWriter;
import org.eclipse.imp.pdb.facts.IRelationWriter;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.meta_environment.ValueFactoryFactory;

public class JDTImporter extends ASTVisitor {

	private static final IWorkspaceRoot ROOT = ResourcesPlugin.getWorkspace().getRoot();
    private static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	private static final TypeFactory TF = TypeFactory.getInstance();
	private static final Type locType = TF.tupleType(TF.integerType(), TF.integerType());
	private static final Type factType = TF.tupleType(locType, TF.stringType());
	
	private IRelationWriter types;
	private IRelationWriter methods;
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
		AST ast = cu.getAST();
		
		/*IProblem[] problems = cu.getProblems();
		for (i = 0; i < problems.length; i++) {
			System.out.println(problems[i].getMessage());
		}*/
		
		/*
		Message[] messages = cu.getMessages();
		for (i = 0; i < messages.length; i++) {
			System.out.println(messages[i].getMessage());
		}*/
		
		types = VF.relationWriter(factType);
		methods = VF.relationWriter(factType);
		fields = VF.relationWriter(factType);
		vars = VF.relationWriter(factType);
		
		cu.accept(this);
		
		IMapWriter mw = VF.mapWriter(TF.stringType(), factType);
		mw.put(VF.string("types"), types.done());
		mw.put(VF.string("methods"), methods.done());
		mw.put(VF.string("fields"), fields.done());
		mw.put(VF.string("vars"), vars.done());
		
		return mw.done();
	}
	
	public void preVisit(ASTNode n) {
		String s = "";
		int i;
		
		// type bindings
		try { s = ((org.eclipse.jdt.core.dom.Type)n).resolveBinding().getQualifiedName(); } catch (Exception e) {}
		try { s = ((Expression)n).resolveTypeBinding().getQualifiedName(); } catch (Exception e) {}
		//try { name = ((Name)n).resolveBinding().getName(); } catch (Exception e) {}
		//try { name = ((Name)n).toString(); } catch (Exception e) {}
		
		if (s != "") {
			addFact(types, n, s);
			//System.out.println("type " + importLocation(n) + " " + s);
			s = "";
		}
		
		// method bindings
		try {
			IMethodBinding mb = ((MethodInvocation)n).resolveMethodBinding();
			s = importMethodBinding(mb);			
		} catch (Exception e) {}
		
		if (s != "") {
			addFact(methods, n, s);
			//System.out.println("method " + importLocation(n) + " " + s);
			s = "";
		}
		
		// variable bindings
		try {
			IVariableBinding vb = ((FieldAccess)n).resolveFieldBinding();
			s = importVariableBinding(vb);
		} catch (Exception e) {}

		if (s != "") {
			addFact(fields, n, s);
			//System.out.println("field " + importLocation(n) + " " + s);
			s = "";
		}
		
		try {
			IVariableBinding vb = (IVariableBinding)((Name)n).resolveBinding();
			s = importVariableBinding(vb);
		} catch (Exception e) {}
		if (s != "") {
			addFact(vars, n, s);
			//System.out.println("var " + importLocation(n) + " " + s);
			s = "";
		}
		
		
		// package bindings
		
		
		
		
		
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
	
	String importLocation(ASTNode n) {
		return "<" + n.getStartPosition() + "," + n.getLength() + ">";
	}
	
	void addFact(IRelationWriter rw, ASTNode n, String value) {				
		ITuple loc = VF.tuple(VF.integer(n.getStartPosition()), VF.integer(n.getLength()));
		ITuple fact = VF.tuple(loc, VF.string(value));
		
		rw.insert(fact);
	}
	
}
