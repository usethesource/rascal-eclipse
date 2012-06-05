/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Anastasia Izmaylova - A.Izmaylova@cwi.nl (CWI)
*******************************************************************************/
package org.rascalmpl.eclipse.library.lang.java.jdt.internal;

import java.util.Stack;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclaration;

public abstract class BindingsResolver implements IBindingsResolver {	
	
	private Stack<ASTNode> scopeStack; 
	private Stack<ITypeBinding> typeStack; 
	
	private BindingConverter bindingConverter;
	
	public BindingsResolver(final BindingConverter bindingConverter) {
		this.bindingConverter = bindingConverter;
		this.scopeStack = new Stack<ASTNode>();
		this.typeStack = new Stack<ITypeBinding>();
	}
	
	public void resolveBindings(ASTNode node) { 
		if(node instanceof AbstractTypeDeclaration) resolveBindings((AbstractTypeDeclaration) node);
		else if(node instanceof AnnotationTypeMemberDeclaration) resolveBindings((AnnotationTypeMemberDeclaration) node);
		else if(node instanceof AnonymousClassDeclaration) resolveBindings((AnonymousClassDeclaration) node);
		else if(node instanceof EnumConstantDeclaration) resolveBindings((EnumConstantDeclaration) node);
		else if(node instanceof Expression) resolveBindings((Expression) node);
		else if(node instanceof ImportDeclaration) resolveBindings((ImportDeclaration) node);
		else if(node instanceof MemberRef) resolveBindings((MemberRef) node);
		else if(node instanceof MethodDeclaration) resolveBindings((MethodDeclaration) node);
		else if(node instanceof MethodRef) resolveBindings((MethodRef) node);
		else if(node instanceof PackageDeclaration) resolveBindings((PackageDeclaration) node);
		else if(node instanceof Statement) resolveBindings((Statement) node);
		else if(node instanceof Type) resolveBindings((Type) node);
		else if(node instanceof TypeParameter) resolveBindings((TypeParameter) node);
		else if(node instanceof VariableDeclaration) resolveBindings((VariableDeclaration) node);
	}
	
	public void manageStacks(ASTNode n, boolean push) {
		boolean isScope = false;
		ITypeBinding tb = getTypeDeclarationBinding(n);

		if (tb != null) {
			if (push) {
				typeStack.push(tb);
				bindingConverter.pushInitializerCounterStack();
			} else { 
				typeStack.pop();
				bindingConverter.popInitializerCounterStack();
			}
			isScope = true;
		} else {
			if (getMethodDeclarationBinding(n) != null) 
				isScope = true;
		}
		if (isScope) {
			if (push) {
				scopeStack.push(n);	
				bindingConverter.pushAnonymousClassCounterStack();
			} else {
				scopeStack.pop();
				bindingConverter.popAnonymousClassCounterStack();
			}
		}
	}
	
	private ITypeBinding getTypeDeclarationBinding(ASTNode n) {
		if (n instanceof AbstractTypeDeclaration) {
			return ((AbstractTypeDeclaration) n).resolveBinding();
		} else if (n instanceof AnonymousClassDeclaration) {
			return ((AnonymousClassDeclaration) n).resolveBinding();
		}
		return null;
	}
	
	private IValue getMethodDeclarationBinding(ASTNode n) {
		if (n instanceof MethodDeclaration) {
			return bindingConverter.getEntity(((MethodDeclaration) n).resolveBinding());
		} else if (n instanceof Initializer) {
			ITypeBinding parentType = typeStack.peek();
			return bindingConverter.getEntity((Initializer) n, parentType);
		}
		return null;
	}
	
	public void resolveBindings(AbstractTypeDeclaration node) {
		importBinding(node.resolveBinding());
	}
	
	public void resolveBindings(AnnotationTypeMemberDeclaration node) {
		importBinding(node.resolveBinding());
	}
	
	public void resolveBindings(AnonymousClassDeclaration node) {
		importBinding(node.resolveBinding());
	}
	
	public void resolveBindings(ImportDeclaration node) {
		IBinding binding = node.resolveBinding();
		if(binding instanceof IMethodBinding) importBinding((IMethodBinding) binding);
		else if (binding instanceof ITypeBinding) importBinding((ITypeBinding) binding);
		else if (binding instanceof IVariableBinding) importBinding((IVariableBinding) binding);
	}
	
	public void resolveBindings(MemberRef node) { /* Java doc */ }
	
	public void resolveBindings(MethodDeclaration node) {
		importBinding(node.resolveBinding());
	}
	
	public void resolveBindings(MethodRef node) { /* Java doc */ }
	
	public void resolveBindings(Name node) {
		IBinding binding = node.resolveBinding();
		if(binding instanceof IMethodBinding) 
			importBinding((IMethodBinding) binding);
		else if(binding instanceof IPackageBinding)
			importBinding((IPackageBinding) binding);
		else if(binding instanceof IVariableBinding)
			importBinding((IVariableBinding) binding);
		if(node.resolveTypeBinding() != null) 
			importBinding(node.resolveTypeBinding());
	}
	
	public void resolveBindings(PackageDeclaration node) {
		importBinding(node.resolveBinding());
	}
	
	public void resolveBindings(Type node) {
		importBinding(node.resolveBinding());
	}
	
	public void resolveBindings(TypeParameter node) {
		importBinding(node.resolveBinding());
	}
	
	public void resolveBindings(VariableDeclaration node) {
		IVariableBinding variableBinding = node.resolveBinding();
		importBinding(variableBinding);
		importBinding(variableBinding.getType());
	}
	
	public void resolveBindings(ClassInstanceCreation node) {
		importBinding(node.resolveConstructorBinding());
		importBinding(node.resolveTypeBinding());
	}
	
	public void resolveBindings(ConstructorInvocation node) {
		importBinding(node.resolveConstructorBinding());
		importBinding(node.resolveConstructorBinding().getReturnType());
	}
	
	public void resolveBindings(EnumConstantDeclaration node) {
		importBinding(node.resolveConstructorBinding());
		importBinding(node.resolveVariable());
	}
	
	public void resolveBindings(SuperConstructorInvocation node) {
		importBinding(node.resolveConstructorBinding());
		importBinding(node.resolveConstructorBinding().getReturnType());
	}
	
	public void resolveBindings(FieldAccess node) {
		importBinding(node.resolveFieldBinding());
		importBinding(node.resolveTypeBinding());
	}
	
	public void resolveBindings(SuperFieldAccess node) {
		importBinding(node.resolveFieldBinding());
		importBinding(node.resolveTypeBinding());
	}
	
	public void resolveBindings(MethodInvocation node) {
		importBinding(node.resolveMethodBinding());
		importBinding(node.resolveTypeBinding());
	}
	
	public void resolveBindings(SuperMethodInvocation node) {
		importBinding(node.resolveMethodBinding());
		importBinding(node.resolveTypeBinding());
	}
	
	private void resolveBindings(Expression node) {
		if(node instanceof ClassInstanceCreation) resolveBindings((ClassInstanceCreation) node);
		else if(node instanceof FieldAccess) resolveBindings((FieldAccess) node);
		else if(node instanceof SuperFieldAccess) resolveBindings((SuperFieldAccess) node);
		else if(node instanceof MethodInvocation) resolveBindings((MethodInvocation) node);
		else if(node instanceof SuperMethodInvocation) resolveBindings((SuperMethodInvocation) node);
		else if(node instanceof Name) resolveBindings((Name) node);
		else importBinding(node.resolveTypeBinding());
	}
	
	private void resolveBindings(Statement node) {
		if(node instanceof ConstructorInvocation) resolveBindings((ConstructorInvocation) node);
		else if(node instanceof SuperConstructorInvocation) resolveBindings((SuperConstructorInvocation) node);
	}
	
	/* 
	 * Methods to be implemented
	 */
	abstract public void importBinding(IMethodBinding binding);	
	abstract public void importBinding(IPackageBinding binding);
	abstract public void importBinding(ITypeBinding binding, Initializer initializer);
	abstract public void importBinding(IVariableBinding binding, Initializer initializer);
	
	private void importBinding(ITypeBinding binding) {
		if((!scopeStack.empty()) && (scopeStack.peek() instanceof Initializer)) 
			importBinding(binding, (Initializer) scopeStack.peek());
		else importBinding(binding, null);
	}
	
	private void importBinding(IVariableBinding binding) { 
		if((!scopeStack.empty()) && (scopeStack.peek() instanceof Initializer))
			importBinding(binding, (Initializer) scopeStack.peek());
		else importBinding(binding, null);
	}
	
	public ITypeBinding getEnclosingType() {
		if(!typeStack.empty()) return typeStack.peek();
		return null;
	}
	
	public ASTNode getEnclosingScope() {
		if(!scopeStack.empty()) return scopeStack.peek();
		return null;
	}
	
}
