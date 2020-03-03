@license{
  Copyright (c) 2009-2015 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Tijs van der Storm - Tijs.van.der.Storm@cwi.nl}

module demo::lang::saf::Implode

import demo::lang::saf::AST;

import ParseTree;

// start[lang::saf::SAF::Fighter] 
public demo::lang::saf::AST::Fighter implode(Tree pt) 
  = implode(#demo::lang::saf::AST::Fighter, pt);

