module JDT

import Map;
import Node;
import Resources;
import Java;

@doc{maps any ast at a certain location to a qualified name}
public alias BindingRel = rel[loc, Entity];

@doc{relationship between entities}
public alias EntityRel = rel[Entity from, Entity to];

@doc{collection of entities}
public alias EntitySet = set[Entity];

@doc{maps an entity to its modifiers}
public alias ModifierRel = rel[Entity entity, Modifier modifier];

anno BindingRel Resource@types; // (loc x type)
anno BindingRel Resource@methods; //  (loc x method)
anno BindingRel Resource@constructors; // (loc x constructor)
anno BindingRel Resource@fields; // (loc x field)
anno BindingRel Resource@variables; // (loc x variable) (local variables and method parameters)
anno BindingRel Resource@packages; // (loc x package)

anno ModifierRel Resource@modifiers;

anno EntityRel  Resource@implements; // (class x interface)
anno EntityRel  Resource@extends; //  (class x class)
anno EntitySet  Resource@declaredTopTypes; // (type) (top classes)
anno EntityRel  Resource@declaredSubTypes; // (type x type) (innerclasses)
anno EntityRel  Resource@declaredMethods; // (type x method)
anno EntityRel  Resource@declaredFields; // (type x field)
anno EntityRel  Resource@calls; // (method x method) union (type x method) (for field initializations)

@doc{import JDT facts from a file or an entire project}
@javaClass{org.meta_environment.rascal.eclipse.lib.JDT}
public Resource java extractClass(loc file);

public Resource extractProject(loc project) {
  return unionFacts(getProject(project), { extractClass(file) | loc file <- files(project), file.extension == "java" });
}

@doc{extract facts from projects}
public Resource extractFacts(Resource top, set[loc] projects) {
  return unionFacts(top, { facts | loc project <- projects, Resource facts <- extractProject(project)});
}

@doc{extracts facts from projects and all projects they depends on (transitively)}
public Resource extractFactsTransitive(loc project) {
  return extractFacts(extractProject(project), dependencies(project));
}

@doc{
	Union fact maps. Union values for facts that appear in both maps (if possible)
    TODO: implement in Java to generalize over value types
    Will collect all facts on r1 and r2 and annotate r1 with the union
}
private Resource unionFacts(Resource r1, Resource r2) {
    m1 = getAnnotations(r1);
    m2 = getAnnotations(r2);
    
	for (s <- domain(m1) & domain(m2)) {
		if (BindingRel br1 := m1[s] && BindingRel br2 := m2[s]) {
			m1[s] = br1 + br2;
		}
		else if (EntityRel ti1 := m1[s] && EntityRel ti2 := m2[s]) {
			m1[s] = ti1 + ti2;
		}
		else if (EntitySet si1 := m1[s] && EntitySet si2 := m2[s]) {
			m1[s] = si1 + si2;
		}
	}
	
	m1 += ( s:m2[s] | s <- domain(m2) - domain(m1) );

	return setAnnotations(r1, m1);
}

private Resource unionFacts(Resource receiver, set[Resource] facts) {
	for (Resource fact <- facts) {
		receiver = unionFacts(receiver, fact);
	}
	
	return receiver;
}

@doc{
  Compose two relations by matching JDT locations with Rascal locations.
     
  Returns a tuple with the composition result and the locations that could not be matched
}
public tuple[rel[&T1, &T2] found, rel[loc, &T2] notfound] matchLocations(rel[&T1, loc] RSClocs, rel[loc, &T2] JDTlocs) {

  rel[&T1, &T2] found = {};
  BindingRel notfound = {};

  for ( jdtTup <- JDTlocs, <loc jl, &T2 v2> := jdtTup ) {
    rel[&T1, &T2] search = { <v1, v2> | <&T1 v1, loc rl> <- RSClocs,
      rl.url == jl.url, rl.offset == jl.offset, rl.length == jl.length };

    if (search != {}) {
      found += search;
    } else {
      // If a declaration is preceded by Javadoc comments, the JDT parser includes them
      // in the location info of the declaration node. Then the node's location doesn't
      // match with 'ours'. Here we try to find the longest location that ends at the same
      // position as the JDT node, but starts after the JDT node's offset position.
        
      int closest = jl.offset + jl.length;
      &T1 candidate;

      for ( <&T1 v1, loc rl> <- RSClocs ) {
        if (rl.url == jl.url && rl.offset + rl.length == jl.offset + length && rl.offset > jl.offset) {
          if (rl.offset < closest) {
            closest = rl.offset;
            candidate = v1;
          }
        }
      }
      
      if (closest != jl.offset + jl.length) {
        found += {<candidate, v2>};        
      } else {
        notfound += jdtTup;
      }
    }
  }

  return <found, notfound>;
}