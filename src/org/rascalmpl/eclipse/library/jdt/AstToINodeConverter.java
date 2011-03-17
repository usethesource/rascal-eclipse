package org.rascalmpl.eclipse.library.jdt;

import java.util.Iterator;
import java.util.List;

import org.eclipse.imp.pdb.facts.INode;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

@SuppressWarnings({"deprecation", "rawtypes"})
public class AstToINodeConverter extends ASTVisitor {
	private static final String IMPORT = "import";
	private static final String PACKAGE = "package";
	private static final String SUPER = "super";
	private static final String EXTENDS = "extends";
	private static final String CLASS = "class";
	private static final String INTERFACE = "interface";
	private static final String FINALLY = "finally";
	private static final String CATCH_CLAUSES = "catchClauses";
	private static final String STATEMENTS = "statements";
	private static final String ANNOTATIONS = "annotations";
	private static final String MEMBER_VALUE_PAIRS = "memberValuePairs";
	private static final String INDEX = "index";
	private static final String THROWS = "throws";
	private static final String RETURN = "return";
	private static final String VALUE = "value";
	private static final String EXTENDED_OPERANDS = "extendedOperands";
	private static final String ELSE = "else";
	private static final String THEN = "then";
	private static final String UPDATERS = "updaters";
	private static final String INITIALIZER = "initializer";
	private static final String FRAGMENTS = "fragments";
	private static final String ENUM_CONSTANTS = "enumConstants";
	private static final String IMPLEMENTS = "implements";
	private static final String PARAMETER = "parameter";
	private static final String WHILE = "while";
	private static final String ARGUMENTS = "arguments";
	private static final String TYPE = "type";
	private static final String ARRAY_DIMENSIONS = "arrayDimensions";
	private static final String DEFAULT_BLOCK = "defaultBlock";
	private static final String BODY = "body";
	private static final String NAME = "name";
	private static final String MODIFIERS = "modifiers";
	private static final String RIGHTSIDE = "right";
	private static final String LEFTSIDE = "left";
	private static final String TYPE_ARGUMENTS = "typeArguments";

	private INode ownNode;

	private final IValueFactory values;

	public AstToINodeConverter(final IValueFactory values) {
		this.values = values;
	}

	public INode getNode() {
		return ownNode;
	}

	@SuppressWarnings("unused")
	private INode[] toArray(List<INode> nodeList) {
		return nodeList.toArray(new INode[0]);
	}

	private INode parseModifiers(int modifiers) {
		NodeList modifierList = new NodeList();

		if (Modifier.isPublic(modifiers)) {
			modifierList.add(values.node("public"));
		}
		if (Modifier.isProtected(modifiers)) {
			modifierList.add(values.node("protected"));
		}
		if (Modifier.isPrivate(modifiers)) {
			modifierList.add(values.node("private"));
		}
		if (Modifier.isStatic(modifiers)) {
			modifierList.add(values.node("static"));
		}
		if (Modifier.isAbstract(modifiers)) {
			modifierList.add(values.node("abstract"));
		}
		if (Modifier.isFinal(modifiers)) {
			modifierList.add(values.node("final"));
		}
		if (Modifier.isSynchronized(modifiers)) {
			modifierList.add(values.node("synchronized"));
		}
		if (Modifier.isVolatile(modifiers)) {
			modifierList.add(values.node("volatile"));
		}
		if (Modifier.isNative(modifiers)) {
			modifierList.add(values.node("native"));
		}
		if (Modifier.isStrictfp(modifiers)) {
			modifierList.add(values.node("strictfp"));
		}
		if (Modifier.isTransient(modifiers)) {
			modifierList.add(values.node("transient"));
		}

		return values.node(MODIFIERS, modifierList.toArray());
	}

	private INode parseModifiers(List ext) {
		NodeList modifierList = new NodeList();

		for (Iterator it = ext.iterator(); it.hasNext();) {
			ASTNode p = (ASTNode) it.next();
			modifierList.add(convertAstNode(p));
		}

		return values.node(MODIFIERS, modifierList.toArray());
	}

	private INode parseModifiers(BodyDeclaration node) {
		if (node.getAST().apiLevel() == AST.JLS2) {
			return parseModifiers(node.getModifiers());
		} else {
			return parseModifiers(node.modifiers());
		}
	}

	private INode createNode(String label) {
		return values.node(label);
	}

	private INode createNodeWithChildren(String label, NodeList list) {
		return values.node(label, list.toArray());
	}
	
	private INode createNodeWithChild(String label, INode child) {
		return values.node(label, child);
	}

	private INode createNodeAndConvertChild(String label, ASTNode node) {
		return values.node(label, convertAstNode(node));
	}

	private INode convertAstNode(ASTNode node) {
		AstToINodeConverter newConverter = new AstToINodeConverter(values);
		node.accept(newConverter);

		return newConverter.getNode();
	}

	private String getNodeName(ASTNode node) {
		return node.getClass().getSimpleName();
	}

	public boolean visit(AnnotationTypeDeclaration node) {
		NodeList attributes = new NodeList();

		attributes.add(parseModifiers(node.modifiers()));
		attributes.add(createNodeAndConvertChild(NAME, node.getName()));

		NodeList bodyDeclarations = new NodeList();
		for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext();) {
			BodyDeclaration d = (BodyDeclaration) it.next();
			bodyDeclarations.add(convertAstNode(d));
		}
		attributes.add(createNodeWithChildren(BODY, bodyDeclarations));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(AnnotationTypeMemberDeclaration node) {
		NodeList attributes = new NodeList();

		attributes.add(parseModifiers(node.modifiers()));
		attributes.add(createNodeAndConvertChild(TYPE_ARGUMENTS, node.getType()));
		attributes.add(createNodeAndConvertChild(NAME, node.getName()));

		if (node.getDefault() != null) {
			attributes.add(createNodeAndConvertChild(DEFAULT_BLOCK, node.getDefault()));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(AnonymousClassDeclaration node) {
		NodeList bodyDeclarations = new NodeList();

		for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext();) {
			BodyDeclaration b = (BodyDeclaration) it.next();
			bodyDeclarations.add(convertAstNode(b));
		}

		ownNode = createNodeWithChildren(getNodeName(node), bodyDeclarations);
		return false;
	}

	public boolean visit(ArrayAccess node) {
		NodeList attributes = new NodeList();
		
		attributes.add(convertAstNode(node.getArray()));
		attributes.add(createNodeAndConvertChild(INDEX, node.getIndex()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(ArrayCreation node) {
		NodeList attributes = new NodeList();

		attributes.add(createNodeAndConvertChild(TYPE, node.getType().getElementType()));

		NodeList dimensions = new NodeList();
		for (Iterator it = node.dimensions().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			attributes.add(convertAstNode(e));
		}
		attributes.add(createNodeWithChildren(ARRAY_DIMENSIONS, dimensions));

		if (node.getInitializer() != null) {
			attributes.add(createNodeAndConvertChild(INITIALIZER, node.getInitializer()));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(ArrayInitializer node) {
		NodeList attributes = new NodeList();
		
		for (Iterator it = node.expressions().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			attributes.add(convertAstNode(e));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(ArrayType node) {
		ownNode = createNodeAndConvertChild(getNodeName(node), node.getComponentType());
		return false;
	}

	public boolean visit(AssertStatement node) {
		NodeList attributes = new NodeList();

		attributes.add(convertAstNode(node.getExpression()));

		if (node.getMessage() != null) {
			attributes.add(convertAstNode(node.getMessage()));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(Assignment node) {
		NodeList attributes = new NodeList();
		
		attributes.add(createNodeAndConvertChild(LEFTSIDE, node.getLeftHandSide()));
		attributes.add(createNodeAndConvertChild(RIGHTSIDE, node.getRightHandSide()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(Block node) {
		NodeList attributes = new NodeList();

		for (Iterator it = node.statements().iterator(); it.hasNext();) {
			Statement s = (Statement) it.next();
			attributes.add(convertAstNode(s));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(BlockComment node) {
		ownNode = createNode(getNodeName(node));
		return false;
	}

	public boolean visit(BooleanLiteral node) {
		NodeList attributes = new NodeList();
		
		if (node.booleanValue() == true) {
			attributes.add(createNode("true"));
		} else {
			attributes.add(createNode("false"));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(BreakStatement node) {
		NodeList attributes = new NodeList();
		
		if (node.getLabel() != null) {
			attributes.add(convertAstNode(node.getLabel()));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(CastExpression node) {
		NodeList attributes = new NodeList();
		
		attributes.add(convertAstNode(node.getType()));
		attributes.add(convertAstNode(node.getExpression()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(CatchClause node) {
		NodeList attributes = new NodeList();
		
		attributes.add(convertAstNode(node.getException()));
		attributes.add(convertAstNode(node.getBody()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(CharacterLiteral node) {
		NodeList attributes = new NodeList();
		
		attributes.add(createNode(node.getEscapedValue()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(ClassInstanceCreation node) {
		NodeList attributes = new NodeList();

		if (node.getExpression() != null) {
			attributes.add(convertAstNode(node.getExpression()));
		}

		if (node.getAST().apiLevel() == AST.JLS2) {
			attributes.add(convertAstNode(node.getName()));
		} 
		else if (node.getAST().apiLevel() >= AST.JLS3) {

			if (!node.typeArguments().isEmpty()) {
				NodeList types = new NodeList();
				for (Iterator it = node.typeArguments().iterator(); it.hasNext();) {
					Type t = (Type) it.next();
					types.add(convertAstNode(t));
				}

				attributes.add(createNodeWithChildren(TYPE_ARGUMENTS, types));
			}

			attributes.add(convertAstNode(node.getType()));
		}

		for (Iterator it = node.arguments().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			attributes.add(convertAstNode(e));
		}

		if (node.getAnonymousClassDeclaration() != null) {
			attributes.add(convertAstNode(node.getAnonymousClassDeclaration()));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(CompilationUnit node) {
		NodeList attributes = new NodeList();

		if (node.getPackage() != null) {
			attributes.add(createNodeAndConvertChild(PACKAGE, node.getPackage()));
		}

		for (Iterator it = node.imports().iterator(); it.hasNext();) {
			ImportDeclaration d = (ImportDeclaration) it.next();
			attributes.add(createNodeAndConvertChild(IMPORT, d));
		}

		for (Iterator it = node.types().iterator(); it.hasNext();) {
			AbstractTypeDeclaration d = (AbstractTypeDeclaration) it.next();
			attributes.add(createNodeAndConvertChild(TYPE, d));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(ConditionalExpression node) {
		NodeList attributes = new NodeList();
		
		attributes.add(convertAstNode(node.getExpression()));
		attributes.add(createNodeAndConvertChild(THEN, node.getThenExpression()));
		attributes.add(createNodeAndConvertChild(ELSE, node.getElseExpression()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(ConstructorInvocation node) {
		NodeList attributes = new NodeList();

		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeArguments().isEmpty()) {
				NodeList types = new NodeList();

				for (Iterator it = node.typeArguments().iterator(); it.hasNext();) {
					Type t = (Type) it.next();
					types.add(convertAstNode(t));
				}

				attributes.add(createNodeWithChildren(TYPE_ARGUMENTS, types));
			}
		}

		NodeList arguments = new NodeList();
		for (Iterator it = node.arguments().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			arguments.add(convertAstNode(e));
		}
		attributes.add(createNodeWithChildren(ARGUMENTS, arguments));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(ContinueStatement node) {
		NodeList attributes = new NodeList();
		
		if (node.getLabel() != null) {
			attributes.add(convertAstNode(node.getLabel()));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(DoStatement node) {
		NodeList attributes = new NodeList();
		
		attributes.add(createNodeAndConvertChild(BODY, node.getBody()));
		attributes.add(createNodeAndConvertChild(WHILE, node.getExpression()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(EmptyStatement node) {
		ownNode = createNode(getNodeName(node));
		return false;
	}

	public boolean visit(EnhancedForStatement node) {
		NodeList attributes = new NodeList();
		
		attributes.add(createNodeAndConvertChild(PARAMETER, node.getParameter()));
		attributes.add(convertAstNode(node.getExpression()));
		attributes.add(createNodeAndConvertChild(BODY, node.getBody()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(EnumConstantDeclaration node) {
		NodeList attributes = new NodeList();

		attributes.add(parseModifiers(node.modifiers()));
		attributes.add(createNodeAndConvertChild(NAME, node.getName()));

		NodeList arguments = new NodeList();
		if (!node.arguments().isEmpty()) {
			for (Iterator it = node.arguments().iterator(); it.hasNext();) {
				Expression e = (Expression) it.next();
				arguments.add(convertAstNode(e));
			}

			attributes.add(createNodeWithChildren(ARGUMENTS, arguments));
		}

		if (node.getAnonymousClassDeclaration() != null) {
			attributes.add(convertAstNode(node.getAnonymousClassDeclaration()));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(EnumDeclaration node) {
		NodeList attributes = new NodeList();

		attributes.add(parseModifiers(node.modifiers()));
		attributes.add(createNodeAndConvertChild(NAME, node.getName()));

		if (!node.superInterfaceTypes().isEmpty()) {
			NodeList implementedInterfaces = new NodeList();

			for (Iterator it = node.superInterfaceTypes().iterator(); it.hasNext();) {
				Type t = (Type) it.next();
				implementedInterfaces.add(convertAstNode(t));
			}
			attributes.add(createNodeWithChildren(IMPLEMENTS, implementedInterfaces));
		}

		NodeList enumConstants = new NodeList();
		for (Iterator it = node.enumConstants().iterator(); it.hasNext();) {
			EnumConstantDeclaration d = (EnumConstantDeclaration) it.next();
			enumConstants.add(convertAstNode(d));
		}
		attributes.add(createNodeWithChildren(ENUM_CONSTANTS, enumConstants));

		if (!node.bodyDeclarations().isEmpty()) {
			NodeList body = new NodeList();
			
			for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext();) {
				BodyDeclaration d = (BodyDeclaration) it.next();
				body.add(convertAstNode(d));
			}
			attributes.add(createNodeWithChildren(BODY, body));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(ExpressionStatement node) {
		ownNode = createNodeAndConvertChild(getNodeName(node), node.getExpression());
		return false;
	}

	public boolean visit(FieldAccess node) {
		NodeList attributes = new NodeList();
		
		attributes.add(convertAstNode(node.getExpression()));
		attributes.add(createNodeAndConvertChild(NAME, node.getName()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(FieldDeclaration node) {
		NodeList attributes = new NodeList();

		attributes.add(parseModifiers(node));
		attributes.add(createNodeAndConvertChild(TYPE, node.getType()));

		NodeList fragments = new NodeList();
		for (Iterator it = node.fragments().iterator(); it.hasNext();) {
			VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
			fragments.add(convertAstNode(f));
		}
		attributes.add(createNodeWithChildren(FRAGMENTS, fragments));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(ForStatement node) {
		NodeList attributes = new NodeList();

		NodeList initializers = new NodeList();
		for (Iterator it = node.initializers().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			initializers.add(convertAstNode(e));
		}
		attributes.add(createNodeWithChildren(INITIALIZER, initializers));

		if (node.getExpression() != null) {
			attributes.add(convertAstNode(node.getExpression()));
		}

		NodeList updaters = new NodeList();
		for (Iterator it = node.updaters().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			updaters.add(convertAstNode(e));
		}
		attributes.add(createNodeWithChildren(UPDATERS, updaters));

		attributes.add(createNodeAndConvertChild(BODY, node.getBody()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(IfStatement node) {
		NodeList attributes = new NodeList();
		
		attributes.add(convertAstNode(node.getExpression()));
		attributes.add(createNodeAndConvertChild(THEN, node.getThenStatement()));

		if (node.getElseStatement() != null) {
			attributes.add(createNodeAndConvertChild(ELSE, node.getElseStatement()));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(ImportDeclaration node) {
		NodeList attributes = new NodeList();

		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (node.isStatic()) {
				attributes.add(createNode("static"));
			}
		}

		attributes.add(createNodeAndConvertChild(NAME, node.getName()));

		if (node.isOnDemand()) {
			attributes.add(createNode("onDemand"));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(InfixExpression node) {
		NodeList attributes = new NodeList();
		attributes.add(createNode(node.getOperator().toString()));
		attributes.add(createNodeAndConvertChild(LEFTSIDE, node.getLeftOperand()));
		attributes.add(createNodeAndConvertChild(RIGHTSIDE, node.getRightOperand()));

		if (node.hasExtendedOperands()) {
			NodeList extendedOperands = new NodeList();
			for (Iterator it = node.extendedOperands().iterator(); it.hasNext();) {
				Expression e = (Expression) it.next();
				extendedOperands.add(convertAstNode(e));
			}

			attributes.add(createNodeWithChildren(EXTENDED_OPERANDS, extendedOperands));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(Initializer node) {
		NodeList attributes = new NodeList();
		
		attributes.add(parseModifiers(node));
		attributes.add(createNodeAndConvertChild(BODY, node.getBody()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(InstanceofExpression node) {
		NodeList attributes = new NodeList();
		
		attributes.add(createNodeAndConvertChild(LEFTSIDE, node.getLeftOperand()));
		attributes.add(createNodeAndConvertChild(RIGHTSIDE, node.getRightOperand()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(LabeledStatement node) {
		NodeList attributes = new NodeList();
		
		attributes.add(convertAstNode(node.getLabel()));
		attributes.add(createNodeAndConvertChild(BODY, node.getBody()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(LineComment node) {
		return false;
	}

	public boolean visit(MarkerAnnotation node) {
		ownNode = createNodeAndConvertChild(getNodeName(node), node.getTypeName());
		return false;
	}

	public boolean visit(MemberRef node) {
		return false;
	}

	public boolean visit(MemberValuePair node) {
		NodeList attributes = new NodeList();
		
		attributes.add(createNodeAndConvertChild(NAME, node.getName()));
		attributes.add(createNodeAndConvertChild(VALUE, node.getValue()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(MethodDeclaration node) {
		NodeList attributes = new NodeList();

		attributes.add(parseModifiers(node));

		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeParameters().isEmpty()) {
				NodeList typeParameters = new NodeList();

				for (Iterator it = node.typeParameters().iterator(); it.hasNext();) {
					TypeParameter t = (TypeParameter) it.next();
					typeParameters.add(convertAstNode(t));
				}
				attributes.add(createNodeWithChildren(TYPE_ARGUMENTS, typeParameters));
			}
		}

		if (!node.isConstructor()) {
			if (node.getAST().apiLevel() == AST.JLS2) {
				attributes.add(createNodeAndConvertChild(RETURN, node.getReturnType()));
			} else if (node.getReturnType2() != null) {
				attributes.add(createNodeAndConvertChild(RETURN, node.getReturnType2()));
			} else {
				// methods really ought to have a return type
				attributes.add(createNodeWithChild(RETURN, createNode("void")));
			}
		}

		attributes.add(createNodeAndConvertChild(NAME, node.getName()));

		NodeList parameters = new NodeList();
		for (Iterator it = node.parameters().iterator(); it.hasNext();) {
			SingleVariableDeclaration v = (SingleVariableDeclaration) it.next();
			parameters.add(convertAstNode(v));
		}
		attributes.add(createNodeWithChildren(PARAMETER, parameters));

		/*
		 * for (int i = 0; i < node.getExtraDimensions(); i++) {
		 * //this.buffer.append("[]"); //$NON-NLS-1$ // TODO: Do these need to be included in the node tree? 
		 * }
		 */

		if (!node.thrownExceptions().isEmpty()) {
			NodeList thrownExceptions = new NodeList();

			for (Iterator it = node.thrownExceptions().iterator(); it.hasNext();) {
				Name n = (Name) it.next();
				thrownExceptions.add(convertAstNode(n));
			}
			attributes.add(createNodeWithChildren(THROWS, thrownExceptions));
		}

		if (node.getBody() != null) {
			attributes.add(createNodeAndConvertChild(BODY, node.getBody()));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(MethodInvocation node) {
		NodeList attributes = new NodeList();

		if (node.getExpression() != null) {
			attributes.add(convertAstNode(node.getExpression()));
		}

		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeArguments().isEmpty()) {

				NodeList typeArguments = new NodeList();
				for (Iterator it = node.typeArguments().iterator(); it.hasNext();) {
					Type t = (Type) it.next();
					typeArguments.add(convertAstNode(t));
				}
				attributes.add(createNodeWithChildren(TYPE_ARGUMENTS, typeArguments));
			}
		}

		attributes.add(createNodeAndConvertChild(NAME, node.getName()));

		NodeList arguments = new NodeList();
		for (Iterator it = node.arguments().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			arguments.add(convertAstNode(e));
		}
		attributes.add(createNodeWithChildren(ARGUMENTS, arguments));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(MethodRef node) {
		return false;
	}

	public boolean visit(MethodRefParameter node) {
		return false;
	}

	public boolean visit(Modifier node) {
		ownNode = createNode(node.getKeyword().toString());
		return false;
	}

	public boolean visit(NormalAnnotation node) {
		NodeList attributes = new NodeList();

		attributes.add(createNodeAndConvertChild(NAME, node.getTypeName()));

		NodeList memberValuePairs = new NodeList();
		for (Iterator it = node.values().iterator(); it.hasNext();) {
			MemberValuePair p = (MemberValuePair) it.next();
			memberValuePairs.add(convertAstNode(p));
		}
		attributes.add(createNodeWithChildren(MEMBER_VALUE_PAIRS, memberValuePairs));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(NullLiteral node) {
		ownNode = createNode(getNodeName(node));
		return false;
	}

	public boolean visit(NumberLiteral node) {
		NodeList attributes = new NodeList();
		attributes.add(createNode(node.getToken()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(PackageDeclaration node) {
		NodeList attributes = new NodeList();

		if (node.getAST().apiLevel() >= AST.JLS3) {
			NodeList annotations = new NodeList();
			for (Iterator it = node.annotations().iterator(); it.hasNext();) {
				Annotation p = (Annotation) it.next();
				annotations.add(convertAstNode(p));
			}
			attributes.add(createNodeWithChildren(ANNOTATIONS, annotations));
		}

		attributes.add(convertAstNode(node.getName()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(ParameterizedType node) {
		NodeList attributes = new NodeList();
		attributes.add(createNodeAndConvertChild(TYPE, node.getType()));

		NodeList typeArguments = new NodeList();
		for (Iterator it = node.typeArguments().iterator(); it.hasNext();) {
			Type t = (Type) it.next();
			typeArguments.add(convertAstNode(t));
		}
		attributes.add(createNodeWithChildren(TYPE_ARGUMENTS, typeArguments));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(ParenthesizedExpression node) {
		ownNode = createNodeAndConvertChild(getNodeName(node), node.getExpression());
		return false;
	}

	public boolean visit(PostfixExpression node) {
		NodeList attributes = new NodeList();
		
		attributes.add(createNodeAndConvertChild(LEFTSIDE, node.getOperand()));
		attributes.add(createNode(node.getOperator().toString()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(PrefixExpression node) {
		NodeList attributes = new NodeList();
		
		attributes.add(createNode(node.getOperator().toString()));
		attributes.add(createNodeAndConvertChild(RIGHTSIDE, node.getOperand()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(PrimitiveType node) {
		NodeList attributes = new NodeList();
		attributes.add(createNode(node.getPrimitiveTypeCode().toString()));		
				
		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(QualifiedName node) {
		NodeList attributes = new NodeList();
		
		attributes.add(convertAstNode(node.getQualifier()));
		attributes.add(convertAstNode(node.getName()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(QualifiedType node) {
		NodeList attributes = new NodeList();
		
		attributes.add(convertAstNode(node.getQualifier()));
		attributes.add(convertAstNode(node.getName()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(ReturnStatement node) {
		NodeList attributes = new NodeList();
		
		if (node.getExpression() != null) {
			attributes.add(convertAstNode(node.getExpression()));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(SimpleName node) {
		ownNode = createNode("'" + node.getIdentifier() + "'");
		return false;
	}

	public boolean visit(SimpleType node) {
		return true;
	}

	public boolean visit(SingleMemberAnnotation node) {
		NodeList attributes = new NodeList();
		
		attributes.add(createNodeAndConvertChild(NAME, node.getTypeName()));
		attributes.add(createNodeAndConvertChild(VALUE, node.getValue()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(SingleVariableDeclaration node) {
		NodeList attributes = new NodeList();

		if (node.getAST().apiLevel() == AST.JLS2) {
			attributes.add(parseModifiers(node.getModifiers()));
		} else if (node.getAST().apiLevel() >= AST.JLS3) {
			attributes.add(parseModifiers(node.modifiers()));
		}

		attributes.add(createNodeAndConvertChild(TYPE, node.getType()));

		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (node.isVarargs()) {
				attributes.add(createNode("vararg"));
			}
		}

		attributes.add(createNodeAndConvertChild(NAME, node.getName()));

		/*
		 * for (int i = 0; i < node.getExtraDimensions(); i++) { //TODO: What to
		 * do with the extra dimensions... this.buffer.append("[]");
		 * //$NON-NLS-1$ }
		 */

		if (node.getInitializer() != null) {
			attributes.add(createNodeAndConvertChild(INITIALIZER, node.getInitializer()));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(StringLiteral node) {
		NodeList attributes = new NodeList();
		attributes.add(createNode(node.getEscapedValue()));
		
		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(SuperConstructorInvocation node) {
		NodeList attributes = new NodeList();

		if (node.getExpression() != null) {
			attributes.add(convertAstNode(node.getExpression()));
		}

		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeArguments().isEmpty()) {

				NodeList typeArguments = new NodeList();
				for (Iterator it = node.typeArguments().iterator(); it.hasNext();) {
					Type t = (Type) it.next();
					typeArguments.add(convertAstNode(t));
				}
				attributes.add(createNodeWithChildren(TYPE_ARGUMENTS, typeArguments));
			}
		}

		NodeList arguments = new NodeList();
		for (Iterator it = node.arguments().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			arguments.add(convertAstNode(e));
		}
		attributes.add(createNodeWithChildren(ARGUMENTS, arguments));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(SuperFieldAccess node) {
		NodeList attributes = new NodeList();
		
		if (node.getQualifier() != null) {
			attributes.add(convertAstNode(node.getQualifier()));
		}
		attributes.add(createNodeAndConvertChild(NAME, node.getName()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(SuperMethodInvocation node) {
		NodeList attributes = new NodeList();

		if (node.getQualifier() != null) {
			attributes.add(convertAstNode(node.getQualifier()));
		}

		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeArguments().isEmpty()) {

				NodeList typeArguments = new NodeList();
				for (Iterator it = node.typeArguments().iterator(); it.hasNext();) {
					Type t = (Type) it.next();
					typeArguments.add(convertAstNode(t));
				}
				attributes.add(createNodeWithChildren(TYPE_ARGUMENTS, typeArguments));
			}
		}

		attributes.add(createNodeAndConvertChild(NAME, node.getName()));

		NodeList arguments = new NodeList();
		for (Iterator it = node.arguments().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			arguments.add(convertAstNode(e));
		}
		attributes.add(createNodeWithChildren(ARGUMENTS, arguments));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(SwitchCase node) {
		NodeList attributes = new NodeList();
		if (node.isDefault()) {
			attributes.add(createNode("default"));
		} else {
			attributes.add(convertAstNode(node.getExpression()));
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(SwitchStatement node) {
		NodeList attributes = new NodeList();
		
		attributes.add(convertAstNode(node.getExpression()));

		NodeList statements = new NodeList();
		for (Iterator it = node.statements().iterator(); it.hasNext();) {
			Statement s = (Statement) it.next();
			statements.add(convertAstNode(s));
		}
		attributes.add(createNodeWithChildren(STATEMENTS, statements));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(SynchronizedStatement node) {
		NodeList attributes = new NodeList();
		
		attributes.add(convertAstNode(node.getExpression()));
		attributes.add(createNodeAndConvertChild(BODY, node.getBody()));
		
		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(TagElement node) {
		// TODO: What to do with JavaDoc?
		return false;
	}

	public boolean visit(TextElement node) {
		// TODO: What to do with JavaDoc?
		return false;
	}

	public boolean visit(ThisExpression node) {
		NodeList attributes = new NodeList();
		
		if (node.getQualifier() != null) {
			attributes.add(convertAstNode(node.getQualifier()));
		}
		
		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(ThrowStatement node) {
		NodeList attributes = new NodeList();
		attributes.add(convertAstNode(node.getExpression()));
		
		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(TryStatement node) {
		NodeList attributes = new NodeList();
		
		attributes.add(createNodeAndConvertChild(BODY, node.getBody()));

		NodeList catchClauses = new NodeList();
		for (Iterator it = node.catchClauses().iterator(); it.hasNext();) {
			CatchClause cc = (CatchClause) it.next();
			catchClauses.add(convertAstNode(cc));
		}
		attributes.add(createNodeWithChildren(CATCH_CLAUSES, catchClauses));
		
		if (node.getFinally() != null) {
			attributes.add(createNodeAndConvertChild(FINALLY, node.getFinally()));
		}
		
		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(TypeDeclaration node) {
		NodeList attributes = new NodeList();
		
		attributes.add(parseModifiers(node));

		if (node.isInterface()) {
			attributes.add(createNode(INTERFACE));
		} else {
			attributes.add(createNode(CLASS));
		}

		attributes.add(createNodeAndConvertChild(NAME, node.getName()));
		
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeParameters().isEmpty()) {
				
				NodeList typeParameters = new NodeList();
				for (Iterator it = node.typeParameters().iterator(); it.hasNext();) {
					TypeParameter t = (TypeParameter) it.next();
					typeParameters.add(convertAstNode(t));					
				}
				attributes.add(createNodeWithChildren(TYPE_ARGUMENTS, typeParameters));
			}
		}
		
		if (node.getAST().apiLevel() == AST.JLS2) {
			if (node.getSuperclass() != null) {
				attributes.add(createNodeAndConvertChild(EXTENDS, node.getSuperclass()));
			}
			if (!node.superInterfaces().isEmpty()) {

				NodeList interfaces = new NodeList();
				for (Iterator it = node.superInterfaces().iterator(); it.hasNext();) {
					Name n = (Name) it.next();
					interfaces.add(convertAstNode(n));
				}
				attributes.add(createNodeWithChildren(IMPLEMENTS, interfaces));
			}
		} else if (node.getAST().apiLevel() >= AST.JLS3) {
			if (node.getSuperclassType() != null) {
				attributes.add(createNodeAndConvertChild(EXTENDS, node.getSuperclassType()));
			}
			if (!node.superInterfaceTypes().isEmpty()) {

				NodeList interfaces = new NodeList();
				for (Iterator it = node.superInterfaceTypes().iterator(); it.hasNext();) {
					Type t = (Type) it.next();
					interfaces.add(convertAstNode(t));
				}
				attributes.add(createNodeWithChildren(IMPLEMENTS, interfaces));
			}
		}

		NodeList bodies = new NodeList();
		for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext();) {
			BodyDeclaration d = (BodyDeclaration) it.next();
			bodies.add(convertAstNode(d));
		}
		attributes.add(createNodeWithChildren(BODY, bodies));
		
		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(TypeDeclarationStatement node) {
		if (node.getAST().apiLevel() == AST.JLS2) {
			ownNode = convertAstNode(node.getTypeDeclaration());
		}
		if (node.getAST().apiLevel() >= AST.JLS3) {
			ownNode = convertAstNode(node.getDeclaration());
		}
		
		return false;
	}

	public boolean visit(TypeLiteral node) {
		NodeList attributes = new NodeList();
		
		attributes.add(convertAstNode(node.getType()));

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(TypeParameter node) {
		NodeList attributes = new NodeList();
		
		attributes.add(createNodeAndConvertChild(NAME, node.getName()));
		
		if (!node.typeBounds().isEmpty()) {

			NodeList extentionList = new NodeList();
			for (Iterator it = node.typeBounds().iterator(); it.hasNext();) {
				Type t = (Type) it.next();
				extentionList.add(convertAstNode(t));
			}
			attributes.add(createNodeWithChildren(EXTENDS, extentionList));
		}
		
		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(VariableDeclarationExpression node) {
		NodeList attributes = new NodeList();
		
		if (node.getAST().apiLevel() == AST.JLS2) {
			attributes.add(parseModifiers(node.getModifiers()));
		}
		if (node.getAST().apiLevel() >= AST.JLS3) {
			attributes.add(parseModifiers(node.modifiers()));
		}
		
		attributes.add(createNodeAndConvertChild(TYPE, node.getType()));
		
		NodeList fragments = new NodeList();
		for (Iterator it = node.fragments().iterator(); it.hasNext();) {
			VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
			fragments.add(convertAstNode(f));
		}
		attributes.add(createNodeWithChildren(FRAGMENTS, fragments));
		
		return false;
	}

	public boolean visit(VariableDeclarationFragment node) {
		NodeList attributes = new NodeList();
		
		attributes.add(createNodeAndConvertChild(NAME, node.getName()));
		
		//TODO: Extra dimensions?
		/*for (int i = 0; i < node.getExtraDimensions(); i++) {
			this.buffer.append("[]");//$NON-NLS-1$
		}*/
		
		if (node.getInitializer() != null) {
			attributes.add(createNodeAndConvertChild(INITIALIZER, node.getInitializer()));
		}
		
		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(VariableDeclarationStatement node) {
		NodeList attributes = new NodeList();
		
		if (node.getAST().apiLevel() == AST.JLS2) {
			attributes.add(parseModifiers(node.getModifiers()));
		}
		if (node.getAST().apiLevel() >= AST.JLS3) {
			attributes.add(parseModifiers(node.modifiers()));
		}
		
		attributes.add(createNodeAndConvertChild(TYPE, node.getType()));

		NodeList fragments = new NodeList();
		for (Iterator it = node.fragments().iterator(); it.hasNext();) {
			VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
			fragments.add(convertAstNode(f));
		}
		attributes.add(createNodeWithChildren(FRAGMENTS, fragments));
		
		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(WhileStatement node) {
		NodeList attributes = new NodeList();
		
		attributes.add(convertAstNode(node.getExpression()));
		attributes.add(createNodeAndConvertChild(BODY, node.getBody()));
		
		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

	public boolean visit(WildcardType node) {
		NodeList attributes = new NodeList();
		
		Type bound = node.getBound();
		if (bound != null) {
			attributes.add(createNodeAndConvertChild(TYPE, bound));
			
			if (node.isUpperBound()) {
				attributes.add(createNode(EXTENDS));
			} else {
				attributes.add(createNode(SUPER));
			}
		}

		ownNode = createNodeWithChildren(getNodeName(node), attributes);
		return false;
	}

}
