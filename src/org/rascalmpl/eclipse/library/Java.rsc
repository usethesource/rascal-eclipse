module Java

import List;
import Integer;
import Node;
import Exception;

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

@doc{the root of Java's type hierarchy}
public Entity Object = entity([package("java"), package("lang"), class("Object")]);

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

public Entity makeClass(str className) {
  return entity([package(p) | /<p:[a-z_A-Z0-9]+>\./ := className] + [class(c) | /\.<c:[a-z_A-Z0-9]+>$/ := className]);
}

public Entity makeInterface(str interfaceName) {
  return entity([package(p) | /<p:[a-z_A-Z0-9]+>\./ := interfaceName] + [interface(c) | /\.<c:[a-z_A-Z0-9]+>$/ := interfaceName])
}

public str readable(Entity entity) {
	str result = "";
	list[Id] ids = entity.id;
	
	if (size(ids) > 0) {
		result = readable(head(ids));	
		for (id <- tail(ids)) {
			result += "." + readable(id);	
		}
	}
	
	
	
	return result;
}

public str readable(list[Entity] entities) {
	str result = "";
	
	if (size(entities) > 0) {
		result = readable(head(entities));
		for (Entity entity <- tail(entities)) {
			result += ", " + readable(entity);	
		}
	}
	
	return result;
}

public str readable(Id id) {
	switch (id) {
		case class(name, params):
			return name + "\<<readable(params)>\>"; 		
		case interface(name, params):
			return name + "\<<readable(params)>\>"; 		
        case method(name, params, returnType):
			return name + "(<readable(params)>)"; 	
        	
	}

	try {
		return id.name;
	} catch : ;
	
	switch (id) {
		case anonymousClass(nr): return "anonymousClass$" + "<nr>";		
		case constr(params): return "constructor(" + readable(params) + ")";		
		case initializer: return "initializer";
		case initializer(nr): return "initializer$" + "<nr>";		
		case primitive(p): return getName(p);
		case array(elementType): return readable(elementType) + "[]";		
		case wildcard: return "?";
		case wildcard(extends(bound)): return "? extends " + readable(bound);
		case wildcard(super(bound)): return "? super " + readable(bound);
		default : throw IllegalArgument(id);
	}
}