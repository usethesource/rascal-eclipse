module Java

import List;
import Integer;
import Node;

@doc{an entity is identified by a list of identifiers, it represents a qualified name of something in Java code}
data Entity = entity(list[Id] id);

@doc{Id's are parts of java source code that have a name. They are used to construct unique entity names}
data Id = package(str name)
        | class(str name)
        | class(str name, list[Entity] params)
        | interface(str name)
        | interface(str name, list[Entity] params)
        | anonymousClass(int nr)
        | enum(str name)
        
        | method(str name, list[Entity] params, Entity returnType)
        | constr(list[Entity] params)
        | initializer()
        | initializer(int nr)

        | field(str name)
        | parameter(str name)
        | variable(str name, int id)
        | enumConstant(str name)
        
        | primitive(PrimitiveType primType)
        | array(Entity elementType)
        
        | typeParameter(str name)
        | wildcard()
        | wildcard(Bound bound)
;

@doc{these are the primitive types of Java}
data PrimitiveType = byte()
                   | short()
                   | \int()
                   | long()
                   | float()
                   | double()
                   | char()
                   | boolean()
                   | \void()
                   | null()
;

data Bound = extends(Entity extended)
           | super(Entity super)
;

data Modifier = \public()
			  | protected()
			  | \private()
			  | static()
			  | abstract()
			  | final()
			  | native()
			  | synchronized()
			  | transient()
			  | volatile()
			  | strictfp()
			  | deprecated() 
;

public str toString(Entity entity) {
	str result = "";
	list[Id] ids = entity.id;
	
	if (size(ids) > 0) {
		result = patchToString(head(ids));	
		for (id <- tail(ids)) {
			result += "." + patchToString(id);	
		}
	}
	
	return result;
}

public str toString(list[Entity] entities) {
	str result = "";
	
	if (size(entities) > 0) {
		result = patchToString(head(entities));
		for (entity <- tail(entities)) {
			result += ", " + patchToString(entity);	
		}
	}
	
	return result;
}

public str toString(Id id) {
	switch (id) {
		case class(name, params):
			return name + "\<" + patchToString(params) + "\>"; 		
		case interface(name, params):
			return name + "\<" + patchToString(params) + "\>"; 		
        case method(name, params, returnType):
			return name + "(" + patchToString(params) + ")"; 		
	}

	try {
		return id.name;
	} catch : ;
	
	switch (id) {
		case anonymousClass(nr): return "anonymousClass$" + toString(nr);		
		case constructor(params): return "constructor(" + patchToString(params) + ")";		
		case initializer: return "initializer";
		case initializer(nr): return "initializer$" + toString(nr);		
		case primitive(p): return getName(p);
		case array(elementType): return patchToString(elementType) + "[]";		
		case wildcard: return "?";
		case wildcard(extends(bound)): return "? extends " + toString(bound);
		case wildcard(super(bound)): return "? super " + toString(bound);
		default : throw IllegalArgument(id);
	}
}