package org.rascalmpl.eclipse.library.jdt;

import static org.rascalmpl.eclipse.library.Java.ADT_ENTITY;
import static org.rascalmpl.eclipse.library.Java.ADT_MODIFIER;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.imp.pdb.facts.IRelationWriter;
import org.eclipse.imp.pdb.facts.ISetWriter;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.values.ValueFactoryFactory;

// TBD: why the difference?:
// entity([package("jdtimporter"),class("Activator   ",[entity([typeParameter("A")])]),anonymousClass(0),    method("set",[entity([package("java"),package("lang"),class("Integer")]),entity([primitive(int())])],entity([package("java"),package("lang"),class("Integer")])),parameter("element")])
// entity([package("jdtimporter"),class("Activator<A>",[entity([typeParameter("A")])]),class("new List(){}"),method("get",[                                                           entity([primitive(int())])],entity([package("java"),package("lang"),class("Integer")])),parameter("index")])



public class JDTImporter extends ASTVisitor {

	protected static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	protected static final TypeFactory TF = TypeFactory.getInstance();
	private BindingConverter bindingCache = new BindingConverter();
	private Stack<ITypeBinding> typeStack = new Stack<ITypeBinding>();
	private Stack<ASTNode> scopeStack = new Stack<ASTNode>(); // only types, methods and initializers
	private IFile file;
	private ISourceLocation loc;
	
	// bindings
	private static final Type locType = TF.sourceLocationType();
	private static final Type bindingTupleType = TF.tupleType(locType, ADT_ENTITY);

	private IRelationWriter typeBindings;
	private IRelationWriter classBindings;
	private IRelationWriter methodBindings;
	private IRelationWriter methodDecls;
	private IRelationWriter fieldDecls;
	private IRelationWriter constructorBindings;
	private IRelationWriter fieldBindings;
	private IRelationWriter variableBindings;
	private IRelationWriter packageBindings;

	// type facts
	private static final Type entityTupleType = TF.tupleType(ADT_ENTITY, "from", ADT_ENTITY, "to");
	private static final Type modifierTupleType = TF.tupleType(ADT_ENTITY, "entity", ADT_MODIFIER, "modifier");

	private IRelationWriter extnds;
	private IRelationWriter implmnts;
	private IRelationWriter declaredMethods;
	private IRelationWriter declaredFields;
	private IRelationWriter declaredSubTypes;
	private ISetWriter declaredTopTypes;
	private IRelationWriter calls;
	private IRelationWriter modifiers;
	
	public JDTImporter() {
		super();
	}
	
	public Map<String,IValue> importFacts(ISourceLocation loc, IFile file) {
		typeBindings = VF.relationWriter(bindingTupleType);
		classBindings = VF.relationWriter(bindingTupleType);
		methodBindings = VF.relationWriter(bindingTupleType);
		methodDecls = VF.relationWriter(bindingTupleType);
		fieldDecls = VF.relationWriter(bindingTupleType);
		constructorBindings = VF.relationWriter(bindingTupleType);
		fieldBindings = VF.relationWriter(bindingTupleType);
		variableBindings = VF.relationWriter(bindingTupleType);
		
		packageBindings = VF.relationWriter(bindingTupleType);
		declaredTopTypes = VF.setWriter(ADT_ENTITY);
		
		implmnts = VF.relationWriter(entityTupleType);
		extnds = VF.relationWriter(entityTupleType);
		declaredSubTypes = VF.relationWriter(entityTupleType);
		declaredMethods = VF.relationWriter(entityTupleType);
		declaredFields = VF.relationWriter(entityTupleType);
		calls = VF.relationWriter(entityTupleType);

		modifiers = VF.relationWriter(modifierTupleType);

		this.loc = loc;
		this.file = file;
		visitCompilationUnit();
		
		Map<String,IValue> facts = new HashMap<String,IValue>();
		facts.put("types", typeBindings.done());
		facts.put("methods", methodBindings.done());
		facts.put("methodDecls", methodDecls.done());
		facts.put("constructors", constructorBindings.done());
		facts.put("fields", fieldBindings.done());
		facts.put("fieldDecls", fieldDecls.done());
		facts.put("variables", variableBindings.done());
		facts.put("packages", packageBindings.done());
		facts.put("classes", classBindings.done());
		facts.put("declaredTopTypes", declaredTopTypes.done());
		facts.put("implements", implmnts.done());
		facts.put("extends", extnds.done());
		facts.put("declaredSubTypes", declaredSubTypes.done());
		facts.put("declaredMethods", declaredMethods.done());
		facts.put("declaredFields", declaredFields.done());
		facts.put("calls", calls.done());
		
		facts.put("modifiers", modifiers.done());
		
		return facts;
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
				int offset = problems[i].getSourceStart();
				int length = problems[i].getSourceEnd() - offset;
				int sl = problems[i].getSourceLineNumber();
				ISourceLocation pos = VF.sourceLocation(loc.getURI(), offset, length, sl, sl, -1, -1);
				throw new Throw(VF.string("Error(s) in compilation unit: " + problems[i].getMessage()), pos, null);
			}
		}
		
		cu.accept(this);
	}
	
	public void preVisit(ASTNode n) {
		importBindingInfo(n);
		importTypeInfo(n);
		importCalls(n);
		manageStacks(n, true);
	}
	
	public void postVisit(ASTNode n) {
		manageStacks(n, false);
	}

	private void manageStacks(ASTNode n, boolean push) {
		// push == false -> pop
		ITypeBinding tb = null;
		boolean isScope = false;
		
		tb = getBindingOfTypeScope(n);
		
		if (tb != null) {
			if (push) {
				typeStack.push(tb);
				bindingCache.pushInitializerCounterStack();
				bindingCache.pushAnonymousClassCounterStack();
			} else {
				typeStack.pop();
				bindingCache.popInitializerCounterStack();
				bindingCache.popAnonymousClassCounterStack();
			}
			
			isScope = true;
		} else {
			if (getEntityOfMethodScope(n) != null) {
				isScope = true;
			}
		}
		
		if (isScope) {
			if (push) {
				scopeStack.push(n);
				bindingCache.pushAnonymousClassCounterStack();
			} else {
				scopeStack.pop();
				bindingCache.popAnonymousClassCounterStack();
			}
			
			return;
		}
	}

	private ITypeBinding getBindingOfTypeScope(ASTNode n) {
		if (n instanceof TypeDeclaration) {
			return ((TypeDeclaration) n).resolveBinding();
		} else if (n instanceof TypeDeclarationStatement) {
			return ((TypeDeclarationStatement) n).getDeclaration().resolveBinding();
		} else if (n instanceof AnonymousClassDeclaration) {
			return ((AnonymousClassDeclaration) n).resolveBinding();
		}
		return null;
	}

	private IValue getEntityOfMethodScope(ASTNode n) {
		if (n instanceof MethodDeclaration) {
			return bindingCache.getEntity(((MethodDeclaration) n).resolveBinding());
		} else if (n instanceof Initializer) {
			return bindingCache.getEntity((Initializer) n);
		}
		return null;
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
				ASTNode scope = scopeStack.peek();
				if (scope instanceof Initializer) {
					possibleParent = (Initializer) scope;
				}
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
			// if this is a method invocation on an AnonymousClassDeclaration,
			// call importBindingInfo first to get declaration Entity.
			Expression exp = ((MethodInvocation) n).getExpression();
			if (exp instanceof ClassInstanceCreation) {
				AnonymousClassDeclaration acdc = ((ClassInstanceCreation) exp).getAnonymousClassDeclaration();
				if (acdc != null) {
					importBindingInfo(acdc);
				}
			}
		} else if (n instanceof SuperConstructorInvocation) {
			cb = ((SuperConstructorInvocation) n).resolveConstructorBinding();
		} else if (n instanceof SuperMethodInvocation) {
			mb = ((SuperMethodInvocation) n).resolveMethodBinding();
		}
		
		if (mb != null) {
			addBinding(methodBindings, n, bindingCache.getEntity(mb));
			if (n instanceof MethodDeclaration) addBinding(methodDecls, n, bindingCache.getEntity(mb));
			if (n instanceof MethodInvocation) {
				MethodInvocation mi = (MethodInvocation)n;
				int mods = mi.resolveMethodBinding().getMethodDeclaration().getModifiers();
				List<IValue> modsForN = bindingCache.getModifiers(mods);
				if (mi.resolveMethodBinding().getMethodDeclaration().isDeprecated())
					modsForN.add(BindingConverter.deprecatedModifier);
				for (IValue modifier : modsForN)
					modifiers.insert(VF.tuple(bindingCache.getEntity(mb), modifier));
			}
		}
		
		if (cb != null) {
			addBinding(constructorBindings, n, bindingCache.getEntity(cb));
		}
		
		// field and variable bindings
		IVariableBinding vb = null;
		IVariableBinding fb = null;
		
		if (n instanceof EnumConstantDeclaration) {
			fb = ((EnumConstantDeclaration) n).resolveVariable();
		} else if (n instanceof FieldDeclaration) {
			FieldDeclaration fd = (FieldDeclaration) n;
			for (Object vdfo : fd.fragments()) {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) vdfo;
				addBinding(fieldDecls, n, bindingCache.getEntity(vdf.resolveBinding()));
			}
		} else if (n instanceof FieldAccess) {
			FieldAccess fa = (FieldAccess) n;
			fb = fa.resolveFieldBinding();
			
			// check for 'length' access of array object
			Expression exp = fa.getExpression();
			if (exp.resolveTypeBinding().isArray() && fb.getName().equals("length")) {
				// put arrayLengthField in idStore, so it doesn't have to call importVariableBinding()
				// (which cannot distinguish between 'length' access and a local var inside an initializer).
				// don't include type of exp, b/c we can't do the same further down
				bindingCache.put(fb.getKey(), BindingConverter.arrayLengthField);
			}

			// if the field is accessed on an AnonymousClassDeclaration,
			// call importBindingInfo first to get declaration Entity.
			if (exp instanceof ClassInstanceCreation) {
				AnonymousClassDeclaration acdc = ((ClassInstanceCreation) exp).getAnonymousClassDeclaration();
				if (acdc != null) {
					importBindingInfo(acdc);
				}
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
		
		if (fb != null || vb != null) {
			Initializer possibleParent = null;
			try {
				ASTNode scope = scopeStack.peek();
				if (scope instanceof Initializer) {
					possibleParent = (Initializer) scope;
				}
			} catch (EmptyStackException e) {
				// ignore
			}
			
			if (fb != null) {
				addBinding(fieldBindings, n, bindingCache.getEntity(fb, possibleParent));
			}
			if (vb != null ) {
				addBinding(variableBindings, n, bindingCache.getEntity(vb, possibleParent));
			}
		}
		
		// package bindings	
		IPackageBinding pb = null; 
		if (n instanceof PackageDeclaration) {
			pb = ((PackageDeclaration) n).resolveBinding();
		}
		
		if (pb != null) {
			addBinding(packageBindings, n, bindingCache.getEntity(pb));
		}
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
			if (tb.isClass()) addBinding(classBindings, n, bindingCache.getEntity(tb));
		}
		
		//EnumDeclaration
		//EnumConstantDeclaration
		//FieldDeclaration
		//MethodDeclaration
		//Initializer
		
		if (n instanceof Initializer) {
			Initializer init = (Initializer) n;
			
			ITypeBinding parentType = typeStack.peek();
			if (parentType != null) {
				ITuple tup = VF.tuple(bindingCache.getEntity(parentType), bindingCache.getEntity(init, parentType));
				declaredMethods.insert(tup);				
			} else {
				System.err.println("dangling initializer " + init.toString());
			}
		}
		
		if (n instanceof BodyDeclaration) {			
			List<IValue> owners = new ArrayList<IValue>();
			if (n instanceof AbstractTypeDeclaration) {
				 owners.add(bindingCache.getEntity(((AbstractTypeDeclaration) n).resolveBinding()));
			} else if (n instanceof AnnotationTypeMemberDeclaration) {
				owners.add(bindingCache.getEntity(((AnnotationTypeMemberDeclaration)n).resolveBinding()));
			} else if (n instanceof Initializer) {
				owners.add(bindingCache.getEntity((Initializer) n));
			} else if (n instanceof MethodDeclaration) {
				owners.add(bindingCache.getEntity(((MethodDeclaration)n).resolveBinding()));
			} else if (n instanceof FieldDeclaration) {
				for (Object fragment: ((FieldDeclaration)n).fragments()) {
					owners.add(bindingCache.getEntity(((VariableDeclarationFragment) fragment).resolveBinding()));
				}
			} else if (n instanceof EnumConstantDeclaration) {
				owners.add(bindingCache.getEntity(((EnumConstantDeclaration)n).resolveConstructorBinding()));
				owners.add(bindingCache.getEntity(((EnumConstantDeclaration)n).resolveVariable()));
			}
			
			BodyDeclaration bd = (BodyDeclaration) n;
			List<IValue> modsForN = bindingCache.getModifiers(bd.modifiers());
			
			Javadoc doc = bd.getJavadoc();
			if (doc != null) {
				for (Object te : doc.tags()) {
					if (TagElement.TAG_DEPRECATED.equals(((TagElement)te).getTagName())) {
						modsForN.add(BindingConverter.deprecatedModifier);
						break;
					}
				}
			}
			
			for (IValue owner: owners) {
				for (IValue modifier: modsForN) {
					modifiers.insert(VF.tuple(owner, modifier));
				}
			}
		}
		
		//method -> parameters?
		
		//method -> local variables?
		
		//throws?
		
		//modifiers?
		
		//scopes? not in JDT :(
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

		if ((tb.isClass() || tb.isInterface() || tb.isEnum()) && tb.getDeclaringClass() == null) {
			declaredTopTypes.insert(thisType);
		} 
		
		// doesn't include anonymous classes
		ITypeBinding[] innertypes = tb.getDeclaredTypes();
		for (ITypeBinding innertype : innertypes) {
			ITuple tup = VF.tuple(thisType, bindingCache.getEntity(innertype));
			declaredSubTypes.insert(tup);
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
	
	private void importCalls(ASTNode n) {		
		IMethodBinding mb = null;
		if (n instanceof MethodInvocation) {
			mb = ((MethodInvocation) n).resolveMethodBinding();
		} else if (n instanceof ConstructorInvocation) {
			mb = ((ConstructorInvocation) n).resolveConstructorBinding();
		} else if (n instanceof SuperConstructorInvocation) {
			mb = ((SuperConstructorInvocation) n).resolveConstructorBinding();
		} else if (n instanceof SuperMethodInvocation) {
			mb = ((SuperMethodInvocation) n).resolveMethodBinding();
		}
		
		if (mb != null) {
			IValue callee = bindingCache.getEntity(mb);
			
			IValue caller = null;
			if (scopeStack.size() > 0) {
				ASTNode scope = scopeStack.peek();
				
				ITypeBinding tb = getBindingOfTypeScope(scope);
				if (tb != null) {
					caller = bindingCache.getEntity(tb);
				} else {
					caller = getEntityOfMethodScope(scope);
				}
			} else {
				// should not happen
			}

			if (caller != null) {
				calls.insert(VF.tuple(caller, callee));
			}
		}
	}

	private void addBinding(IRelationWriter rw, ASTNode n, IValue entity) {		
		ISourceLocation fileLoc = new org.rascalmpl.eclipse.library.Resources(VF).makeFile(file);
		ISourceLocation loc = VF.sourceLocation(fileLoc.getURI(), n.getStartPosition(), n.getLength(), -1, -1, -1, -1);
		rw.insert(VF.tuple(loc, entity));
	}
}
