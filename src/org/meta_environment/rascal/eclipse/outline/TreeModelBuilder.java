package org.meta_environment.rascal.eclipse.outline;

import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.services.base.TreeModelBuilderBase;
import org.meta_environment.rascal.ast.ASTFactory;
import org.meta_environment.rascal.ast.Module;
import org.meta_environment.rascal.ast.NullASTVisitor;
import org.meta_environment.rascal.ast.Toplevel;
import org.meta_environment.rascal.ast.Declaration.Alias;
import org.meta_environment.rascal.ast.Declaration.Annotation;
import org.meta_environment.rascal.ast.Declaration.Data;
import org.meta_environment.rascal.ast.Declaration.Function;
import org.meta_environment.rascal.ast.Declaration.Rule;
import org.meta_environment.rascal.ast.Declaration.Tag;
import org.meta_environment.rascal.ast.Declaration.Variable;
import org.meta_environment.rascal.ast.Declaration.View;
import org.meta_environment.rascal.ast.Module.Default;
import org.meta_environment.rascal.ast.Toplevel.DefaultVisibility;
import org.meta_environment.rascal.ast.Toplevel.GivenVisibility;
import org.meta_environment.rascal.parser.ASTBuilder;

public class TreeModelBuilder extends TreeModelBuilderBase {
	public static final int CATEGORY_ALIAS = 1;
	public static final int CATEGORY_DATA = 2;
	public static final int CATEGORY_ANNOTATION = 3;
	public static final int CATEGORY_FUNCTION = 4;
	public static final int CATEGORY_RULE = 5;
	public static final int CATEGORY_TAG = 6;
	public static final int CATEGORY_VARIABLE = 7;
	public static final int CATEGORY_VIEW = 8;

	@Override
	protected void visitTree(Object root) {
		if (root == null) {
			return;
		}
		ASTBuilder builder = new ASTBuilder(new ASTFactory());
		
		Module mod = builder.buildModule((IConstructor) root);
		
		createTopItem(mod.getTree());
		mod.accept(new Visitor());
	}
	
	private class Visitor extends NullASTVisitor<ModelTreeNode> {

		@Override
		public ModelTreeNode visitModuleDefault(Default x) {
			pushSubItem(x.getHeader(), 0);
			for (Toplevel t : x.getBody().getToplevels()) {
				t.accept(this);
			}
			popSubItem();
			return null;
		}
		
		@Override
		public ModelTreeNode visitToplevelDefaultVisibility(DefaultVisibility x) {
			return x.getDeclaration().accept(this);
		}
		
		@Override
		public ModelTreeNode visitToplevelGivenVisibility(GivenVisibility x) {
			return x.getDeclaration().accept(this);
		}
		
		@Override
		public ModelTreeNode visitDeclarationAlias(Alias x) {
			return createSubItem(x,  CATEGORY_ALIAS);
		}
		
		@Override
		public ModelTreeNode visitDeclarationData(Data x) {
			return createSubItem(x, CATEGORY_DATA);
		}
		
		@Override
		public ModelTreeNode visitDeclarationAnnotation(Annotation x) {
			return createSubItem(x,  CATEGORY_ANNOTATION);
		}
		
		@Override
		public ModelTreeNode visitDeclarationFunction(Function x) {
			return createSubItem(x,  CATEGORY_FUNCTION);
		}
		
		@Override
		public ModelTreeNode visitDeclarationRule(Rule x) {
			return createSubItem(x,  CATEGORY_RULE);
		}
		
		@Override
		public ModelTreeNode visitDeclarationTag(Tag x) {
			return createSubItem(x,  CATEGORY_TAG);
		}
		
		@Override
		public ModelTreeNode visitDeclarationVariable(Variable x) {
			return createSubItem(x,  CATEGORY_VARIABLE);
		}
		
		@Override
		public ModelTreeNode visitDeclarationView(View x) {
			return createSubItem(x,  CATEGORY_VIEW);
		}
	}

}
