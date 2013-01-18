@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Jouke Stoel - Jouke.Stoel@cwi.nl (CWI)}
module lang::java::jdt::JavaADT

import lang::java::jdt::Java;

@doc{This annotation has been removed, use @bindings["typeBinding"] instead}
// @doc{Returns the type of the AstNode if it can be resolved}
// anno Entity AstNode@javaType; 

@doc{All the bindings that can be resolved for a node}
anno map[str, Entity] AstNode@bindings;

@doc{Each node is linked to the location in a source}
anno loc AstNode@location;

data AstNode	= compilationUnit(Option[AstNode] package, list[AstNode] imports, list[AstNode] typeDeclarations)
				
				// Declarations
				| anonymousClassDeclaration(list[AstNode] bodyDeclarations)
				| annotationTypeDeclaration(list[Modifier] modifiers, list[AstNode] annotations, str name, list[AstNode] bodyDeclarations)
				| annotationTypeMemberDeclaration(list[Modifier] modifiers, list[AstNode] annotations, AstNode typeArgument, str name, Option[AstNode] defaultBlock)
				| enumDeclaration(list[Modifier] modifiers, list[AstNode] annotations, str name, list[AstNode] implements, list[AstNode] enumConstants, list[AstNode] bodyDeclarations)
				| enumConstantDeclaration(list[Modifier] modifiers, list[AstNode] annotations, str name, list[AstNode] arguments, Option[AstNode] anonymousClassDeclaration)
				| typeDeclaration(list[Modifier] modifiers, list[AstNode] annotations, str objectType, str name, list[AstNode] genericTypes, Option[AstNode] extends, list[AstNode] implements, list[AstNode] bodyDeclarations)
				| fieldDeclaration(list[Modifier] modifiers, list[AstNode] annotations, AstNode \type, list[AstNode] fragments)
				| initializer(list[Modifier] modifiers, list[AstNode] annotations, AstNode body)
				| methodDeclaration(list[Modifier] modifiers, list[AstNode] annotations, list[AstNode] genericTypes, Option[AstNode] returnType, str name, list[AstNode] parameters, list[AstNode] possibleExceptions, Option[AstNode] implementation)
				| importDeclaration(str name, bool staticImport, bool onDemand)
				| packageDeclaration(str name, list[AstNode] annotations)
				| singleVariableDeclaration(str name, list[Modifier] modifiers, list[AstNode] annotations, AstNode \type, Option[AstNode] initializer, bool isVarargs)
				| variableDeclarationFragment(str name, Option[AstNode] initializer)
				| typeParameter(str name, list[AstNode] extendsList)

				// Expressions
				| markerAnnotation(str typeName)
				| normalAnnotation(str typeName, list[AstNode] memberValuePairs)
				| memberValuePair(str name, AstNode \value)				
				| singleMemberAnnotation(str typeName, AstNode \value)
				| arrayAccess(AstNode array, AstNode index)
				| arrayCreation(AstNode \type, list[AstNode] dimensions, Option[AstNode] initializer)
				| arrayInitializer(list[AstNode] expressions)
				| assignment(AstNode leftSide, AstNode rightSide)
				| booleanLiteral(bool boolValue)
				| castExpression(AstNode \type, AstNode expression)
				| characterLiteral(str charValue)
				| classInstanceCreation(Option[AstNode] optionalExpression, AstNode \type, list[AstNode] genericTypes, list[AstNode] typedArguments, Option[AstNode] anonymousClassDeclaration)
				| conditionalExpression(AstNode expression, AstNode thenBranch, AstNode elseBranch)
				| fieldAccess(AstNode expression, str name)
				| infixExpression(str operator, AstNode leftSide, AstNode rightSide, list[AstNode] extendedOperands)
				| instanceofExpression(AstNode leftSide, AstNode rightSide)
				| methodInvocation(Option[AstNode] optionalExpression, list[AstNode] genericTypes, str name, list[AstNode] typedArguments)
				| superMethodInvocation(Option[AstNode] optionalQualifier, list[AstNode] genericTypes, str name, list[AstNode] typedArguments)
				| qualifiedName(AstNode qualifier, str name)
				| simpleName(str simpleName)
				| nullLiteral()
				| numberLiteral(str number)
				| parenthesizedExpression(AstNode expression)
				| postfixExpression(AstNode operand, str operator)
				| prefixExpression(AstNode operand, str operator)
				| stringLiteral(str stringValue)
				| superFieldAccess(Option[AstNode] optionalQualifier, str name)
				| thisExpression(Option[AstNode] optionalQualifier)
				| typeLiteral(AstNode \type)
				| variableDeclarationExpression(list[Modifier] modifiers, list[AstNode] annotations, AstNode \type, list[AstNode] fragments)
						
				// Statements
				| assertStatement(AstNode expression, Option[AstNode] message)
				| block(list[AstNode] statements)
				| breakStatement(Option[str] label)
				| constructorInvocation(list[AstNode] genericTypes, list[AstNode] typedArguments)
				| superConstructorInvocation(Option[AstNode] optionalExpression, list[AstNode] genericTypes, list[AstNode] typedArguments)
				| continueStatement(Option[str] label)
				| doStatement(AstNode body, AstNode whileExpression)
				| emptyStatement()
				| enhancedForStatement(AstNode parameter, AstNode collectionExpression, AstNode body)
				| expressionStatement(AstNode expression)
				| forStatement(list[AstNode] initializers, Option[AstNode] optionalBooleanExpression, list[AstNode] updaters, AstNode body)
				| ifStatement(AstNode booleanExpression, AstNode thenStatement, Option[AstNode] elseStatement)
				| labeledStatement(str name, AstNode body)
				| returnStatement(Option[AstNode] optionalExpression)
				| switchStatement(AstNode expression, list[AstNode] statements)
				| switchCase(bool isDefault, Option[AstNode] optionalExpression)
				| synchronizedStatement(AstNode expression, AstNode body)
				| throwStatement(AstNode expression)
				| tryStatement(AstNode body, list[AstNode] catchClauses, Option[AstNode] \finally)										
				| catchClause(AstNode exception, AstNode body)
				| typeDeclarationStatement(AstNode typeDeclaration)
				| variableDeclarationStatement(list[Modifier] modifiers, list[AstNode] annotations, AstNode \type, list[AstNode] fragments)
				| whileStatement(AstNode expression, AstNode body)
							
				// Types
				| arrayType(AstNode \typeOfArray)
				| parameterizedType(AstNode \typeOfParam, list[AstNode] genericTypes)
				| qualifiedType(AstNode qualifier, str name)
				| primitiveType(PrimitiveType primitive)
				| simpleType(str name)
				| unionType(list[AstNode] types)
				| wildcardType(Option[AstNode] bound, Option[str] lowerOrUpper)
																			
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
