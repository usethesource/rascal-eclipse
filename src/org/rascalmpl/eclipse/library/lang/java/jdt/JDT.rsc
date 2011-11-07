@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{}
@contributor{Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI}
@contributor{Bas Basten - Bas.Basten@cwi.nl (CWI)}
@contributor{Jouke Stoel - Jouke.Stoel@cwi.nl (CWI)}
@contributor{Mark Hills - Mark.Hills@cwi.nl (CWI)}
@contributor{Arnold Lankamp - Arnold.Lankamp@cwi.nl}
module lang::java::jdt::JDT

import Map;
import Node;
import util::Resources;
import lang::java::jdt::Java;
import lang::java::jdt::JavaADT;

@doc{maps any ast at a certain location to a qualified name}
public alias BindingRel = rel[loc, Entity];

@doc{relationship between entities}
public alias EntityRel = rel[Entity from, Entity to];

@doc{collection of entities}
public alias EntitySet = set[Entity];

@doc{maps an entity to its modifiers}
public alias ModifierRel = rel[Entity entity, Modifier modifier];

public alias AstNodeRel = rel[Entity method, AstNode body];

@doc{contains all type declarations and uses}
anno BindingRel Resource@types;        

@doc{contains all method declarations and uses}
anno BindingRel Resource@methods;      

@doc{contains just method declarations}
anno BindingRel Resource@methodDecls;

@doc{contains all constructor declarations}
anno BindingRel Resource@constructors; 

@doc{contains all field declarations and uses}
anno BindingRel Resource@fields;       

@doc{contains just field declarations}
anno BindingRel Resource@fieldDecls;

@doc{contains all local variable and method parameter declarations}
anno BindingRel Resource@variables;    

@doc{contains all package declarations} 
anno BindingRel Resource@packages;     

@doc{contains all class declarations}
anno BindingRel Resource@classes;

@doc{maps Entities to the modifiers that have been declared for it}
anno ModifierRel Resource@modifiers; 

@doc{defines which types implement which interfaces}
anno EntityRel  Resource@implements;   

@doc{defines which classes extends which other classes}
anno EntityRel  Resource@extends;      

@doc{defines which top-level classes are declared}
anno EntitySet  Resource@declaredTopTypes; 

@doc{defines which inner classes are declared}
anno EntityRel  Resource@declaredSubTypes; 

@doc{defines which class defines which methods}
anno EntityRel  Resource@declaredMethods;  

@doc{defines the relation between a method and its body}
anno AstNodeRel	Resource@methodBodies;

@doc{defines which class defines which fields}
anno EntityRel  Resource@declaredFields;   

@doc{defines which methods call which other methods, and which class initialization code calls which methods}
anno EntityRel  Resource@calls;

@doc{import JDT facts from a Java file}
@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.internal.JDT}
@reflect
public java Resource extractClass(loc file);

@doc{import JDT facts from a file or an entire project}
public Resource extractProject(loc project) {
  return extractResource(getProject(project));
}

@doc{import JDT facts from a project, file or folder}
public Resource extractResource(Resource res) {
  if (res.id?) {
    if (res.contents?) {
      return extractResources(res, res.contents);
    } else {
      loc file = res.id;
      if (file.extension == "java" && isOnBuildPath(file)) {
        return extractClass(file);
      }
    }
  }
  return res;
}

@doc{import JDT from a set of resources}
private Resource extractResources(Resource receiver, set[Resource] res) {
  return unionFacts(receiver, { extractResource(r) | r <- res });
}

@doc{extracts facts from projects and all projects they depends on (transitively)}
public Resource extractFactsTransitive(loc project) {
  return extractResources(extractProject(project), { getProject(p) | p <- dependencies(project) });
}

@doc{checks if a Resource is in its project's build path}
@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.internal.JDT}
public java bool isOnBuildPath(loc file);

@doc{
	Union fact maps. Union values for facts that appear in both maps (if possible)
    TODO: implement in Java to generalize over value types
    Will collect all facts on r1 and r2 and annotate r1 with the union
}
public Resource unionFacts(Resource r1, Resource r2) {
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
		else if (ModifierRel mr1 := m1[s] && ModifierRel mr2 := m2[s]) {
			m1[s] = mr1 + mr2;
		}
		else if (AstNodeRel anr1 := m1[s] && AstNodeRel anr2 := m2[s]) {
			m1[s] = anr1 + anr2;
		}
	}
	
	m1 += ( s:m2[s] | s <- domain(m2) - domain(m1) );

	return setAnnotations(r1, m1);
}

public Resource unionFacts(Resource receiver, set[Resource] facts) {
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
  rel[loc, &T2] notfound = {};

  for ( jdtTup <- JDTlocs, <loc jl, &T2 v2> := jdtTup ) {
    rel[&T1, &T2] search = { <v1, v2> | <&T1 v1, loc rl> <- RSClocs,
      rl.uri == jl.uri, rl.offset == jl.offset, rl.length == jl.length };

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
        if (rl.uri == jl.uri && rl.offset + rl.length == jl.offset + length && rl.offset > jl.offset) {
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
