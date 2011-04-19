@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI}
module lang::sdf2::Plugin

import lang::sdf2::syntax::Sdf2;
import util::IDE;
import ParseTree;

public Module parseModule(str input, loc l) {
  return parse(#Module, input, l);
}

public Module parseDef(str input, loc l) {
  return parse(#SDF, input, l);
}

public int main() {
  registerLanguage("SDF2 module", "sdf", parseModule);
  registerLanguage("SDF2 definition", "def", parseDef);
  return 0;
}