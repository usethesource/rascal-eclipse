/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.outline;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.preferences.PreferenceValueParser.optParameter;
import org.eclipse.imp.services.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.rascalmpl.ast.AbstractAST;
import org.rascalmpl.ast.Declaration;
import org.rascalmpl.ast.FunctionDeclaration;
import org.rascalmpl.ast.Import;
import org.rascalmpl.ast.ImportedModule;
import org.rascalmpl.ast.Module;
import org.rascalmpl.ast.Prod;
import org.rascalmpl.ast.Signature;
import org.rascalmpl.ast.Sym;
import org.rascalmpl.ast.Variant;
import org.rascalmpl.ast.Declaration.Variable;
import org.rascalmpl.eclipse.outline.TreeModelBuilder.Group;
import org.rascalmpl.interpreter.utils.Names;
import org.rascalmpl.semantics.dynamic.QualifiedName.Default;
import org.rascalmpl.values.uptr.TreeAdapter;

public class LabelProvider implements ILabelProvider, ILanguageService  {
	private Set<ILabelProviderListener> fListeners = new HashSet<ILabelProviderListener>();

	public Image getImage(Object element) {
		return null;
	}
	
	public String getText(Object element) {
		if (element instanceof ModelTreeNode) {
			ModelTreeNode node = (ModelTreeNode) element;
			
			Object node2 = node.getASTNode();
			
			if (node2 instanceof AbstractAST) {
				return getLabelFor((AbstractAST) node2);
			}
			else if (node2 instanceof IConstructor) {
				return getLabelFor((IConstructor) node2);
			}
			else if (node2 instanceof Group<?>) {
				return getLabelFor((Group<?>) node2);
			}
		}
		else if (element instanceof Group<?>) {
			return getLabelFor((Group<?>) element);
		}
		else if (element instanceof IConstructor) {
			return getLabelFor((IConstructor) element);
		}
		
		return "***";
	}
	
	private String getLabelFor(Group<?> group) {
		return group.getName();
	}

	private String getLabelFor(AbstractAST node2) {
		String result;
		
		if (node2 instanceof Module) {
			result = ((org.rascalmpl.semantics.dynamic.QualifiedName.Default) ((Module) node2).getHeader().getName()).fullName();
		}
		else if (node2 instanceof Prod.Labeled) {
			Prod p = (Prod) node2;
			result = "| " + Names.name(p.getName());
		}
		else if (node2 instanceof Prod.Unlabeled) {
			result = "| ...";
		}
		else if (node2 instanceof Declaration.Function) {
			Signature signature = ((Declaration.Function) node2).getFunctionDeclaration().getSignature();
			result = Names.name(signature.getName());
		}
		else if (node2 instanceof FunctionDeclaration) {
			Signature signature = ((Declaration.Function) node2).getFunctionDeclaration().getSignature();
			result = Names.name(signature.getName());
		}
		else if (node2 instanceof org.rascalmpl.ast.Variable) {
			org.rascalmpl.ast.Variable v = (org.rascalmpl.ast.Variable) node2;
			result = Names.name(v.getName());
		}
		else if (node2 instanceof Declaration.Variable) {
			Declaration.Variable var = (Variable) node2;
			result = Names.name(var.getName()) + ": " + var.getType();
		}
		else if (node2 instanceof Declaration.Data) {
			result = ((Default) ((Declaration.Data) node2).getUser().getName()).fullName();
		}
		else if (node2 instanceof Declaration.DataAbstract) {
			result = ((Default) ((Declaration.DataAbstract) node2).getUser().getName()).fullName();
		}
		else if (node2 instanceof Declaration.Alias) {
			result = ((Default) ((Declaration.Alias) node2).getUser().getName()).fullName();
		}
		else if (node2 instanceof Variant.NAryConstructor) {
			Variant v = (Variant) node2;
			result = Names.name(v.getName()); 
		}
		else if (node2 instanceof ImportedModule) {
			ImportedModule i = (ImportedModule) node2;
			result = (((Default) i.getName()).fullName());
		}
		else {
		    result = "???";
		}
		return result.replaceAll("\n", " ").trim();
	}

	private String getLabelFor(IConstructor node) {
		return TreeAdapter.yield(node);
	}

	public void addListener(ILabelProviderListener listener) {
		fListeners.add(listener);
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		fListeners.remove(listener);
	}
}
