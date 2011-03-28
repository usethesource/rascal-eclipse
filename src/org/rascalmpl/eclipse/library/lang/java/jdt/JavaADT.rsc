module lang::java::jdt::JavaADT

import lang::java::jdt::Java;

data AstNode	= compilationUnit(AstNode package, list[AstNode] imports, list[AstNode] typeDeclarations)
				
				// Declarations
				| anonymousClassDeclaration(list[AstNode] bodyDeclarations)
				| annotationTypeDeclaration(list[Modifier] modifiers, str name, list[AstNode] bodyDeclarations)
				| annotationTypeMemberDeclaration(list[Modifier] modifiers, AstNode typeArgument, str name, Option[AstNode] defaultBlock)
				| enumDeclaration(list[Modifier] modifiers, str name, list[AstNode] implements, list[AstNode] enumConstants, list[AstNode] bodyDeclarations)
				| enumConstantDeclaration(list[Modifier] modifiers, str name, list[AstNode] arguments, Option[AstNode] anonymousClassDeclaration)
				| typeDeclaration(list[Modifier] modifiers, str objectType, str name, list[TypedAstNode] genericTypes, Option[AstNode] extends, list[AstNode] implements, list[AstNode] bodyDeclarations)
				| fieldDeclaration(list[Modifier] modifiers, TypedAstNode \type, list[AstNode] fragments)
				| initializer(list[Modifier] modifiers, AstNode body)
				| methodDeclaration(list[Modifier] modifiers, list[TypedAstNode] genericTypes, Option[AstNode] returnType, str name, list[AstNode] parameters, list[AstNode] possibleExceptions, Option[AstNode] implementation)
				| importDeclaration(str name, bool staticImport, bool onDemand)
				| packageDeclaration(str name, list[AstNode] annotations)
				| singleVariableDeclaration(str name, list[Modifier] modifiers, TypedAstNode \type, Option[AstNode] initializer, bool isVarargs)
				| variableDeclarationFragment(str name, Option[AstNode] initializer)
				| typeParameter(str name, list[AstNode] extendsList)

				// Expressions
				| markerAnnotation(str typeName)
				| normalAnnotation(str typeName, list[AstNode] memberValuePairs)
				| memberValuePair(str name, AstNode \value)				
				| singleMemberAnnotation(str typeName, AstNode \value)
				| arrayAccess(AstNode array, AstNode index)
				| arrayCreation(TypedAstNode \type, list[AstNode] dimensions, Option[AstNode] initializer)
				| arrayInitializer(list[AstNode] expressions)
				| assignment(AstNode leftSide, AstNode rightSide)
				| booleanLiteral(bool boolValue)
				| castExpression(TypedAstNode \type, AstNode expression)
				| characterLiteral(str charValue)
				| classInstanceCreation(Option[TypedAstNode] optionalExpression, TypedAstNode \type, list[TypedAstNode] genericTypes, list[TypedAstNode] typedArguments, Option[AstNode] anonymousClassDeclaration)
				| conditionalExpression(AstNode expression, AstNode thenBranch, AstNode elseBranch)
				| fieldAccess(AstNode expression, str name)
				| infixExpression(str operator, AstNode leftSide, AstNode rightSide, list[AstNode] extendedOperands)
				| instanceofExpression(AstNode leftSide, AstNode rightSide)
				| methodInvocation(Option[TypedAstNode] optionalExpression, list[TypedAstNode] genericTypes, str name, list[TypedAstNode] typedArguments)
				| superMethodInvocation(Option[str] qualifier, list[TypedAstNode] genericTypes, str name, list[TypedAstNode] typedArguments)
				| qualifiedName(str qualifiedName)
				| simpleName(str simpleName)
				| nullLiteral()
				| numberLiteral(str number)
				| parenthesizedExpression(AstNode expression)
				| postfixExpression(AstNode operand, str operator)
				| prefixExpression(AstNode operand, str operator)
				| stringLiteral(str stringValue)
				| superFieldAccess(Option[str] qualifier, str name)
				| thisExpression(Option[str] qualifierString)
				| typeLiteral(TypedAstNode \type)
				| variableDeclarationExpression(list[Modifier] modifiers, TypedAstNode \type, list[AstNode] fragments)
						
				// Statements
				| assertStatement(AstNode expression, Option[AstNode] message)
				| block(list[AstNode] statements)
				| breakStatement(Option[str] label)
				| constructorInvocation(list[TypedAstNode] genericTypes, list[TypedAstNode] typedArguments)
				| superConstructorInvocation(Option[TypedAstNode] optionalExpression, list[TypedAstNode] genericTypes, list[TypedAstNode] typedArguments)
				| continueStatement(Option[str] label)
				| doStatement(AstNode body, AstNode whileExpression)
				| emptyStatement()
				| enhancedForStatement(AstNode parameter, AstNode collectionExpression, AstNode body)
				| expressionStatement(AstNode expression)
				| forStatement(list[AstNode] initializers, Option[AstNode] booleanExpression, list[AstNode] updaters, AstNode body)
				| ifStatement(AstNode booleanExpression, AstNode thenStatement, Option[AstNode] elseStatement)
				| labeledStatement(str label, AstNode body)
				| returnStatement(Option[TypedAstNode] optionalExpression)
				| switchStatement(AstNode expression, list[AstNode] statements)
				| switchCase(bool isDefault, Option[TypedAstNode] optionalExpression)
				| synchronizedStatement(AstNode expression, AstNode body)
				| throwStatement(AstNode expression)
				| tryStatement(AstNode body, list[AstNode] catchClauses, Option[AstNode] \finally)										
				| catchClause(AstNode exception, AstNode body)
				| typeDeclarationStatement(AstNode typeDeclaration)
				| variableDeclarationStatement(list[Modifier] modifiers, TypedAstNode \type, list[AstNode] fragments)
				| whileStatement(AstNode expression, AstNode body)
							
				// Types
				| arrayType(AstNode \typeOfArray)
				| parameterizedType(AstNode \typeOfParam, list[TypedAstNode] genericTypes)
				| qualifiedType(str qualifier, str name)
				| primitiveType(str primitive)
				| simpleType(str name)
				| wildcardType(Option[AstNode] wildcardType, Option[str] bound)
																			
				// Comments 
				| blockComment()
				| lineComment()

				// Javadoc
				| javadoc()
				| tagElement()
				| textElement()
				| memberRef()
				| memberRefParameter()
				;
				
data Option[&T] = some(&T opt)
				| none()
				;

public alias TypedAstNode = tuple[Entity \type, AstNode astNode];
