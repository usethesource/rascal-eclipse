module Java

import List;
import Integer;
import Node;
import Exception;


data Entity = entity(list[Id] id);

data Id = package(str name)
        | class(str name)
        | class(str name, list[Entity] params)
        | interface(str name)
        | interface(str name, list[Entity] params)
        | anonymousClass(int nr)
        | enum(str name)
        
        | method(str name, list[Entity] params, Entity returnType)
        | constructor(list[Entity] params)
        | initializer
        | initializer(int nr)

        | field(str name)
        | parameter(str name)
        | variable(str name)
        | enumConstant(str name)
        
        | primitive(PrimitiveType type)
        | array(Entity elementType)
        
        | typeParameter(str name)
        | wildcard
        | wildcard(Bound bound)
;

data PrimitiveType = byte
                   | short
                   | \int
                   | long
                   | float
                   | double
                   | char
                   | boolean
                   | \void
                   | null
;

data Bound = extends(Entity type)
           | super(Entity type);


public str toString(Entity entity) {
	str result = "";
	list[Id] ids = entity.id;
	
	if (size(ids) > 0) {
		result = toString(head(ids));	
		for (id <- tail(ids)) {
			result += "." + toString(id);	
		}
	}
	
	return result;
}

public str toString(list[Entity] entities) {
	str result = "";
	
	if (size(entities) > 0) {
		result = toString(head(entities));
		for (entity <- tail(entities)) {
			result += ", " + toString(entity);	
		}
	}
	
	return result;
}

public str toString(Id id) {
	switch (id) {
		case class(name, params):
			return name + "\<" + toString(params) + ">"; 		
		case interface(name, params):
			return name + "\<" + toString(params) + ">"; 		
        case method(name, params, returnType):
			return name + "(" + toString(params) + ")"; 		
	}

	try {
		return id.name;
	} catch : ;
	
	switch (id) {
		case anonymousClass(nr): return "anonymousClass$" + toString(nr);		
		case constructor(params): return "constructor(" + toString(params) + ")";		
		case initializer: return "initializer";
		case initializer(nr): return "initializer$" + toString(nr);		
		case primitive(p): return getName(p);
		case array(elementType): return toString(elementType) + "[]";		
		case wildcard: return "?";
		case wildcard(extends(bound)): return "? extends " + toString(bound);
		case wildcard(super(bound)): return "? super " + toString(bound);
		default : throw IllegalArgument(id);
	}
}
