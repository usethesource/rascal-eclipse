@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI}
module util::ParseTreeUI

import ParseTree;
import vis::Figure;
import vis::Render;

public void ambiguityMap(Tree t) {
  render(vcat(ambMap(markVertical(t))));
}

anno bool Tree@vertical;

@doc{annotates each tree iff it contains a newline}
Tree markVertical(Tree t) {
  return visit (t) {
     case Tree u:appl(_,[_*, Tree v, _*]) : if (v@vertical?false) insert u[@vertical=true]; else fail;
     case Tree u:amb({_*, Tree v, _*})    : if (v@vertical?false) insert u[@vertical=true]; else fail;
     case char(10) : return t[@vertical=true];
     case Tree u   : return u[@vertical=false];
  }
}

list[Figure] ambMap(appl(Production p, list[Tree] args)) {
  list[list[Tree]] blocks = [];
  
   while ([list[Tree] pre, Tree t, list[Tree] post] := args, t@vertical) {
     blocks += [pre + t];
     args = post;
   };
   
   blocks += args;
   
   return box(vcat([hcat([ambMap(h) | h <- v]) | v <- blocks]), gap(5));
}