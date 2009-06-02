package org.meta_environment.rascal.eclipse.lib.jdt;

import static org.meta_environment.rascal.eclipse.lib.Java.*;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IListWriter;
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
import org.eclipse.jdt.core.dom.IPackageBinding;
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
import org.meta_environment.rascal.eclipse.lib.Java;
import org.meta_environment.rascal.interpreter.control_exceptions.Throw;

// TBD: why the difference?:
// entity([package("jdtimporter"),class("Activator",[entity([typeParameter("A")])]),anonymousClass(0),method("set",[entity([package("java"),package("lang"),class("Integer")]),entity([primitive(int())])],entity([package("java"),package("lang"),class("Integer")])),parameter("element")])
// entity([package("jdtimporter"),class("Activator<A>",[entity([typeParameter("A")])]),class("new List(){}"),method("get",[entity([primitive(int())])],entity([package("java"),package("lang"),class("Integer")])),parameter("index")])



public class JDTImporter extends ASTVisitor {

	//private static final IWorkspaceRoot ROOT = ResourcesPlugin.getWorkspace().getRoot();
    private static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	private static final TypeFactory TF = TypeFactory.getInstance();
	private static Map<String, IValue> primitiveTypes;
	private static IValue javaLangObject;
	private static IList arrayLengthField;

	static {
		primitiveTypes = new HashMap<String, IValue>();
		
		primitiveTypes.put("byte", VF.constructor(Java.CONS_BYTE));
		primitiveTypes.put("short", VF.constructor(Java.CONS_SHORT));
		primitiveTypes.put("int", VF.constructor(Java.CONS_INT));
		primitiveTypes.put("long", VF.constructor(Java.CONS_LONG));
		primitiveTypes.put("float", VF.constructor(Java.CONS_FLOAT));
		primitiveTypes.put("double", VF.constructor(Java.CONS_DOUBLE));
		primitiveTypes.put("char", VF.constructor(Java.CONS_CHAR));
		primitiveTypes.put("boolean", VF.constructor(Java.CONS_BOOLEAN));
		primitiveTypes.put("void", VF.constructor(Java.CONS_VOID));
		primitiveTypes.put("null", VF.constructor(Java.CONS_NULL));
		
		IListWriter lw = VF.listWriter(ADT_ID);
		lw.append(VF.constructor(CONS_PACKAGE, VF.string("java")));
		lw.append(VF.constructor(CONS_PACKAGE, VF.string("lang")));
		lw.append(VF.constructor(CONS_CLASS, VF.string("Object")));		
		javaLangObject = createEntity(lw.done());
		
		arrayLengthField = VF.list(VF.constructor(CONS_FIELD, (VF.string("length"))));
	}
	
	private IFile file;
	private int anonymousClassCounter = 0;
	private int initializerCounter = 0;
	private Map<Object, IList> idStore = new HashMap<Object, IList>();
	private Stack<ITypeBinding> classStack = new Stack<ITypeBinding>();
	private Stack<Initializer> initializerStack = new Stack<Initializer>();
	
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
			//System.out.println(problems[i].getMessage());
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
			addBinding(typeBindings, n, getIds(tb));
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
			addBinding(methodBindings, n, getIds(mb));
		}		
		if (cb != null) {
			addBinding(constructorBindings, n, getIds(cb));
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
				// (which cannot distinguish between 'length' access and a local var inside an initializer)
				idStore.put(fb.getKey(), arrayLengthField);
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
						// assume 'length' access of array object
						// put arrayLengthField in idStore, so it doesn't have to call importVariableBinding()
						// (which cannot distinguish between 'length' access and a local var inside an initializer)
						idStore.put(vb.getKey(), arrayLengthField);						
					}
				}					
			}
		}
		
		if (fb != null) {
			addBinding(fieldBindings, n, getIds(fb));
		}
		if (vb != null ) {
			addBinding(variableBindings, n, getIds(vb));
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
			
			IListWriter lw = VF.listWriter(ADT_ID);
			lw.append(VF.constructor(CONS_INITIALIZER_NUMBERED, VF.integer(initializerCounter++))); // TBD: get number!
			IList l = lw.done();
			
			ITypeBinding parentClass = classStack.peek();
			if (parentClass != null) {
				IList parentName = getIds(parentClass);
				l = parentName.concat(l);
				
				ITuple tup = VF.tuple(createEntity(parentName), createEntity(l));
				declaredMethods.insert(tup);				
			} else {
				System.out.println("dangling initializer " + init.toString());
			}
			
			idStore.put(init, l);
			
			//System.out.println("initializer " + init.toString());
		}
		
		//method -> parameters?
		
		//method -> local variables?
		
		//throws?
		
		//modifiers?
		
		//scopes? not in JDT :(

		//calls? not in JDT :(
	}
	
	private void importTypeInfo(ITypeBinding tb) {
		IValue thisType = createEntity(getIds(tb));
		
		if (tb.isClass()) {
			ITypeBinding superclass = tb.getSuperclass();
			if (superclass != null) {
				ITuple tup = VF.tuple(thisType, createEntity(getIds(superclass)));
				extnds.insert(tup);
			} else {
				extnds.insert(VF.tuple(thisType, javaLangObject));
			}
		}
		
		ITypeBinding[] interfaces = tb.getInterfaces();
		if (tb.isInterface() && interfaces.length == 0) {
			extnds.insert(VF.tuple(thisType, javaLangObject));
		} else {
			for (ITypeBinding interf : interfaces) {
				ITuple tup = VF.tuple(thisType, createEntity(getIds(interf)));
				if (tb.isClass()) {
					implmnts.insert(tup);
				} else {
					extnds.insert(tup);
				}
			}
		}
		
		ITypeBinding[] innertypes = tb.getDeclaredTypes();
		for (ITypeBinding innertype : innertypes) {
			ITuple tup = VF.tuple(thisType, createEntity(getIds(innertype)));
			declaredTypes.insert(tup);
		}
		
		// doesn't include initializers
		// these are added in importTypeInfo(ASTNode n)
		IMethodBinding[] methods = tb.getDeclaredMethods();
		for (IMethodBinding method : methods) {
			ITuple tup = VF.tuple(thisType, createEntity(getIds(method)));
			declaredMethods.insert(tup);
		}
		
		IVariableBinding[] fields = tb.getDeclaredFields();
		for (IVariableBinding field : fields) {
			ITuple tup = VF.tuple(thisType, createEntity(getIds(field)));
			declaredFields.insert(tup);
		}
	}

	private void manageStacks(ASTNode n, boolean push) {
		// push == false -> pop
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
			if (tb.isClass()) {
				if (push) {
					classStack.push(tb);
				} else {
					classStack.pop();
				}
			}
		}
		
		if (n instanceof Initializer) {
			Initializer init = (Initializer) n;
			
			if (push) {
				initializerStack.push(init);
			} else {
				initializerStack.pop();
			}
		}
	}
	
	static IValue createEntity(IList ids) {
		return VF.constructor(CONS_ENTITY, ids);
	}
	
	IList getIds(IPackageBinding pb) {
		String key = pb.getKey();
		IList l = idStore.get(key); 
		if (l == null) {
			l = importPackageBinding(pb);
			idStore.put(key, l);
		}
		return l;
	}
	
	IList getIds(ITypeBinding tb) {
		String key = tb.getKey();
		IList l = idStore.get(key); 
		if (l == null) {
			l = importTypeBinding(tb);
			idStore.put(key, l);
		}
		return l;
	}

	IList getIds(IMethodBinding mb) {
		String key = mb.getKey();
		IList l = idStore.get(key); 
		if (l == null) {
			l = importMethodBinding(mb);
			idStore.put(key, l);
		}
		return l;
	}

	IList getIds(IVariableBinding vb) {
		String key = vb.getKey();
		IList l = idStore.get(key); 
		if (l == null) {
			l = importVariableBinding(vb);
			idStore.put(key, l);
		}
		return l;
	}
	
	IList getIds(Initializer init) {
		IList l = idStore.get(init); //should always exist
		return l;
	}
	
	IList importTypeBinding(ITypeBinding tb) {
		IListWriter lw = VF.listWriter(ADT_ID);
		IList prefix = VF.list(ADT_ID);
		IPackageBinding pb = tb.getPackage();
		
		if (!tb.isTypeVariable()) {
			ITypeBinding declaringClass = tb.getDeclaringClass();
			if (declaringClass != null) {
				prefix = getIds(declaringClass);
			} else {
				IMethodBinding declaringMethod = tb.getDeclaringMethod();
				if (declaringMethod != null) {
					prefix = getIds(declaringMethod);
				} else {
					if (pb != null) {
						prefix = getIds(pb);
					}
				}			
			}
		}
		
		if (pb != null) {
			// class, interface or enum
			
			if (tb.isClass()) {
				if (tb.isGenericType()) {
					IValue params = importTypeBindings(tb.getTypeParameters());
					lw.append(VF.constructor(CONS_GENERIC_CLASS, VF.string(tb.getName()), params));
				} else if (tb.isParameterizedType()) {
					IValue params = importTypeBindings(tb.getTypeArguments());
					lw.append(VF.constructor(CONS_GENERIC_CLASS, VF.string(tb.getName()), params));
				} else if (tb.isAnonymous()) {
					//TBD: find out another way to get the anonymous class number...
					lw.append(VF.constructor(CONS_ANONYMOUS_CLASS, VF.integer(anonymousClassCounter++)));
				} else { // regular class
					lw.append(VF.constructor(CONS_CLASS, VF.string(tb.getName())));
				}
			} else if (tb.isInterface()) {
				if (tb.isGenericType()) {
					IValue params = importTypeBindings(tb.getTypeParameters());
					lw.append(VF.constructor(CONS_GENERIC_INTERFACE, VF.string(tb.getName()), params));
				} else if (tb.isParameterizedType()) {
					IValue params = importTypeBindings(tb.getTypeArguments());
					lw.append(VF.constructor(CONS_GENERIC_INTERFACE, VF.string(tb.getName()), params));
				} else { // regular interface
					lw.append(VF.constructor(CONS_INTERFACE, VF.string(tb.getName())));
				}			
			} else if (tb.isEnum()) {
				lw.append(VF.constructor(CONS_ENUM, VF.string(tb.getName())));
			} else {
				//TBD: unknown type?
			}
		} else {
			// primitive type, array type, null type, type variable, wildcard type or capture binding
			
			if (tb.isArray()) {
				IValue arrayType = createEntity(getIds(tb.getElementType()));
				lw.append(VF.constructor(CONS_ARRAY, arrayType));
			} else

			if (tb.isPrimitive() || tb.isNullType()) {
				IValue pt = primitiveTypes.get(tb.getName());
				if (pt != null) {					
					lw.append(VF.constructor(CONS_PRIMITIVE, pt));
				} // TBD: else ?
			} else
			
			if (tb.isTypeVariable()) {
				lw.append(VF.constructor(CONS_TYPE_PARAMETER, VF.string(tb.getName())));
			} else
			
			if (tb.isWildcardType()) {
				lw.append(VF.constructor(CONS_WILDCARD));
			} else
				
			if (tb.isCapture()) {
				//tb.getWildcard();
				//TBD
			} else {
				//TBD: unkown type?
			}
		}
						
		return prefix.concat(lw.done());
	}

	IList importTypeBindings(ITypeBinding tbs[]) {
		IListWriter lw = VF.listWriter(ADT_ENTITY);
		
		for (ITypeBinding tb : tbs) {
			lw.insert(createEntity(getIds(tb)));
		}
		
		return lw.done();
	}

	IList importPackageBinding(IPackageBinding pb) {
		IListWriter lw = VF.listWriter(ADT_ID);
		
		String components[] = pb.getNameComponents();
		for (String c : components) {
			lw.append(VF.constructor(CONS_PACKAGE, VF.string(c)));
		}
		
		return lw.done();
	}
	
	IList importMethodBinding(IMethodBinding mb) {
		IList prefix = getIds(mb.getDeclaringClass());
		IListWriter lw = VF.listWriter(ADT_ID);
		
		IList params = importTypeBindings(mb.getParameterTypes());		
		IValue returnType = createEntity(getIds(mb.getReturnType()));
	
		//TBD: mb.isVarargs() ?
		//TBD: check generic and parameterized?
		
		if (mb.isConstructor()) {
			lw.append(VF.constructor(CONS_CONSTRUCTOR, params));	
		} else {
			lw.append(VF.constructor(CONS_METHOD, VF.string(mb.getName()), params, returnType));
		}
		
		return prefix.concat(lw.done());
	}
	
	IList importVariableBinding(IVariableBinding vb) {
		IListWriter lw = VF.listWriter(ADT_ID);
		IList prefix = VF.list(ADT_ID);
		
		ITypeBinding declaringClass = vb.getDeclaringClass();
		if (declaringClass != null) {
			prefix = getIds(declaringClass);
		} else {
			IMethodBinding declaringMethod = vb.getDeclaringMethod();
			if (declaringMethod != null) {			
				prefix = getIds(declaringMethod);
			} else {
				//local variable in initializer
				try {
					Initializer parent = initializerStack.peek();
					prefix = getIds(parent);
				} catch (EmptyStackException e) {
					//let prefix remain empty
					System.out.println("dangling var " + vb.getName());
				}
			}
		}
		
		if (vb.isEnumConstant()) {
			lw.append(VF.constructor(CONS_ENUM_CONSTANT, VF.string(vb.getName())));
		} else if (vb.isField()) {
			// fields also include enum constants, so they should be handled first
			lw.append(VF.constructor(CONS_FIELD, VF.string(vb.getName())));
		} else if (vb.isParameter()) {
			lw.append(VF.constructor(CONS_PARAMETER, VF.string(vb.getName())));
		} else {
			// local variable
			lw.append(VF.constructor(CONS_VARIABLE, VF.string(vb.getName())));
		}

		return prefix.concat(lw.done());
	}

	void addBinding(IRelationWriter rw, ASTNode n, IList ids) {		
		ITuple loc = VF.tuple(VF.string(file.getLocation().toString()), VF.integer(n.getStartPosition()), VF.integer(n.getLength()));
		rw.insert(VF.tuple(loc, createEntity(ids)));
	}
}
