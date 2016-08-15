package org.rascalmpl.eclipse.editor.proposer;

import java.util.HashMap;
import java.util.Set;

import org.rascalmpl.value.ISourceLocation;

public class Symbol implements ISymbol {
	public static final String symbol_type_void = "Void";
	public static final String symbol_type_module = "Module";
	public static final String symbol_type_function = "Function";
	public static final String symbol_type_var = "Variable";
	public static final String symbol_type_arg = "Argument";
	public static final String symbol_type_alias = "Alias";
	public static final String symbol_type_adt = "ADT";
	public static final String symbol_type_constructor = "Constructor";
	
	public static final String symbol_attribute_datatype = "Datatype";
	
	// Basic datatypes
	public static final String symbol_datatype_unknown = "Unknown";
	public static final String symbol_datatype_string = "str";
	public static final String symbol_datatype_boolean = "bool";
	public static final String symbol_datatype_bag = "bag";
	public static final String symbol_datatype_datetime = "datetime";
	public static final String symbol_datatype_integer = "int";
	public static final String symbol_datatype_list = "list";
	public static final String symbol_datatype_location = "loc";
	public static final String symbol_datatype_map = "map";
	public static final String symbol_datatype_node = "node";
	public static final String symbol_datatype_number = "num";
	public static final String symbol_datatype_rational = "rational";
	public static final String symbol_datatype_real = "real";
	public static final String symbol_datatype_relation = "rel";
	public static final String symbol_datatype_set = "set";
	public static final String symbol_datatype_tuple = "tuple";
	public static final String symbol_datatype_type = "type";
	public static final String symbol_datatype_value = "value";
	public static final String symbol_datatype_void = "void";	
		
	private final String name;
	private final String type;
	private final ISourceLocation location;
	private String label;
	
	private final HashMap<String, String> attributes = new HashMap<String, String>();

	public Symbol(String name, String type, ISourceLocation location) {
		this.name = name;
		this.type = type;
		this.location = location;
		this.label = name;		
	}

	@Override
	public <T> T accept(SymbolVisitor<T> visitor) {
		return visitor.visitSymbol(this);
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public ISourceLocation getLocation() {
		return location;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getType() {
		return type;
	}
	
	@Override
	public String getAttribute(String key) {
		return attributes.get(key);
	}
	
	@Override
	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		// symbol(str name, str symbolType)
		// symbol(str name, str symbolType, map[str, str] attributes)
		
		//TODO: Serialize location. Find way to add multiple annotations.
		
		String labelAnnotation = label.isEmpty() ? "" : "[@label=\"" + label + "\"]";
		
		if (attributes.size() > 0) {
			Set<String> attributeKeys = attributes.keySet();
			String attributeMapString = "";	
			for (String key : attributeKeys) {
				if (!attributeMapString.isEmpty()) attributeMapString += ", ";
				attributeMapString += String.format("\"%s\" : \"%s\"", key, attributes.get(key)); 
			}
			
			attributeMapString = "(" + attributeMapString + ")";
			
			return String.format("symbol(\"%s\", \"%s\", %s)%s", name, type, attributeMapString, labelAnnotation);
		}
		
		return String.format("symbol(\"%s\", \"%s\")%s", name, type, labelAnnotation);
	}
}
