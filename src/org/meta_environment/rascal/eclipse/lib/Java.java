package org.meta_environment.rascal.eclipse.lib;

import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.pdb.facts.type.TypeStore;

/**
 * Abstract Data Types for Java entities.
 * @author basten@cwi.nl
 */
public class Java {

	private static final TypeFactory TF = TypeFactory.getInstance();
	private static final TypeStore store = new TypeStore();

	
	public static final Type ADT_ID = TF.abstractDataType(store, "Id");
	public static final Type ADT_ENTITY = TF.abstractDataType(store, "Entity");
	public static final Type ADT_PRIMITIVETYPE = TF.abstractDataType(store, "PrimitiveType");

	public static final Type CONS_ENTITY = TF.constructor(store, ADT_ENTITY, "entity", TF.listType(ADT_ID), "id");

	public static final Type CONS_PACKAGE = TF.constructor(store, ADT_ID, "package", TF.stringType(), "name");
	public static final Type CONS_CLASS = TF.constructor(store, ADT_ID, "class", TF.stringType(), "name");
	public static final Type CONS_GENERIC_CLASS = TF.constructor(store, ADT_ID, "class", TF.stringType(), "name", TF.listType(ADT_ENTITY), "params");
	public static final Type CONS_INTERFACE = TF.constructor(store, ADT_ID, "interface", TF.stringType(), "name");
	public static final Type CONS_GENERIC_INTERFACE = TF.constructor(store, ADT_ID, "interface", TF.stringType(), "name", TF.listType(ADT_ENTITY), "params");
	public static final Type CONS_ANONYMOUS_CLASS = TF.constructor(store, ADT_ID, "anonymousClass", TF.integerType(), "nr");
	public static final Type CONS_METHOD = TF.constructor(store, ADT_ID, "method", TF.stringType(), "name", TF.listType(ADT_ENTITY), "params", ADT_ENTITY, "returnType");
	public static final Type CONS_CONSTRUCTOR = TF.constructor(store, ADT_ID, "constructor", TF.listType(ADT_ENTITY), "params");
	public static final Type CONS_INITIALIZER = TF.constructor(store, ADT_ID, "initializer");
	public static final Type CONS_INITIALIZER_NUMBERED = TF.constructor(store, ADT_ID, "initializer", TF.integerType(), "nr");
	public static final Type CONS_FIELD = TF.constructor(store, ADT_ID, "field", TF.stringType(), "name");
	public static final Type CONS_PARAMETER = TF.constructor(store, ADT_ID, "parameter", TF.stringType(), "name");
	public static final Type CONS_VARIABLE = TF.constructor(store, ADT_ID, "variable", TF.stringType(), "name");
	public static final Type CONS_PRIMITIVE = TF.constructor(store, ADT_ID, "primitive", ADT_PRIMITIVETYPE, "type");
	public static final Type CONS_ARRAY = TF.constructor(store, ADT_ID, "array", ADT_ENTITY, "elementType");
	public static final Type CONS_ENUM = TF.constructor(store, ADT_ID, "enum", TF.stringType(), "name");
	public static final Type CONS_ENUM_CONSTANT = TF.constructor(store, ADT_ID, "enumConstant", TF.stringType(), "name");
	public static final Type CONS_TYPE_PARAMETER = TF.constructor(store, ADT_ID, "typeParameter", TF.stringType(), "name");
	public static final Type CONS_WILDCARD = TF.constructor(store, ADT_ID, "wildcard");

	public static final Type CONS_BYTE = TF.constructor(store, ADT_PRIMITIVETYPE, "byte");
	public static final Type CONS_SHORT = TF.constructor(store, ADT_PRIMITIVETYPE, "short");
	public static final Type CONS_INT = TF.constructor(store, ADT_PRIMITIVETYPE, "int");
	public static final Type CONS_LONG = TF.constructor(store, ADT_PRIMITIVETYPE, "long");
	public static final Type CONS_FLOAT = TF.constructor(store, ADT_PRIMITIVETYPE, "float");
	public static final Type CONS_DOUBLE = TF.constructor(store, ADT_PRIMITIVETYPE, "double");
	public static final Type CONS_CHAR = TF.constructor(store, ADT_PRIMITIVETYPE, "char");
	public static final Type CONS_BOOLEAN = TF.constructor(store, ADT_PRIMITIVETYPE, "boolean");
	public static final Type CONS_VOID = TF.constructor(store, ADT_PRIMITIVETYPE, "void");
	public static final Type CONS_NULL = TF.constructor(store, ADT_PRIMITIVETYPE, "null");

}
