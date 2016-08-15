package org.rascalmpl.eclipse.editor.proposer;

import java.util.ArrayList;
import java.util.List;

import org.rascalmpl.ast.AbstractAST;
import org.rascalmpl.ast.BasicType.Bag;
import org.rascalmpl.ast.BasicType.Bool;
import org.rascalmpl.ast.BasicType.DateTime;
import org.rascalmpl.ast.BasicType.Int;
import org.rascalmpl.ast.BasicType.Loc;
import org.rascalmpl.ast.BasicType.Map;
import org.rascalmpl.ast.BasicType.Node;
import org.rascalmpl.ast.BasicType.Num;
import org.rascalmpl.ast.BasicType.Rational;
import org.rascalmpl.ast.BasicType.Real;
import org.rascalmpl.ast.BasicType.Relation;
import org.rascalmpl.ast.BasicType.Set;
import org.rascalmpl.ast.BasicType.Tuple;
import org.rascalmpl.ast.BasicType.Value;
import org.rascalmpl.ast.BasicType.Void;
import org.rascalmpl.ast.Body.Toplevels;
import org.rascalmpl.ast.Declaration.Alias;
import org.rascalmpl.ast.Declaration.Data;
import org.rascalmpl.ast.Declaration.Function;
import org.rascalmpl.ast.Expression.TypedVariable;
import org.rascalmpl.ast.FunctionDeclaration;
import org.rascalmpl.ast.FunctionDeclaration.Abstract;
import org.rascalmpl.ast.FunctionDeclaration.Conditional;
import org.rascalmpl.ast.FunctionDeclaration.Default;
import org.rascalmpl.ast.FunctionDeclaration.Expression;
import org.rascalmpl.ast.FunctionType.TypeArguments;
import org.rascalmpl.ast.LocalVariableDeclaration.Dynamic;
import org.rascalmpl.ast.Module;
import org.rascalmpl.ast.Name.Lexical;
import org.rascalmpl.ast.NullASTVisitor;
import org.rascalmpl.ast.QualifiedName;
import org.rascalmpl.ast.Signature.NoThrows;
import org.rascalmpl.ast.Signature.WithThrows;
import org.rascalmpl.ast.Statement;
import org.rascalmpl.ast.Statement.DoWhile;
import org.rascalmpl.ast.Statement.For;
import org.rascalmpl.ast.Statement.IfThen;
import org.rascalmpl.ast.Statement.IfThenElse;
import org.rascalmpl.ast.Statement.NonEmptyBlock;
import org.rascalmpl.ast.Statement.VariableDeclaration;
import org.rascalmpl.ast.Statement.While;
import org.rascalmpl.ast.Toplevel;
import org.rascalmpl.ast.Toplevel.GivenVisibility;
import org.rascalmpl.ast.Type;
import org.rascalmpl.ast.Type.Basic;
import org.rascalmpl.ast.Type.Structured;
import org.rascalmpl.ast.Type.User;
import org.rascalmpl.ast.TypeArg;
import org.rascalmpl.ast.TypeArg.Named;
import org.rascalmpl.ast.UserType.Name;
import org.rascalmpl.ast.Variable;
import org.rascalmpl.ast.Variable.Initialized;
import org.rascalmpl.ast.Variable.UnInitialized;
import org.rascalmpl.ast.Variant;
import org.rascalmpl.ast.Variant.NAryConstructor;
import org.rascalmpl.value.ISourceLocation;

public class SymbolTreeCreator extends NullASTVisitor<List<ISymbol>> {
	private class AlgebraicDataTypeInfo extends NullASTVisitor<Boolean> {
		private class ConstructorInfo extends NullASTVisitor<Boolean> {
			private class NamedArgInfo extends NullASTVisitor<Boolean> {
				String name = "";
				
				public String getName(Named named) {
					named.accept(this);
					return name;
				}
				
				@Override
				public Boolean visitTypeArgNamed(Named x) {
					if (x.hasName()) {
						NameVisitor.getName(x.getName());
						return true;
					}
					return false;
				}
			}
			String name = "";
			
			List<ISymbol> arguments = new ArrayList<ISymbol>();
			
			private void addArgument(String argName, String type, ISourceLocation location) {
				ISymbol argumentSymbol = new Symbol(argName, Symbol.symbol_type_arg, location);
				argumentSymbol.setAttribute(Symbol.symbol_attribute_datatype, type);
				arguments.add(argumentSymbol);
			}
			
			private void addNamelessArgument(String typeName, ISourceLocation location) {
				ISymbol argumentSymbol = new Symbol("",Symbol.symbol_type_arg,location);
				argumentSymbol.setAttribute(Symbol.symbol_attribute_datatype, typeName);
				arguments.add(argumentSymbol);
			}
			
			public List<ISymbol> getArguments() {
				return arguments;
			}
			
			public boolean getInfo(NAryConstructor cons) {
				return cons.accept(this);
			}
			
			public String getName() {
				return name;
			}

			@Override
			public Boolean visitTypeArgDefault(org.rascalmpl.ast.TypeArg.Default x) {
				if (x.hasType()) {
					TypeInfo typeInfo = new TypeInfo();
					String typeName = typeInfo.getTypeName(x.getType());
					addNamelessArgument(typeName, x.getLocation());
					return true;
				}		
				return false;
			}
			
			@Override
			public Boolean visitTypeArgNamed(Named x) {
				String name = "";
				String type = Symbol.symbol_datatype_unknown;
				
				NamedArgInfo argInfo = new NamedArgInfo();
				name = argInfo.getName(x);
				
				if (x.hasType()) {
					TypeInfo typeInfo = new TypeInfo();
					type = typeInfo.getTypeName(x.getType());
				}
				
				addArgument(name, type, x.getLocation());
				return true;
			}

			@Override
			public Boolean visitVariantNAryConstructor(NAryConstructor x) {
				if (x.hasName()) {
					name = NameVisitor.getName(x.getName());
					if (x.hasArguments()) {
						for (TypeArg argument : x.getArguments()) {
							argument.accept(this);
						}
					}
					
					return true;
				}
				return false;
			}
		}
		
		String fullyQualitifedName = "";
		
		List<ISymbol> variants = new ArrayList<ISymbol>();
		
		private void addConstructor(List<ISymbol> variants, ConstructorInfo consInfo, ISourceLocation loc) {
			if (variants != null && consInfo != null) {
				ISymbol variantSymbol = new Symbol(consInfo.getName(), Symbol.symbol_type_constructor, loc);
				variantSymbol.setAttribute(Symbol.symbol_attribute_datatype, fullyQualitifedName);
				Scope variantScope = new Scope(variantSymbol);				
				for (ISymbol consArgument : consInfo.getArguments()) {
					variantScope.addSymbol(consArgument);
				}
				
				variants.add(variantScope);
			}
		}
		
		public String getFullyQualitifedName() {
			return fullyQualitifedName;
		}
		
		public boolean getInfo(Data data) {
			return data.accept(this);
		}
		
		public List<ISymbol> getVariants() {
			return variants;
		}
		
		@Override
		public Boolean visitDeclarationData(Data x) {
			
			if (x.hasUser()) {
				x.getUser().accept(this);
				
				if (x.hasVariants()) {
					for (Variant variant : x.getVariants()) {
						variant.accept(this);
					}
				}
				
				return true;
			}			
			return false;
		}	
				
		@Override
		public Boolean visitUserTypeName(Name x) {
			if (x.hasName()) {
				fullyQualitifedName = NameVisitor.getName(x.getName());
				return true;
			}		
			return false;
		}
		
		@Override
		public Boolean visitVariantNAryConstructor(NAryConstructor x) {			
			ConstructorInfo consInfo = new ConstructorInfo();			
			if (consInfo.getInfo(x)) {
				addConstructor(variants, consInfo, x.getLocation());
				return true;
			}			
			return false;
		}
	}
	
	private class AliasInfo extends NullASTVisitor<Boolean> {
		private String fullyQualifiedName = "";
		private String baseType = Symbol.symbol_datatype_unknown;

		public String getBaseType() {
			return baseType;
		}

		public String getFullyQualifiedName() {
			return fullyQualifiedName;
		}
		
		public boolean getInfo(Alias alias) {
			return alias.accept(this);
		}

		@Override
		public Boolean visitDeclarationAlias(Alias x) {
			if (x.hasUser()) {
				if (x.hasBase()) {
					TypeInfo typeInfo = new TypeInfo();
					baseType = typeInfo.getTypeName(x.getBase());
				}
				
				return x.getUser().accept(this);
			}
			return false;
		}	

		@Override
		public Boolean visitUserTypeName(Name x) {
			if (x.hasName()) {
				fullyQualifiedName = NameVisitor.getName(x.getName());
				return true;
			}
			return false;
		}
	}
	
	private class FunctionInfo extends NullASTVisitor<Boolean> {
		private String name = "";
		private List<ISymbol> parameterSymbols = new ArrayList<ISymbol>();
		private String returnType = Symbol.symbol_datatype_unknown;

		public boolean getInfo(FunctionDeclaration funcDecl) {
			return funcDecl.accept(this);
		}

		public String getName() {
			return name;
		}
		
		public List<ISymbol> getParameterSymbols() {
			return parameterSymbols;
		}

		public String getReturnType() {
			return returnType;
		}

		@Override
		public Boolean visitExpressionTypedVariable(TypedVariable x) {
			VariableInfo varInfo = new VariableInfo();
			if (varInfo.getInfo(x)) {
				addArgument(parameterSymbols, varInfo, x.getLocation());
				return true;
			}

			return false;
		}

		@Override
		public Boolean visitFormalsDefault(org.rascalmpl.ast.Formals.Default x) {
			if (x.hasFormals()) {
				for (org.rascalmpl.ast.Expression exp : x.getFormals()) {
					exp.accept(this);
				}

				return true;
			}

			return false;
		}

		@Override
		public Boolean visitFunctionDeclarationAbstract(Abstract x) {						
			if (x.hasSignature()) {
				return x.getSignature().accept(this);
			}

			return false;
		}

		@Override
		public Boolean visitFunctionDeclarationConditional(Conditional x) {
			if (x.hasSignature()) {
				return x.getSignature().accept(this);
			}

			return false;
		}

		@Override
		public Boolean visitFunctionDeclarationDefault(Default x) {
			if (x.hasSignature()) {
				return x.getSignature().accept(this);
			}

			return false;
		}

		@Override
		public Boolean visitFunctionDeclarationExpression(Expression x) {
			if (x.hasSignature()) {
				return x.getSignature().accept(this);
			}

			return false;
		}

		@Override
		public Boolean visitParametersDefault(org.rascalmpl.ast.Parameters.Default x) {

			if (x.hasFormals()) {
				return x.getFormals().accept(this);
			}

			return false;
		}

		@Override
		public Boolean visitSignatureNoThrows(NoThrows x) {
			if (x.hasName()) {
				name = NameVisitor.getName(x.getName());
				
				if (x.hasParameters()) {
					x.getParameters().accept(this);
				}
				
				if (x.hasType()) {
					TypeInfo typeInfo = new TypeInfo();
					returnType = typeInfo.getTypeName(x.getType());
				}

				return true;
			}

			return false;
		}

		@Override
		public Boolean visitSignatureWithThrows(WithThrows x) {
			if (x.hasName()) {
				name = NameVisitor.getName(x.getName());
				
				if (x.hasParameters()) {
					x.getParameters().accept(this);
				}
				
				if (x.hasType()) {
					TypeInfo typeInfo = new TypeInfo();
					returnType = typeInfo.getTypeName(x.getType());
				}

				return true;
			}

			return false;
		}
	}

	private class ModuleInfo extends NullASTVisitor<Boolean> {
		private String fullyQualifiedName = "";

		public String getFullyQualifiedName() {
			return fullyQualifiedName;
		}

		public boolean getInfo(Module module) {
			return module.accept(this);
		}

		@Override
		public Boolean visitHeaderDefault(org.rascalmpl.ast.Header.Default x) {
			if (x.hasName()) {
				fullyQualifiedName = NameVisitor.getName(x.getName());
				return true;
			}
			return false;
		}

		@Override
		public Boolean visitModuleDefault(org.rascalmpl.ast.Module.Default x) {
			if (x.hasHeader()) {
				return x.getHeader().accept(this);
			}
			return false;
		}		
	}

	private static class NameVisitor extends NullASTVisitor<String> {
		public static String getName(org.rascalmpl.ast.Name name) {
			return name.accept(new NameVisitor());			
		}
		
		public static String getName(QualifiedName name) {
			return name.accept(new NameVisitor());
		}
		
		@Override
		public String visitNameLexical(Lexical x) {
			return x.getString();
		}
		
		@Override
		public String visitQualifiedNameDefault(org.rascalmpl.ast.QualifiedName.Default x) {
			if (x.hasNames()) {
				String fullyQualifiedName = "";
				for (org.rascalmpl.ast.Name name : x.getNames()) {
					if (!fullyQualifiedName.isEmpty()) {
						fullyQualifiedName += "::";
					}
					fullyQualifiedName += name.accept(this);
				}
				return fullyQualifiedName;
			}
			return null;
		}
	}

	private class TypeInfo extends NullASTVisitor<Boolean> {
		private String type = Symbol.symbol_datatype_unknown;
		
		public String getTypeName(Type x) {
			x.accept(this);
			return type;
		}
		
		public String getTypeName(TypeArg typeArg) {
			if (typeArg.hasType()) {
				typeArg.getType().accept(this);
			}
			return type;
		}
		
		@Override
		public Boolean visitBasicTypeBag(Bag x) {
			type = Symbol.symbol_datatype_bag;
			return true;
		}
		
		@Override
		public Boolean visitBasicTypeBool(Bool x) {
			type = Symbol.symbol_datatype_boolean;
			return true;
		}
		
		@Override
		public Boolean visitBasicTypeDateTime(DateTime x) {
			type = Symbol.symbol_datatype_datetime;
			return true;
		}
		
		@Override
		public Boolean visitBasicTypeInt(Int x) {
			type = Symbol.symbol_datatype_integer;
			return true;
		}
		
		@Override
		public Boolean visitBasicTypeList(org.rascalmpl.ast.BasicType.List x) {
			type = Symbol.symbol_datatype_list;
			return true;
		}
		
		@Override
		public Boolean visitBasicTypeLoc(Loc x) {
			type = Symbol.symbol_datatype_location;
			return true;
		}	
		
		@Override
		public Boolean visitBasicTypeMap(Map x) {
			type = Symbol.symbol_datatype_map;
			return true;
		}
		
		@Override
		public Boolean visitBasicTypeNode(Node x) {
			type = Symbol.symbol_datatype_node;
			return true;
		}
		
		@Override
		public Boolean visitBasicTypeNum(Num x) {
			type = Symbol.symbol_datatype_number;
			return true;
		}
		
		@Override
		public Boolean visitBasicTypeRational(Rational x) {
			type = Symbol.symbol_datatype_rational;
			return true;
		}
		
		@Override
		public Boolean visitBasicTypeReal(Real x) {
			type = Symbol.symbol_datatype_real;
			return true;
		}
		
		@Override
		public Boolean visitBasicTypeRelation(Relation x) {
			type = Symbol.symbol_datatype_relation;
			return true;
		}
		
		@Override
		public Boolean visitBasicTypeSet(Set x) {
			type = Symbol.symbol_datatype_set;
			return true;
		}
		
		@Override
		public Boolean visitBasicTypeString(org.rascalmpl.ast.BasicType.String x) {
			type = Symbol.symbol_datatype_string;
			return true;
		}
		
		@Override
		public Boolean visitBasicTypeTuple(Tuple x) {
			type = Symbol.symbol_datatype_tuple;
			return true;
		}
		
		@Override
		public Boolean visitBasicTypeType(org.rascalmpl.ast.BasicType.Type x) {
			type = Symbol.symbol_datatype_type;
			return true;
		}
		
		@Override
		public Boolean visitBasicTypeValue(Value x) {
			type = Symbol.symbol_datatype_value;
			return true;
		}
		
		@Override
		public Boolean visitBasicTypeVoid(Void x) {
			type = Symbol.symbol_datatype_void;
			return true;
		}
		
		@Override
		public Boolean visitFunctionTypeTypeArguments(TypeArguments x) {
			if (x.hasType()) {
				x.getType().accept(this);
				
				if (x.hasArguments()) {
					String arguments = "";
					TypeInfo typeInfo = new TypeInfo();
					for (TypeArg typeArg : x.getArguments()) {
						if (!arguments.isEmpty()) {
							arguments += ",";
						}
						
						arguments += typeInfo.getTypeName(typeArg);
					}
					type += "(" + arguments + ")";
				}
				return true;
			}		
			return false;
		}
		
		@Override
		public Boolean visitStructuredTypeDefault(org.rascalmpl.ast.StructuredType.Default x) {
			if (x.hasBasicType()) {
				x.getBasicType().accept(this);
				if (x.hasArguments()) {
					String argInfo = "";
					TypeInfo typeInfo = new TypeInfo();
					for (TypeArg typeArg : x.getArguments()) {
						if (!argInfo.isEmpty()) {
							argInfo += ", HE MAHE";
						}
						
						argInfo += typeInfo.getTypeName(typeArg);
					}
					type += "[" + argInfo + "]";
				}
				return true;
			}
			return false;
		}
		
		@Override
		public Boolean visitTypeBasic(Basic x) {
			if (x.hasBasic()) {
				return x.getBasic().accept(this);
			}
			return false;			
		}
		
		@Override
		public Boolean visitTypeFunction(org.rascalmpl.ast.Type.Function x) {
			if (x.hasFunction()) {
				return x.getFunction().accept(this);
			}
			return false;
		}

		@Override
		public Boolean visitTypeStructured(Structured x) {
			if (x.hasStructured()) {
				return x.getStructured().accept(this);
			}
			return false;
		}
		
		@Override
		public Boolean visitTypeUser(User x) {
			if (x.hasUser()) {
				return x.getUser().accept(this);
			}
			return false;
		}
		
		@Override
		public Boolean visitUserTypeName(Name x) {
			if (x.hasName()) {
				type = NameVisitor.getName(x.getName());
				return true;
			}
			return false;
		}
	}

	private class VariableInfo extends NullASTVisitor<Boolean> {
		private String name = "";
		private String typeName = Symbol.symbol_datatype_unknown;

		public boolean getInfo(TypedVariable typedVar) {
			return typedVar.accept(this);
		}

		public boolean getInfo(Variable var) {
			return var.accept(this);
		}

		public String getName() {
			return name;
		}
		
		public String getTypeName() {
			return typeName;
		}

		@Override
		public Boolean visitExpressionTypedVariable(TypedVariable x) {
			if (x.hasName()) {
				name = NameVisitor.getName(x.getName());
			}
			
			if (x.hasType()) {
				TypeInfo typeInfo = new TypeInfo();
				typeName = typeInfo.getTypeName(x.getType());
			}

			return true;
		}

		@Override
		public Boolean visitVariableInitialized(Initialized x) {
			if (x.hasName()) {
				name = NameVisitor.getName(x.getName());
				return true;
			}
			return false;
		}

		@Override
		public Boolean visitVariableUnInitialized(UnInitialized x) {
			if (x.hasName()) {
				name = NameVisitor.getName(x.getName());
				return true;
			}
			return false;
		}
	}

	public static ISymbol create(AbstractAST ast) {
		List<ISymbol> symbols = ast.accept(new SymbolTreeCreator());

		if (symbols.size() == 1) {

			return symbols.get(0);
		}

		return null;
	}

	private void addAlias(List<ISymbol> symbols, AliasInfo aliasInfo, ISourceLocation location) {
		if (symbols != null && aliasInfo != null) {
			ISymbol aliasSymbol = new Symbol(aliasInfo.getFullyQualifiedName(), Symbol.symbol_type_alias, location);
			aliasSymbol.setAttribute(Symbol.symbol_attribute_datatype, aliasInfo.getBaseType());
			symbols.add(aliasSymbol);
		}
	}

	private void addArgument(List<ISymbol> symbols, VariableInfo varInfo, ISourceLocation location) {
		if (symbols != null && varInfo != null) {
			ISymbol argSymbol = new Symbol(varInfo.getName(), Symbol.symbol_type_arg, location);
			argSymbol.setAttribute(Symbol.symbol_attribute_datatype, varInfo.getTypeName());
			symbols.add(argSymbol);
		}
	}

	private void addDataType(List<ISymbol> symbols, AlgebraicDataTypeInfo adtInfo, ISourceLocation location) {
		if (symbols != null && adtInfo != null) {
			symbols.add(new Symbol(adtInfo.getFullyQualitifedName(), Symbol.symbol_type_adt, location));
			for (ISymbol variant : adtInfo.getVariants()) {
				variant.setAttribute(Symbol.symbol_attribute_datatype, adtInfo.getFullyQualitifedName());
				symbols.add(variant);
			}
		}		
	}

	private void addFunction(List<ISymbol> symbols, FunctionDeclaration funcDecl) {
		FunctionInfo info = new FunctionInfo();
		if (info.getInfo(funcDecl)) {
			ISymbol scopeSymbol = new Symbol(info.getName(), Symbol.symbol_type_function, funcDecl.getLocation());
			scopeSymbol.setAttribute(Symbol.symbol_attribute_datatype, info.getReturnType());			
			Scope currentScope = new Scope(scopeSymbol);			
			symbols.add(currentScope);

			if (funcDecl.hasBody()) {
				currentScope.addSymbols(funcDecl.getBody().accept(this));
			}

			List<ISymbol> paramSymbols = info.getParameterSymbols();
			if (paramSymbols != null) {
				currentScope.addSymbols(paramSymbols);
			}
		}
	}

	private void addVariable(List<ISymbol> symbols, VariableInfo info, ISourceLocation location, String varType) {
		if (info != null && symbols != null) {
			String typeName = info.getTypeName().equals(Symbol.symbol_datatype_unknown) && varType != null && !varType.isEmpty() ? varType : info.getTypeName();
			ISymbol varSymbol = new Symbol(info.getName(), Symbol.symbol_type_var, location);
			varSymbol.setAttribute(Symbol.symbol_attribute_datatype, typeName);
			symbols.add(varSymbol);
		}
	}

	private void addVariables(List<ISymbol> symbols, List<Variable> vars, String varType) {
		for (Variable var : vars) {
			VariableInfo varInfo = new VariableInfo();
			if (varInfo.getInfo(var)) {				
				addVariable(symbols, varInfo, var.getLocation(), varType);
			}
		}
	}
	
	@Override
	public List<ISymbol> visitBodyToplevels(Toplevels x) {
		List<ISymbol> symbols = new ArrayList<ISymbol>();

		if (x.hasToplevels()) {
			for (Toplevel levels : x.getToplevels()) {
				List<ISymbol> subsymbols = levels.accept(this);
				if (subsymbols != null) {
					symbols.addAll(subsymbols);
				}
			}
		}

		return symbols;
	}
	
	@Override
	public List<ISymbol> visitDeclarationAlias(Alias x) {
		List<ISymbol> symbols = new ArrayList<ISymbol>();
		AliasInfo aliasInfo = new AliasInfo();
		if (aliasInfo.getInfo(x)) {
			addAlias(symbols, aliasInfo, x.getLocation());
		}

		return symbols;
	}

	@Override
	public List<ISymbol> visitDeclarationData(Data x) {
		List<ISymbol> symbols = new ArrayList<ISymbol>();
		AlgebraicDataTypeInfo adtInfo = new AlgebraicDataTypeInfo();
		if (adtInfo.getInfo(x)) {
			addDataType(symbols, adtInfo, x.getLocation());
		}				
		
		return symbols;
	}

	@Override
	public List<ISymbol> visitDeclarationFunction(Function x) {
		List<ISymbol> symbols = new ArrayList<ISymbol>();

		if (x.hasFunctionDeclaration()) {
			addFunction(symbols, x.getFunctionDeclaration());
		}

		return symbols;
	}

	@Override
	public List<ISymbol> visitDeclarationVariable(org.rascalmpl.ast.Declaration.Variable x) {
		List<ISymbol> symbols = new ArrayList<ISymbol>();

		String typeName = "";		
		if (x.hasType()) {
			TypeInfo typeInfo = new TypeInfo();
			typeName = typeInfo.getTypeName(x.getType());
		}
		
		if (x.hasVariables()) {
			addVariables(symbols, x.getVariables(), typeName);
		}	
		
		return symbols;
	}

	@Override
	public List<ISymbol> visitDeclaratorDefault(org.rascalmpl.ast.Declarator.Default x) {
		List<ISymbol> symbols = new ArrayList<ISymbol>();

		String typeName = "";
		if (x.hasType()) {
			TypeInfo typeInfo = new TypeInfo();
			typeName = typeInfo.getTypeName(x.getType());
		}
		
		if (x.hasVariables()) {
			addVariables(symbols, x.getVariables(), typeName);
		}

		return symbols;
	}

	@Override
	public List<ISymbol> visitFunctionBodyDefault(org.rascalmpl.ast.FunctionBody.Default x) {
		List<ISymbol> symbols = new ArrayList<ISymbol>();

		if (x.hasStatements()) {
			for (Statement statement : x.getStatements()) {
				List<ISymbol> subsymbols = statement.accept(this);
				if (subsymbols != null) {
					symbols.addAll(subsymbols);
				}
			}
		}

		return symbols;
	}

	@Override
	public List<ISymbol> visitLocalVariableDeclarationDefault(org.rascalmpl.ast.LocalVariableDeclaration.Default x) {
		if (x.hasDeclarator()) {
			return x.getDeclarator().accept(this);
		}

		return null;
	}

	@Override
	public List<ISymbol> visitLocalVariableDeclarationDynamic(Dynamic x) {
		if (x.hasDeclarator()) {
			return x.getDeclarator().accept(this);
		}

		return null;
	}

	@Override
	public List<ISymbol> visitModuleDefault(org.rascalmpl.ast.Module.Default x) {
		List<ISymbol> symbols = new ArrayList<ISymbol>(1);

		ModuleInfo moduleInfo = new ModuleInfo();
		if (moduleInfo.getInfo(x)) {
			Scope currentScope = new Scope(new Symbol(moduleInfo.getFullyQualifiedName(), Symbol.symbol_type_module, x.getLocation()));
			symbols.add(currentScope);

			if (x.hasBody()) {
				currentScope.addSymbols(x.getBody().accept(this));
			}
		}

		return symbols;
	}

	@Override
	public List<ISymbol> visitStatementDoWhile(DoWhile x) {
		List<ISymbol> symbols = new ArrayList<ISymbol>();

		if (x.hasBody()) {
			List<ISymbol> subSymbols = x.getBody().accept(this);
			if (subSymbols != null) {
				symbols.addAll(subSymbols);
			}

			// TODO: Get variables declared in expressions into body scope
		}

		return symbols;
	}

	@Override
	public List<ISymbol> visitStatementFor(For x) {
		List<ISymbol> symbols = new ArrayList<ISymbol>();

		if (x.hasBody()) {
			List<ISymbol> subSymbols = x.getBody().accept(this);
			if (subSymbols != null) {
				symbols.addAll(subSymbols);
			}

			// TODO: Get variables declared in expressions into body scope
		}

		return symbols;
	}

	@Override
	public List<ISymbol> visitStatementFunctionDeclaration(org.rascalmpl.ast.Statement.FunctionDeclaration x) {
		List<ISymbol> symbols = new ArrayList<ISymbol>();

		if (x.hasFunctionDeclaration()) {
			addFunction(symbols, x.getFunctionDeclaration());
		}

		return symbols;
	}

	@Override
	public List<ISymbol> visitStatementIfThen(IfThen x) {
		if (x.hasThenStatement()) {
			List<ISymbol> symbols = new ArrayList<ISymbol>();
			List<ISymbol> subSymbols = x.getThenStatement().accept(this);
			if (subSymbols != null) {
				symbols.addAll(subSymbols);
			}

			return symbols;
		}

		return null;
	}

	@Override
	public List<ISymbol> visitStatementIfThenElse(IfThenElse x) {
		List<ISymbol> symbols = new ArrayList<ISymbol>();

		if (x.hasThenStatement()) {
			List<ISymbol> subSymbols = x.getThenStatement().accept(this);
			if (subSymbols != null) {
				symbols.addAll(subSymbols);
			}
		}

		if (x.hasElseStatement()) {
			List<ISymbol> subSymbols = x.getElseStatement().accept(this);
			if (subSymbols != null) {
				symbols.addAll(subSymbols);
			}
		}

		return symbols;
	}

	@Override
	public List<ISymbol> visitStatementNonEmptyBlock(NonEmptyBlock x) {
		List<ISymbol> symbols = new ArrayList<ISymbol>();
		Scope currentScope = new Scope(null, x.getLocation());
		symbols.add(currentScope);

		if (x.hasStatements()) {
			for (Statement statement : x.getStatements()) {
				currentScope.addSymbols(statement.accept(this));
			}
		}

		return symbols;
	}

	@Override
	public List<ISymbol> visitStatementVariableDeclaration(VariableDeclaration x) {
		if (x.hasDeclaration()) {
			return x.getDeclaration().accept(this);
		}

		return null;
	}

	@Override
	public List<ISymbol> visitStatementWhile(While x) {
		List<ISymbol> symbols = new ArrayList<ISymbol>();

		if (x.hasBody()) {
			List<ISymbol> subSymbols = x.getBody().accept(this);
			if (subSymbols != null) {
				symbols.addAll(subSymbols);
			}

			// TODO: Get variables declared in expressions into body scope
		}

		return symbols;
	}

	@Override
	public List<ISymbol> visitToplevelGivenVisibility(GivenVisibility x) {
		List<ISymbol> symbols = new ArrayList<ISymbol>();

		if (x.hasDeclaration()) {
			List<ISymbol> subsymbols = x.getDeclaration().accept(this);
			if (subsymbols != null) {
				symbols.addAll(subsymbols);
			}
		}

		return symbols;
	}
}
