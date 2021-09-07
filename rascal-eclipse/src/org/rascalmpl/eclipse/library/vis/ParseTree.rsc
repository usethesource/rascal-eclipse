@license{
  Copyright (c) 2009-2015 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Paul Klint - Paul.Klint@cwi.nl - CWI}
module vis::ParseTree

// Visualization of ParseTrees

import lang::rascal::format::Grammar;
import lang::rascal::format::Escape;
import vis::Figure;
import vis::Render;

import ParseTree;
import String;
import ValueIO;
import Set;

public void renderParsetree(Tree t){
	render(space(visParsetree(t),std(gap(4,15)),std(resizable(false))));
}

public Figure visParsetree(Tree t){
  switch(t){
   
   case b:appl(Production prod, list[Tree] args) : {
   		if(\lex(_) := prod.def){
   			return box(text(unparse(b)),size(5));
   		}
   		if(prod.def has string){
   			return box(text(prod.def.string),size(5));
   		}
   		if(\layouts(_) := prod.def){
   			return box(size(5),fillColor("grey"),popup(unparse(b)));
   		}
	     FProperty p = popup(topProd2rascal(prod));
	     return tree(ellipse(size(5),p),[visParsetree(c) | c <- args]);
     }
     
     case amb(set[Tree] alternatives):{
         FProperty p = popup("Ambiguous: <size(alternatives)>");
        // viewTrees(root, toList(alternatives));
         return tree(ellipse(size(10), fillColor("red"), p),[visParsetree(c) | c <- alternatives]);
      }
     
    case char(int c) : {
        return  box(text(escape(stringChar(c)), fontColor("blue")));
    }

    case cycle(Symbol symbol, int cycleLength) : {
         FProperty p = popup("Cycle-<cycleLength> of <symbol>");
         return tree(ellipse(size(10), fillColor("yellow"), p), []);
    }
  }
  throw "viewTree1: missing case for: <t>";
}

private FProperty popup(str s){
	return mouseOver(box(text(s), grow(1.2), resizable(false),fillColor("yellow")));
}

public FProperty popup(str s){
    return mouseOver(box(text(s), grow(1.2), resizable(false), fillColor("yellow")));
}

