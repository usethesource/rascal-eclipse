/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * 
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Bas Basten - Bas.Basten@cwi.nl (CWI)
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Anastasia Izmaylova - A.Izmaylova@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.library.lang.java.jdt.internal;

import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.ADT_ENTITY;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.ADT_ID;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_ABSTRACT;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_ANONYMOUS_CLASS;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_ARRAY;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_CAPTURE;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_CLASS;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_CONSTRUCTOR;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_DEPRECATED;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_ENTITY;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_ENUM;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_ENUM_CONSTANT;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_EXTENDS;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_FIELD;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_FINAL;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_GENERIC_CLASS;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_GENERIC_INTERFACE;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_INITIALIZER_NUMBERED;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_INTERFACE;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_METHOD;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_NATIVE;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_PACKAGE;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_PARAMETER;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_PRIMITIVE;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_PRIVATE;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_PROTECTED;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_PUBLIC;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_STATIC;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_STRICTFP;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_SUPER;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_SYNCHRONIZED;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_TRANSIENT;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_TYPE_PARAMETER;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_VARIABLE;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_VOLATILE;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_WILDCARD;
import static org.rascalmpl.eclipse.library.lang.java.jdt.internal.Java.CONS_WILDCARD_BOUND;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IListWriter;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.Modifier;
import org.rascalmpl.values.ValueFactoryFactory;

public class BindingConverter extends ASTVisitor {

	private static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	public static final Map<String, IValue> primitiveTypes;
	public static final IValue javaLangObject;
	public static final IValue deprecatedModifier;
	public static final IList arrayLengthField;
	private Stack<Integer> anonymousClassCounterStack = new Stack<Integer>();
	private Stack<Integer> initializerCounterStack = new Stack<Integer>();
	private int anonymousClassCounter = 0;
	private int initializerCounter = 0;
	private Map<Object, IList> idStore = new HashMap<Object, IList>();

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

		arrayLengthField = VF.list(VF.constructor(CONS_FIELD, (VF
				.string("length"))));
		
		deprecatedModifier = VF.constructor(CONS_DEPRECATED);
	}

	public BindingConverter() {
		super();
	}

	private static IValue createEntity(IList ids) {
		return VF.constructor(CONS_ENTITY, ids);
	}

	public IValue getEntity(IPackageBinding binding) {
		return createEntity(getIds(binding));
	}

	public IValue getEntity(ITypeBinding binding) {
		return createEntity(getIds(binding));
	}

	public IValue getEntity(ITypeBinding binding, Initializer possibleParent) {
		return createEntity(getIds(binding, possibleParent));
	}

	public IValue getEntity(IMethodBinding binding) {
		return createEntity(getIds(binding));
	}

	public IValue getEntity(IVariableBinding binding) {
		return createEntity(getIds(binding, null));
	}

	public IValue getEntity(IVariableBinding binding, Initializer possibleParent) {
		return createEntity(getIds(binding, possibleParent));
	}

	public IValue getEntity(Initializer binding) {
		return createEntity(getIds(binding, null));
	}

	public IValue getEntity(Initializer binding, ITypeBinding possibleParent) {
		return createEntity(getIds(binding, possibleParent));
	}
	
	public List<IValue> getModifiers(List list) {
		List<IValue> result = new ArrayList<IValue>();
		for (Object element : list) {
			IExtendedModifier extMod = (IExtendedModifier) element;
			
			// otherwise the modifier is an annotation. This might be @Deprecated 
			// which already has a CONS_ due to the Javadoc @deprecated tag. 
			if (extMod.isModifier()) { 
				Modifier mod = (Modifier) extMod;
				if (mod.isAbstract()) {
					result.add(VF.constructor(CONS_ABSTRACT));
				} else if (mod.isFinal()) {
					result.add(VF.constructor(CONS_FINAL));
				} else if (mod.isNative()) {
					result.add(VF.constructor(CONS_NATIVE));
				} else if (mod.isPrivate()) {
					result.add(VF.constructor(CONS_PRIVATE));
				} else if (mod.isProtected()) {
					result.add(VF.constructor(CONS_PROTECTED));
				} else if (mod.isPublic()) {
					result.add(VF.constructor(CONS_PUBLIC));
				} else if (mod.isStatic()) {
					result.add(VF.constructor(CONS_STATIC));
				} else if (mod.isStrictfp()) {
					result.add(VF.constructor(CONS_STRICTFP));
				} else if (mod.isSynchronized()) {
					result.add(VF.constructor(CONS_SYNCHRONIZED));
				} else if (mod.isTransient()) {
					result.add(VF.constructor(CONS_TRANSIENT));
				} else if (mod.isVolatile()) {
					result.add(VF.constructor(CONS_VOLATILE));
				}
			} 
		}

		return result;
	}

	public List<IValue> getModifiers(int mods) {
		List<IValue> result = new ArrayList<IValue>();
		if (Modifier.isAbstract(mods))
			result.add(VF.constructor(CONS_ABSTRACT));
		if (Modifier.isFinal(mods))
			result.add(VF.constructor(CONS_FINAL));
		if (Modifier.isNative(mods))
			result.add(VF.constructor(CONS_NATIVE));
		if (Modifier.isPrivate(mods))
			result.add(VF.constructor(CONS_PRIVATE));
		if (Modifier.isProtected(mods))
			result.add(VF.constructor(CONS_PROTECTED));
		if (Modifier.isPublic(mods))
			result.add(VF.constructor(CONS_PUBLIC));
		if (Modifier.isStatic(mods))
			result.add(VF.constructor(CONS_STATIC));
		if (Modifier.isStrictfp(mods))
			result.add(VF.constructor(CONS_STRICTFP));
		if (Modifier.isSynchronized(mods))
			result.add(VF.constructor(CONS_SYNCHRONIZED));
		if (Modifier.isTransient(mods))
			result.add(VF.constructor(CONS_TRANSIENT));
		if (Modifier.isVolatile(mods))
			result.add(VF.constructor(CONS_VOLATILE));
		return result;
	}

	public void put(Object key, IList value) {
		idStore.put(key, value);
	}

	public void pushAnonymousClassCounterStack() {
		anonymousClassCounterStack.push(Integer.valueOf(anonymousClassCounter));
		anonymousClassCounter = 0;
	}

	public void popAnonymousClassCounterStack() {
		anonymousClassCounter = anonymousClassCounterStack.pop().intValue();
	}

	public void pushInitializerCounterStack() {
		initializerCounterStack.push(Integer.valueOf(initializerCounter));
		initializerCounter = 0;
	}

	public void popInitializerCounterStack() {
		initializerCounter = initializerCounterStack.pop().intValue();
	}

	private IList getIds(IPackageBinding pb) {
		String key = pb.getKey();
		IList l = idStore.get(key);
		if (l == null) {
			l = importPackageBinding(pb);
			idStore.put(key, l);
		}
		return l;
	}

	private IList getIds(ITypeBinding tb) {
		String key = tb.getKey();
		IList l = idStore.get(key);
		if (l == null) {
			l = importTypeBinding(tb, null);
			idStore.put(key, l);
		}
		return l;
	}

	private IList getIds(ITypeBinding tb, Initializer possibleParent) {
		String key = tb.getKey();
		IList l = idStore.get(key);
		if (l == null) {
			l = importTypeBinding(tb, possibleParent);
			idStore.put(key, l);
		}
		return l;
	}

	private IList getIds(IMethodBinding mb) {
		String key = mb.getKey();
		IList l = idStore.get(key);
		if (l == null) {
			l = importMethodBinding(mb);
			idStore.put(key, l);
		}
		return l;
	}

	private IList getIds(IVariableBinding vb, Initializer possibleParent) {
		String key = vb.getKey();
		IList l = idStore.get(key);
		if (l == null) {
			l = importVariableBinding(vb, possibleParent);
			idStore.put(key, l);
		}
		return l;
	}

	private IList getIds(Initializer init, ITypeBinding possibleParent) {
		IList l = idStore.get(init);
		if (l == null) {
			l = importInitializer(init, possibleParent);
			idStore.put(init, l);
		}
		return l;
	}

	private IList importTypeBinding(ITypeBinding tb, Initializer possibleParent) {
		IListWriter lw = VF.listWriter(ADT_ID);
		IList prefix = VF.list(ADT_ID);
		IPackageBinding pb = tb.getPackage();

		if (!tb.isTypeVariable()) {
			// anonymous classes have a declaring method and a declaring class
			// therefore, prefer method prefix
			IMethodBinding declaringMethod = tb.getDeclaringMethod();
			if (declaringMethod != null) {
				prefix = getIds(declaringMethod);
			} else {
				ITypeBinding declaringClass = tb.getDeclaringClass();
				/*
				 *  Parameterized types and raw types are handled as a special case
				 */
				if(tb.isParameterizedType() || tb.isRawType()) {
					if(tb.isLocal()) {
						// if parameterized type is of a recorded local generic type, 
						// then prefix is shared with a recorded local generic type
						IList recordedLocalGenericType = idStore.get(tb.getTypeDeclaration().getKey());
						if(recordedLocalGenericType != null) {
							prefix = recordedLocalGenericType.sublist(0, recordedLocalGenericType.length() - 1);
						}
					} else if(declaringClass != null) {
						prefix = getIds(declaringClass);
					} else if(pb != null) {
						prefix = getIds(pb);
					}	
				} else {
					if (declaringClass != null && possibleParent == null) {
						// for innerclasses within initializers, getDeclaringClass()
						// returns the parent class of the initializer :-(
						prefix = getIds(declaringClass);
					} else {
						/*
						 *  The initializer is taken as a parent only if the type is local,
						 *  this check is required for uses of non-local types within initializers 
						 */
						if (possibleParent != null && tb.isLocal()) {
							prefix = getIds(possibleParent, null);	
						} else if (declaringClass != null) {
							prefix = getIds(declaringClass);
						} else if (pb != null) {
							prefix = getIds(pb);
						}
					}
				}
				/*
				if (declaringClass != null && possibleParent == null) {
					// for innerclasses within initializers, getDeclaringClass()
					// returns the parent class of the initializer :-(
					prefix = getIds(declaringClass);
				} else {
					if (possibleParent != null) {
						prefix = getIds(possibleParent, null);
					} else {
						if (pb != null) {
							prefix = getIds(pb);
						}
					}
				}
				*/
			}
		}

		IValue id = getId(tb);
		if(id != null) lw.append(id);

		return prefix.concat(lw.done());
	}

	private IList importTypeBindings(ITypeBinding tbs[]) {
		IListWriter lw = VF.listWriter(ADT_ENTITY);

		for (ITypeBinding tb : tbs) {
			lw.append(createEntity(getIds(tb)));
		}

		return lw.done();
	}

	private IList importPackageBinding(IPackageBinding pb) {
		IListWriter lw = VF.listWriter(ADT_ID);

		String components[] = pb.getNameComponents();
		for (String c : components) {
			lw.append(VF.constructor(CONS_PACKAGE, VF.string(c)));
		}

		return lw.done();
	}

	private IList importMethodBinding(IMethodBinding mb) {
		IList prefix = getIds(mb.getDeclaringClass());
		IListWriter lw = VF.listWriter(ADT_ID);

		lw.append(getId(mb));

		return prefix.concat(lw.done());
	}

	private IList importVariableBinding(IVariableBinding vb,
			Initializer possibleParent) {
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
				/*
				 * Excerpt from VariableBinding.getDeclaringMethod():
				 * 
				 * ASTNode node = this.resolver.findDeclaringNode(this); while
				 * (true) { if (node == null) break; switch(node.getNodeType())
				 * { case ASTNode.INITIALIZER : return null;
				 * 
				 * (btw: this is not seen in TypeBinding.getDeclaringMethod())
				 */

				// local variable in initializer
				if (possibleParent != null) {
					// initializer should be in cache already
					/* 
					 * The field length of an array type has no declaring class
					 */
					if(!vb.isField()) 
						prefix = getIds(possibleParent, null);
				} else {
					// let prefix remain empty
					System.err.println("dangling var " + vb.getName());
				}
			}
		}

		lw.append(getId(vb));

		return prefix.concat(lw.done());
	}

	private IList importInitializer(Initializer init, ITypeBinding possibleParent) {
		IListWriter lw = VF.listWriter(ADT_ID);
		lw.append(VF.constructor(CONS_INITIALIZER_NUMBERED, VF
				.integer(initializerCounter++)));
		IList l = lw.done();

		if (possibleParent != null) {
			IList parentName = getIds(possibleParent);
			l = parentName.concat(l);
		} else {
			System.err.println("dangling initializer " + init.toString());
		}

		return l;
	}
	
	public IValue getId(IMethodBinding mb) {
		IList params = importTypeBindings(mb.getParameterTypes());
		IValue returnType = createEntity(getIds(mb.getReturnType()));

		// TBD: mb.isVarargs() ?
		// TBD: check generic and parameterized?
		
		if (mb.isConstructor()) {
				return VF.constructor(CONS_CONSTRUCTOR, params);
		} else {
				return VF.constructor(CONS_METHOD, VF.string(mb.getName()), params, returnType);
		}
	}
	
	public IValue getId(ITypeBinding tb) {
		if (tb.getPackage() != null) {
			// class, interface or enum

			if (tb.isClass()) {
				if (tb.isGenericType()) {
					/*
					 *  The assignment below ensures type parameters, 
					 *  required, e.g., in case of static imports of generic types
					 */
					tb = tb.getTypeDeclaration();
					IValue params = importTypeBindings(tb.getTypeParameters());
					return VF.constructor(CONS_GENERIC_CLASS, VF.string(tb.getName()), params);
				} else if (tb.isParameterizedType()) {
					IValue params = importTypeBindings(tb.getTypeArguments());
					return VF.constructor(CONS_GENERIC_CLASS, VF
							/*
							 *  Type arguments are not part of a name
							 */
							.string(tb.getErasure().getName()), params);
				} else if (tb.isAnonymous()) {
					return VF.constructor(CONS_ANONYMOUS_CLASS, VF.integer(anonymousClassCounter++));
				} else { // regular class
					return VF.constructor(CONS_CLASS, VF.string(tb.getName()));
				}
			} else if (tb.isInterface()) {
				if (tb.isGenericType()) {
					/*
					 *  The assignment below ensures type parameters, 
					 *  required, e.g., in case of static imports of generic types
					 */ 
					tb = tb.getTypeDeclaration();
					IValue params = importTypeBindings(tb.getTypeParameters());
					return VF.constructor(CONS_GENERIC_INTERFACE, VF.string(tb.getName()), params);
				} else if (tb.isParameterizedType()) {
					IValue params = importTypeBindings(tb.getTypeArguments());
					return VF.constructor(CONS_GENERIC_INTERFACE, VF
							/*
							 *  Type arguments are not part of a name
							 */
							.string(tb.getErasure().getName()), params);
				} else { // regular interface
					return VF.constructor(CONS_INTERFACE, VF.string(tb.getName()));
				}
			} else if (tb.isEnum()) {
				// TBD tb.getName() can be "", when it should refer to the enum
				// constant name

				return VF.constructor(CONS_ENUM, VF.string(tb.getName()));
			} else {
				// TBD: unknown type?
				return null;
			}
		} else {
			// primitive type, array type, null type, type variable, wildcard
			// type or capture binding

			if (tb.isArray()) {
				IValue arrayType = createEntity(getIds(tb.getElementType()));
				return VF.constructor(CONS_ARRAY, arrayType);
			} else if (tb.isPrimitive() || tb.isNullType()) {
				IValue pt = primitiveTypes.get(tb.getName());
				if (pt != null) {
					return VF.constructor(CONS_PRIMITIVE, pt);
				} // TBD: else ?
				return null;
			} else if (tb.isTypeVariable()) {
				return VF.constructor(CONS_TYPE_PARAMETER, VF.string(tb.getName()));
			} else if (tb.isWildcardType()) {
				ITypeBinding bound = tb.getBound();
				if (bound == null) {
					return VF.constructor(CONS_WILDCARD);
				} else {
					IValue bnd = VF.constructor(
							tb.isUpperbound() ? CONS_EXTENDS : CONS_SUPER,
							createEntity(getIds(bound)));
					return VF.constructor(CONS_WILDCARD_BOUND, bnd);
				}
			} else if (tb.isCapture()) {
				/*
				 * Captures have declaring classes and no declaring methods
				 */
				return VF.constructor(CONS_CAPTURE, getEntity(tb.getWildcard()));
			} else {
				// TBD: unkown type?
				return null;
			}
		}
	}
	
	public IValue getId(IVariableBinding vb) {
		if (vb.isEnumConstant()) {
			return VF.constructor(CONS_ENUM_CONSTANT, VF.string(vb.getName()));
		} else if (vb.isField()) {
			// fields also include enum constants,
			// so they should be handled first
			return VF.constructor(CONS_FIELD, VF.string(vb.getName()));
		} else if (vb.isParameter()) {
			return VF.constructor(CONS_PARAMETER, VF.string(vb.getName()));
		} else {
			// local variable
			return VF.constructor(CONS_VARIABLE, VF.string(vb.getName()), VF.integer(vb.getVariableId()));
		}
	}

}