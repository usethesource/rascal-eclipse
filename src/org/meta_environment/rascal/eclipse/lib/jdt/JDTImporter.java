package org.meta_environment.rascal.eclipse.lib.jdt;

import static org.meta_environment.rascal.eclipse.lib.Java.ADT_ENTITY;

import java.util.EmptyStackException;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IMapWriter;
import org.eclipse.imp.pdb.facts.IRelationWriter;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
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
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.meta_environment.ValueFactoryFactory;
import org.meta_environment.rascal.interpreter.control_exceptions.Throw;

// TBD: why the difference?:
// entity([package("jdtimporter"),class("Activator   ",[entity([typeParameter("A")])]),anonymousClass(0),    method("set",[entity([package("java"),package("lang"),class("Integer")]),entity([primitive(int())])],entity([package("java"),package("lang"),class("Integer")])),parameter("element")])
// entity([package("jdtimporter"),class("Activator<A>",[entity([typeParameter("A")])]),class("new List(){}"),method("get",[                                                           entity([primitive(int())])],entity([package("java"),package("lang"),class("Integer")])),parameter("index")])



public class JDTImporter extends ASTVisitor {

	protected static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	protected static final TypeFactory TF = TypeFactory.getInstance();
	private BindingConverter bindingCache = new BindingConverter();
	private Stack<ITypeBinding> classStack = new Stack<ITypeBinding>();
	private Stack<Initializer> initializerStack = new Stack<Initializer>();
	private IFile file;
	
	// bindings
	private static final Type locType = TF.tupleType(TF.stringType(), TF.integerType(), TF.integerType());
	private static final Type bindingTupleType = TF.tupleType(locType, ADT_ENTITY);

	private IRelationWriter typeBindings;
	private IRelationWriter methodBindings;
	private IRelationWriter constructorBindings;
	private IRelationWriter fieldBindings;
	private IRelationWriter variableBindings;

	// type facts
	private static final Type typeFactTupleType = TF.tupleType(ADT_ENTITY, ADT_ENTITY);

	private IRelationWriter extnds;
	private IRelationWriter implmnts;
	private IRelationWriter declaredMethods;
	private IRelationWriter declaredFields;
	private IRelationWriter declaredTypes;
	
	public JDTImporter() {
		super();
	}
	
	public IMap importFacts(IFile file) {
		typeBindings = VF.relationWriter(bindingTupleType);
		methodBindings = VF.relationWriter(bindingTupleType);
		constructorBindings = VF.relationWriter(bindingTupleType);
		fieldBindings = VF.relationWriter(bindingTupleType);
		variableBindings = VF.relationWriter(bindingTupleType);
		
		implmnts = VF.relationWriter(typeFactTupleType);
		extnds = VF.relationWriter(typeFactTupleType);
		declaredTypes = VF.relationWriter(typeFactTupleType);
		declaredMethods = VF.relationWriter(typeFactTupleType);
		declaredFields = VF.relationWriter(typeFactTupleType);

		this.file = file;
		visitCompilationUnit();
		
		IMapWriter mw = VF.mapWriter(TF.stringType(), TF.valueType());
		mw.put(VF.string("typeBindings"), typeBindings.done());
		mw.put(VF.string("methodBindings"), methodBindings.done());
		mw.put(VF.string("constructorBindings"), constructorBindings.done());
		mw.put(VF.string("fieldBindings"), fieldBindings.done());
		mw.put(VF.string("variableBindings"), variableBindings.done());

		mw.put(VF.string("implements"), implmnts.done());
		mw.put(VF.string("extends"), extnds.done());
		mw.put(VF.string("declaredTypes"), declaredTypes.done());
		mw.put(VF.string("declaredMethods"), declaredMethods.done());
		mw.put(VF.string("declaredFields"), declaredFields.done());
		
		return mw.done();
	}
	
	private void visitCompilationUnit() {
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
		}
		
		cu.accept(this);
	}
	
	public void preVisit(ASTNode n) {
		manageStacks(n, true);
		importBindingInfo(n);
		importTypeInfo(n);
	}
	
	public void postVisit(ASTNode n) {
		manageStacks(n, false);
	}

	private void manageStacks(ASTNode n, boolean push) {
		// push == false -> pop
		ITypeBinding tb = null;
		
		if (n instanceof TypeDeclaration) {
			tb = ((TypeDeclaration) n).resolveBinding();
		} else if (n instanceof TypeDeclarationStatement) {
			tb = ((TypeDeclarationStatement) n).getDeclaration().resolveBinding();
		} else if (n instanceof AnonymousClassDeclaration) {
			tb = ((AnonymousClassDeclaration) n).resolveBinding();
		}
		
		if (tb != null) {
			if (tb.isClass()) {
				if (push) {
					classStack.push(tb);
					bindingCache.pushInitializerCounterStack();
					bindingCache.pushAnonymousClassCounterStack();
				} else {
					classStack.pop();
					bindingCache.popInitializerCounterStack();
					bindingCache.popAnonymousClassCounterStack();
				}
			}
			
			return;
		}
		
		if (n instanceof Initializer) {
			Initializer init = (Initializer) n;
			
			if (push) {
				initializerStack.push(init);
				bindingCache.pushAnonymousClassCounterStack();
			} else {
				initializerStack.pop();
				bindingCache.popAnonymousClassCounterStack();
			}
			
			return;
		}
		
		if (n instanceof MethodDeclaration) {
			if (push) {
				bindingCache.pushAnonymousClassCounterStack();
			} else {
				bindingCache.popAnonymousClassCounterStack();
			}
		}
	}
	
	private void importBindingInfo(ASTNode n) {
	
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
			Initializer possibleParent = null;
			try {
				possibleParent = initializerStack.peek();
			} catch (EmptyStackException e) {
				// ignore
			}
			
			addBinding(typeBindings, n, bindingCache.getEntity(tb, possibleParent));
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
			addBinding(methodBindings, n, bindingCache.getEntity(mb));
		}		
		if (cb != null) {
			addBinding(constructorBindings, n, bindingCache.getEntity(cb));
		}
		
		// field and variable bindings
		IVariableBinding vb = null;
		IVariableBinding fb = null;
		
		if (n instanceof EnumConstantDeclaration) {
			fb = ((EnumConstantDeclaration) n).resolveVariable();
		} else if (n instanceof FieldAccess) {
			FieldAccess fa = (FieldAccess) n;
			fb = fa.resolveFieldBinding();
			Expression exp = fa.getExpression();
			if (exp.resolveTypeBinding().isArray() && fb.getName().equals("length")) {
				// 'length' access of array object
				// put arrayLengthField in idStore, so it doesn't have to call importVariableBinding()
				// (which cannot distinguish between 'length' access and a local var inside an initializer).
				// don't include type of exp, b/c we can't do the same further down
				bindingCache.put(fb.getKey(), BindingConverter.arrayLengthField);
			}
		} else if (n instanceof SuperFieldAccess) {
			fb = ((SuperFieldAccess) n).resolveFieldBinding();
		} else if (n instanceof VariableDeclaration) {
			vb = ((VariableDeclaration) n).resolveBinding();
		} else if (n instanceof SimpleName) {
			// local variable, parameter or field.
			SimpleName name = (SimpleName)n;
			IBinding b = name.resolveBinding();
			if (b instanceof IVariableBinding) {
				vb = (IVariableBinding) b;
				
				if (vb.getDeclaringClass() != null) {
					// field
					fb = vb;
					vb = null;
				} else {
					// The field 'length' of an array type has no declaring class
					// Let's try to distinguish between 'length' access and a local variable/parameter 
					ASTNode parent = n.getParent();
					if (vb.getName().equals("length")
							&& parent != null
							&& parent instanceof QualifiedName
							&& vb.toString().equals("public final int length")) {
						// assume 'length' access of array object (local variables can't be public)
						// put arrayLengthField in idStore, so it doesn't have to call importVariableBinding()
						// (which cannot distinguish between 'length' access and a local var inside an initializer).
						// we can't get the array type of the object of which the field was accessed
						bindingCache.put(vb.getKey(), BindingConverter.arrayLengthField);						
					}
				}					
			}
		}
		
		Initializer possibleParent = null;
		try {
			possibleParent = initializerStack.peek();
		} catch (EmptyStackException e) {
			// ignore
		}
		
		if (fb != null) {
			addBinding(fieldBindings, n, bindingCache.getEntity(fb, possibleParent));
		}
		if (vb != null ) {
			addBinding(variableBindings, n, bindingCache.getEntity(vb, possibleParent));
		}
	
		
		// package bindings	
		// these only exists for package declarations, which must use the fully qualified name
		// therefore we skip these
	}
	
	private void importTypeInfo(ASTNode n) {
		ITypeBinding tb = null;
		
		if (n instanceof TypeDeclaration) {
			tb = ((TypeDeclaration) n).resolveBinding();
		}
		
		if (n instanceof TypeDeclarationStatement) {
			tb = ((TypeDeclarationStatement) n).getDeclaration().resolveBinding();
		}
		
		if (n instanceof AnonymousClassDeclaration) {
			tb = ((AnonymousClassDeclaration) n).resolveBinding();
		}
		
		if (tb != null) {
			importTypeInfo(tb);
		}
		
		//EnumDeclaration
		//EnumConstantDeclaration
		//FieldDeclaration
		//MethodDeclaration
		//Initializer
		
		if (n instanceof Initializer) {
			Initializer init = (Initializer) n;
			
			ITypeBinding parentClass = classStack.peek();
			if (parentClass != null) {				
				ITuple tup = VF.tuple(bindingCache.getEntity(parentClass), bindingCache.getEntity(init, parentClass));
				declaredMethods.insert(tup);				
			} else {
				System.err.println("dangling initializer " + init.toString());
			}
		}
		
		//method -> parameters?
		
		//method -> local variables?
		
		//throws?
		
		//modifiers?
		
		//scopes? not in JDT :(

		//calls? not in JDT :(
	}
	
	private void importTypeInfo(ITypeBinding tb) {
		IValue thisType = bindingCache.getEntity(tb);
		
		if (tb.isClass()) {
			ITypeBinding superclass = tb.getSuperclass();
			if (superclass != null) {
				ITuple tup = VF.tuple(thisType, bindingCache.getEntity(superclass));
				extnds.insert(tup);
			} else {
				extnds.insert(VF.tuple(thisType, BindingConverter.javaLangObject));
			}
		}
		
		ITypeBinding[] interfaces = tb.getInterfaces();
		if (tb.isInterface() && interfaces.length == 0) {
			extnds.insert(VF.tuple(thisType, BindingConverter.javaLangObject));
		} else {
			for (ITypeBinding interf : interfaces) {
				ITuple tup = VF.tuple(thisType, bindingCache.getEntity(interf));
			if (tb.isClass()) {
					implmnts.insert(tup);
				} else {
					extnds.insert(tup);
				}
			}
		}
		
		ITypeBinding[] innertypes = tb.getDeclaredTypes();
		for (ITypeBinding innertype : innertypes) {
			ITuple tup = VF.tuple(thisType, bindingCache.getEntity(innertype));
			declaredTypes.insert(tup);
		}
		
		// doesn't include initializers
		// these are added in importTypeInfo(ASTNode n)
		IMethodBinding[] methods = tb.getDeclaredMethods();
		for (IMethodBinding method : methods) {
			ITuple tup = VF.tuple(thisType, bindingCache.getEntity(method));
			declaredMethods.insert(tup);
		}
		
		IVariableBinding[] fields = tb.getDeclaredFields();
		for (IVariableBinding field : fields) {
			ITuple tup = VF.tuple(thisType, bindingCache.getEntity(field));
			declaredFields.insert(tup);
		}
	}
	
	private void addBinding(IRelationWriter rw, ASTNode n, IValue entity) {		
		ITuple loc = VF.tuple(VF.string(file.getLocation().toString()), VF.integer(n.getStartPosition()), VF.integer(n.getLength()));
		rw.insert(VF.tuple(loc, entity));
	}
}
