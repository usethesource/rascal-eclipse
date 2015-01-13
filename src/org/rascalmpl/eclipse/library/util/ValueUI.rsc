@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI}
module util::ValueUI

import util::Editors;
import vis::Figure;
import vis::Render;
import Node;
import Map;  
import List;
import Ambiguity;
import util::Clipboard;
import ParseTree;
import vis::ParseTree;
import vis::web::Chart;

@label{Visualize parse tree}
public void visPT(Tree x) {
  renderParsetree(x);
}

@label{Copy to clipboard}
public void copyToClipboard(value x) {
  copy(x);
}

@label{Open location in editor}
public void openEditor(loc l) {
  edit(l);
}

@label{Diagnose ambiguity}
public void diagnoseAmbiguity(Tree x) {
  text(diagnose(x));
}



@javaClass{org.rascalmpl.eclipse.library.util.ValueUI}
@doc{Starts an editor with an indented textual representation of any value}
@deprecated
public java void text(value v, int indent);

@doc{Starts an editor with an indented textual representation of any value}
@label{Indented value in editor}
public void text(value v, int indent=2) {
  text(v, indent);
}

@javaClass{org.rascalmpl.eclipse.library.util.ValueUI}
@doc{Starts a tree view with a node for each nested value in a value}
@label{Explorable tree view}
public java void tree(value v);

//@label{Scatter plot with histogram}
//public void scatterplot(map[num,num] data) {
//   ...
//}

@doc{Displays any value as a set of nested figures. EXPERIMENTAL!}
@label{Experimental value as graph view}
public void graph(value v) {
  render(toGraph(v));
}



Figure toGraph(value v) {
 props = [hcenter(),vcenter(),gap(5)];
 
  switch (v) {
    case bool b : return vis::Figure::text("<b>");
    case num i  : return vis::Figure::text("<i>");
    case str s  : return vis::Figure::text("<s>");
    case list[value] l : return hcat(tail([text(","),box(toGraph(e)) | e <- l]), gap(5), grow(1.1));
    case rel[value from, value to] r : {
      int next = 0;
      int id() { next = next + 1; return next; }
      ids = ( e : "<id()>" | value e <- (r.from + r.to) );
      return box(graph([box(toGraph(e),[id(ids[e])] + props) | e <- ids],
                   [edge(ids[from],ids[to],toArrow(triangle(10))) | from <- r.from, to <- r[from]],hint("layered"),gap(40)));
    }
    case set[value] l :  return box(pack([toGraph(e) | e <- l]),[hcenter(),vcenter()]);
    case node x :  return box(vcat([vis::Figure::text(getName(x),fontSize(20)), vcat([toGraph(c) | c <- x ])]),props);
    case map[value,value] x : return box(vcat([hcat([toGraph(key), toGraph(x[key])]) | key <- x],props)); 
    case tuple[value a, value b] t: return box(hcat([toGraph(t.a), toGraph(t.b)]),props);
    case tuple[value a, value b, value c] t: return box(hcat([toGraph(t.a), toGraph(t.b), toGraph(t.c)]),props);
    default: return vis::Figure::text("<v>");
  }
}

@doc{Displays (svg) any value as a set of nested figures. EXPERIMENTAL!}
@label{Experimental value as web view}
public void histogramHtml(value v) {
  switch (v) {
     case map[num, num] m: chart(m);
     case map[num, list[num]] m: chart(m);
     case map[str, num] r: chart(r);
     case map[str, list[num]] m: chart(m);
     case rel[num, num] r: chart(r);
     case rel[num, num, str] r: chart(r);
     case rel[str, str] r: chart(r);    
     case rel[loc, loc] r: chart(r);
     case rel[map[str, num], str] r:chart(r);
     case rel[map[str, num], str, str] r:chart(r);
     case rel[map[loc, num], loc] r:chart(r);
     case rel[map[loc, num], loc, str] r:chart(r);
     case rel[map[str, num], map[str, num]] r:chart(r);
     case rel[map[loc, num], map[loc, num]] r:chart(r);
     case tuple[str name, list[num] ticks, num(num) q] f: chart([f]);
     case tuple[str name, lrel[num, num] r] p: chart([p]);
     case list[tuple[str name, list[num] ticks, num(num) q]] f: chart(f);
     case list[tuple[str name, lrel[num, num] r]] p: chart(p);
     default: chartDefault("<v>");
     }    
}
