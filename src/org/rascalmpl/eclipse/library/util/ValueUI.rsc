@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI}
module util::ValueUI

import vis::Figure;
import vis::Render;
import Node;
import Map;

@javaClass{org.rascalmpl.eclipse.library.util.ValueUI}
@doc{Starts an editor with an indented textual representation of any value}
public java void text(value v, int indent);

@doc{Starts an editor with an indented textual representation of any value}
public void text(value v) {
  text(v, 2);
}

@javaClass{org.rascalmpl.eclipse.library.util.ValueUI}
@doc{Starts a tree view with a node for each nested value in a value}
public java void tree(value v);

@doc{Displays any value as a set of nested figures. EXPERIMENTAL!}
public void graph(value v) {
  render(toGraph(v));
}

Figure toGraph(value v) {
 props = [hcenter(),vcenter(),gap(5),width(0),height(0)];
 
  switch (v) {
    case bool b : return text("<b>");
    case num i  : return text("<i>");
    case str s  : return text("<s>");
    case list[value] l : return box(pack([toGraph(e) | e <- l]), props);
    case rel[value from, value to] r : {
      int next = 0;
      int id() { next = next + 1; return next; }
      ids = ( e : "<id()>" | value e <- (r.from + r.to) );
      return box(graph([box(toGraph(e),[id(ids[e])] + props) | e <- ids],
                   [edge(ids[from],ids[to]) | from <- r.from, to <- r[from]],hint("layered"),width(800),height(600)));
    }
    case set[value] l :  return box(pack([toGraph(e) | e <- l]),[hcenter(),vcenter()]);
    case node x :  return box(vcat([text(getName(x),fontSize(20)), vcat([toGraph(c) | c <- x ])]),props);
    case map[value,value] x : return box(vcat([hcat([toGraph(key), toGraph(x[key])]) | key <- x],props)); 
    case tuple[value a, value b] t: return box(hcat([toGraph(t.a), toGraph(t.b)]),props);
    case tuple[value a, value b, value c] t: return box(hcat([toGraph(t.a), toGraph(t.b), toGraph(t.c)]),props);
    default: return text("<v>");
  }
}