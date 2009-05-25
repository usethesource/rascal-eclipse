module JDT

// TBD: ADT definition for types/names?

public alias BindingRel = rel[tuple[int, int], str];
public alias TypeInfoRel = rel[str, str];
public alias TypeOrNameRel[&T] = rel[&T, str];
public alias LocationRel[&T] = rel[&T, loc];

/*
Imports the following binding relations:
- typeBindings
- methodBindings
- constructorBindings
- fieldBindings
- variableBindings 
*/
public map[str, BindingRel] java getBindings(str project, str file)
@javaClass{org.meta_environment.rascal.eclipse.lib.JDT}
;

/*
Imports the following relations:
- implements
- extends
- declaredTypes
- declaredMethods
- declaredFields
*/
public map[str, TypeInfoRel] java getTypeInfo(str project, str file)
@javaClass{org.meta_environment.rascal.eclipse.lib.JDT}
;

public tuple[TypeOrNameRel[&T], BindingRel] mapLocations(BindingRel bindings, LocationRel[&T] locs) {

  TypeOrNameRel[&T] found = {};
  BindingRel notfound = {};

  for ( <<int offset, int length>, str s> <- bindings ) {
    TypeOrNameRel[&T] search = { <n,s> | <&T n, loc l> <- locs, l.offset == offset, l.length == length };

    if (search != {}) {
      found += search;
    } else {
      // If a declaration is preceded by Javadoc comments, the JDT parser includes them
      // in the location info of the declaration node. Then the node's location doesn't
      // match with 'ours'. Here we try to find the longest location that ends at the same
      // position as the JDT node, but starts after the JDT node's offset position.
        
      int closest = offset + length;
      &T candidate;

      for ( <&T n, loc l> <- locs ) {
        if (l.offset + l.length == offset + length && l.offset > offset) {
          if (l.offset < closest) {
            closest = l.offset;
            candidate = n;
          }
        }
      }
      
      if (closest != offset + length) {
        found += {<candidate, s>};        
      } else {
        notfound += <<offset, length>, s>;
      }
    }
  }

  return <found, notfound>;
}


public tuple[map[str, TypeOrNameRel[&T]], map[str, BindingRel]] mapAll(map[str, BindingRel] facts, LocationRel[&T] locations) {
  map[str, TypeOrNameRel[&T]] found = ();
  map[str, BindingRel] notfound = ();

  for ( str s <- facts ) {
    tuple[TypeOrNameRel[&T], BindingRel] r = mapLocations(facts[s], locations);
    found += (s : r[0]);
    notfound += (s : r[1]);
  }
  
  return <found, notfound>;
}
