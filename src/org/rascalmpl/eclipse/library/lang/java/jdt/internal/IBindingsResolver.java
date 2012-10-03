/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Anastasia Izmaylova - A.Izmaylova@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.library.lang.java.jdt.internal;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclaration;

public interface IBindingsResolver {
	public void resolveBindings(AbstractTypeDeclaration node); // declaration
	public void resolveBindings(AnnotationTypeMemberDeclaration node); // declaration
	public void resolveBindings(AnonymousClassDeclaration node); // declaration
	public void resolveBindings(ImportDeclaration node); // declaration
	public void resolveBindings(MemberRef node); // Java doc
	public void resolveBindings(MethodDeclaration node);
	public void resolveBindings(MethodRef node); // Java doc
	public void resolveBindings(Name node); // Expression
	public void resolveBindings(PackageDeclaration node);
	public void resolveBindings(Type node); // type
	public void resolveBindings(TypeParameter node); // type
	public void resolveBindings(VariableDeclaration node); // declaration
	public void resolveBindings(ClassInstanceCreation node); // Expression
	public void resolveBindings(ConstructorInvocation node); // Statement
	public void resolveBindings(EnumConstantDeclaration node); // declaration
	public void resolveBindings(SuperConstructorInvocation node); // Statement
	public void resolveBindings(FieldAccess node); // Expression
	public void resolveBindings(SuperFieldAccess node); // Expression
	public void resolveBindings(MethodInvocation node); // Expression
	public void resolveBindings(SuperMethodInvocation node); // Expression
	public void resolveBindings(ASTNode node);
}
