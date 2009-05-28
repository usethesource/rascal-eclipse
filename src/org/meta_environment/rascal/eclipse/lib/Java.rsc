module Java

data Entity = entity(list[Id] id);

data Id = package(str name)
        | class(str name)
        | class(str name, list[Entity] params)
        | interface(str name)
        | interface(str name, list[Entity] params)
        | anonymousClass(int nr)
        
        | method(str name, list[Entity] params, Entity returnType)
        | constructor(list[Entity] params)
        | initializer
        | initializer(int nr)
        | field(str name)
        | param(str name)
        | var(str name)
        
        | primitive(PrimitiveType type)
        | array(Entity)
        
        | enum(str name)
        | enumConstant(str name)
        
        | paramType(str name)
        | wildcard
;


data PrimitiveType = byte
                   | short
                   | \int
                   | long
                   | float
                   | double
                   | char
                   | boolean
;


