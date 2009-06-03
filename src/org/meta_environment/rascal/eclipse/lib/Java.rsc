module Java

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
        | wildcard(Entity bound)
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


