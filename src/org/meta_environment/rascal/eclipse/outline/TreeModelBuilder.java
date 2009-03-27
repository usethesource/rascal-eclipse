package org.meta_environment.rascal.eclipse.outline;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.services.base.TreeModelBuilderBase;
import org.meta_environment.rascal.ast.ASTFactory;
import org.meta_environment.rascal.ast.AbstractAST;
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
	
	private Group<AbstractAST> functions;
	private Group<AbstractAST> variables;
	private Group<AbstractAST> aliases;
	private Group<Group<AbstractAST>> adts;
	private Group<AbstractAST> annos;
	private Group<AbstractAST> tags;
	private Group<AbstractAST> views;
	private Group<AbstractAST> rules;
	
	private String module;
	private ISourceLocation loc;

	@Override
	protected void visitTree(Object root) {
		if (root == null) {
			return;
		}
		ASTBuilder builder = new ASTBuilder(new ASTFactory());
		
		Module mod = builder.buildModule((IConstructor) root);

		loc = mod.getLocation();
		
		functions = new Group<AbstractAST>("Functions", loc);
		variables = new Group<AbstractAST>("Variables",loc);
		aliases = new Group<AbstractAST>("Aliases",loc);
		adts = new Group<Group<AbstractAST>>("Types",loc);
		annos = new Group<AbstractAST>("Annotations",loc);
		tags = new Group<AbstractAST>("Tags",loc);
		views = new Group<AbstractAST>("Views",loc);
		rules = new Group<AbstractAST>("Rules",loc);

		mod.accept(new Visitor());
		
		createTopItem(module);
		addGroup(variables);
		addGroup(functions);
		addGroups(adts);
		addGroup(rules);
		addGroup(annos);
		addGroup(tags);
		addGroup(views);
	}

	private <T> void addGroup(Group<T> group) {
		pushSubItem(group);
		for (T t : group) {
			createSubItem(t);
		}
		popSubItem();
	}
	
	private <T> void addGroups(Group<Group<T>> nested) {
		pushSubItem(nested);
		
		for (Group<T> group : nested) {
			addGroup(group);
		}
		
		popSubItem();
	}
	
	private Group<AbstractAST> findGroup(Group<Group<AbstractAST>> nested,
			String string) {
		for (Group<AbstractAST> group : nested) {
			if (group.getName().equals(string)) {
				return group;
			}
		}
		
		return new Group<AbstractAST>(string, loc);
	}
	
	private class Visitor extends NullASTVisitor<AbstractAST> {

		@Override
		public AbstractAST visitModuleDefault(Default x) {
			module = x.getHeader().toString();
			for (Toplevel t : x.getBody().getToplevels()) {
				t.accept(this);
			}
			return x;
		}
		
		@Override
		public AbstractAST visitToplevelDefaultVisibility(DefaultVisibility x) {
			return x.getDeclaration().accept(this);
		}
		
		@Override
		public AbstractAST visitToplevelGivenVisibility(GivenVisibility x) {
			return x.getDeclaration().accept(this);
		}
		
		@Override
		public AbstractAST visitDeclarationAlias(Alias x) {
			return aliases.add(x);
		}
		
		@Override
		public AbstractAST visitDeclarationData(Data x) {
			Group<AbstractAST> adt = findGroup(adts, x.getUser().toString());
			return adt.add(x);
		}
		
		@Override
		public AbstractAST visitDeclarationAnnotation(Annotation x) {
			return annos.add(x);
		}
		
		@Override
		public AbstractAST visitDeclarationFunction(Function x) {
			return functions.add(x);
		}
		
		@Override
		public AbstractAST visitDeclarationRule(Rule x) {
			return rules.add(x);
		}
		
		@Override
		public AbstractAST visitDeclarationTag(Tag x) {
			return tags.add(x);
		}
		
		@Override
		public AbstractAST visitDeclarationVariable(Variable x) {
			for (org.meta_environment.rascal.ast.Variable v : x.getVariables()) {
				variables.add(v);
			}
			return x;
		}
		
		@Override
		public AbstractAST visitDeclarationView(View x) {
			return views.add(x);
		}
	}

	static public class Group<T> implements Iterable<T> {
		private final String name;
		private final List<T> contents = new LinkedList<T>();
		private final ISourceLocation loc;
		
		public Group(String name, ISourceLocation loc) {
			this.name = name;
			this.loc = loc;
		}
		
		public String getName() {
			return name;
		}
		
		public T add(T node) {
			contents.add(node);
			return node;
		}
		
		@Override
		public Iterator<T> iterator() {
			return contents.iterator();
		}

		public ISourceLocation getLocation() {
			return loc;
		}
	}
}
