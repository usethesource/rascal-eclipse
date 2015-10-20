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
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.rascalmpl.ast.AbstractAST;
import org.rascalmpl.ast.Declaration.Alias;
import org.rascalmpl.ast.Declaration.Annotation;
import org.rascalmpl.ast.Declaration.Data;
import org.rascalmpl.ast.Declaration.DataAbstract;
import org.rascalmpl.ast.Declaration.Function;
import org.rascalmpl.ast.FunctionDeclaration;
import org.rascalmpl.ast.FunctionDeclaration.Abstract;
import org.rascalmpl.ast.FunctionDeclaration.Conditional;
import org.rascalmpl.ast.FunctionDeclaration.Expression;
import org.rascalmpl.ast.ImportedModule.Actuals;
import org.rascalmpl.ast.ImportedModule.ActualsRenaming;
import org.rascalmpl.ast.ImportedModule.Renamings;
import org.rascalmpl.ast.NullASTVisitor;
import org.rascalmpl.ast.Prod.Labeled;
import org.rascalmpl.ast.Prod.Unlabeled;
import org.rascalmpl.ast.Variable.Initialized;
import org.rascalmpl.ast.Variable.UnInitialized;
import org.rascalmpl.ast.Variant.NAryConstructor;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.outline.TreeModelBuilder.Group;
import org.rascalmpl.interpreter.utils.Names;
import org.rascalmpl.value.IConstructor;
import org.rascalmpl.values.uptr.TreeAdapter;

import io.usethesource.impulse.editor.ModelTreeNode;
import io.usethesource.impulse.language.ILanguageService;
import io.usethesource.impulse.services.ILabelProvider;

public class LabelProvider implements ILabelProvider, ILanguageService  {
	private Set<ILabelProviderListener> fListeners = new HashSet<ILabelProviderListener>();
	private Image cachedImage;
	
	public Image getImage(Object element) {
		if (element instanceof IFile || element instanceof IProject) {
			return getRascalImage();
		}
		
		return null;
	}
	
	private synchronized Image getRascalImage() {
		if (cachedImage == null) {
			cachedImage = Activator.getRascalImage().createImage();
		}
		return cachedImage;
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
		return group.getName() + " (" + group.size() + ")";
	}

	private String getLabelFor(AbstractAST ast) {
		String result =  ast.accept(new NullASTVisitor<String>() {
			@Override
			public String visitModuleDefault(org.rascalmpl.ast.Module.Default x) {
				return Names.fullName(x.getHeader().getName());
			}
			
			@Override
			public String visitProdLabeled(Labeled x) {
				return "| " + Names.name(x.getName());
			}
			
			@Override
			public String visitProdUnlabeled(Unlabeled x) {
				return "| ...";
			}
			
			@Override
			public String visitDeclarationFunction(Function x) {
				return x.getFunctionDeclaration().accept(this);
			}
			
			@Override
			public String visitFunctionDeclarationDefault(
					org.rascalmpl.ast.FunctionDeclaration.Default x) {
				return visitAnyFunctionDeclaration(x);
			}
			
			private String visitAnyFunctionDeclaration(FunctionDeclaration x) {
				return Names.name(x.getSignature().getName());
			}
			
			@Override
			public String visitFunctionDeclarationAbstract(Abstract x) {
				return visitAnyFunctionDeclaration(x);
			}
			
			@Override
			public String visitFunctionDeclarationConditional(Conditional x) {
				return visitAnyFunctionDeclaration(x);
			}
			
			@Override
			public String visitFunctionDeclarationExpression(Expression x) {
				return visitAnyFunctionDeclaration(x);
			}
			
			@Override
			public String visitVariableInitialized(Initialized x) {
				return visitAnyVariable(x);
			}
			
			@Override
			public String visitVariableUnInitialized(UnInitialized x) {
				return visitAnyVariable(x);
			}
			
			private String visitAnyVariable(org.rascalmpl.ast.Variable x) {
				return Names.name(x.getName());
			}
			
			@Override
			public String visitDeclarationAnnotation(Annotation x) {
				return Names.name(x.getName());
			}
			
			@Override
			public String visitDeclarationData(Data x) {
				return Names.fullName(x.getUser().getName());
			}
			
			@Override
			public String visitDeclarationDataAbstract(DataAbstract x) {
				return Names.fullName(x.getUser().getName());
			}
			
			@Override
			public String visitDeclarationAlias(Alias x) {
				return Names.fullName(x.getUser().getName());
			}
			
			@Override
			public String visitVariantNAryConstructor(NAryConstructor x) {
				return Names.name(x.getName());
			}
			
			@Override
			public String visitImportedModuleDefault(
					org.rascalmpl.ast.ImportedModule.Default x) {
				return visitAnyImportedModule(x);
			}

			@Override
			public String visitImportedModuleActuals(Actuals x) {
				return visitAnyImportedModule(x);
			}
			
			@Override
			public String visitImportedModuleActualsRenaming(ActualsRenaming x) {
				return visitAnyImportedModule(x);
			}
			
			@Override
			public String visitImportedModuleRenamings(Renamings x) {
				return visitAnyImportedModule(x);
			}
			
			private String visitAnyImportedModule(org.rascalmpl.ast.ImportedModule x) {
				return Names.fullName(x.getName());
			}
		});
		
		if (result == null) {
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
