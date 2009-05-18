module JDT

public alias JDTFactRel = rel[tuple[int, int], str];
public alias TypeOrNameRel[&T] = rel[&T, str];
public alias LocationRel[&T] = rel[&T, loc];


public map[str, JDTFactRel] java facts(str project, str file)
@javaClass{org.meta_environment.rascal.eclipse.lib.JDT}
;

public tuple[TypeOrNameRel[&T], JDTFactRel] mapLocations(JDTFactRel jdtf, LocationRel[&T] locs) {

  TypeOrNameRel[&T] found = {};
  JDTFactRel notfound = {};

  for ( <<int offset, int length>, str s> <- jdtf ) {
    TypeOrNameRel[&T] search = { <n,s> | <&T n, loc l> <- locs, l.offset == offset, l.length == length };

    if (search != {}) {
      found += search;
    } else {
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


public tuple[map[str, TypeOrNameRel[&T]], map[str, JDTFactRel]] mapAll(map[str, JDTFactRel] facts, LocationRel[&T] locations) {
  map[str, TypeOrNameRel[&T]] found = ();
  map[str, JDTFactRel] notfound = ();

  for ( str s <- facts ) {
    tuple[TypeOrNameRel[&T], JDTFactRel] r = mapLocations(facts[s], locations);
    found += (s : r[0]);
    notfound += (s : r[1]);
  }
  
  return <found, notfound>;
}
