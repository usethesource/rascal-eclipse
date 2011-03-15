module lang::sdf2::Plugin

import lang::sdf2::syntax::Sdf2;
import SourceEditor;
import ParseTree;

public Module parseModule(str input, loc l) {
  return parse(#Module, input, l);
}

public SDF parseDefinition(str input, loc l) {
  return parse(#SDF, input, l);
}

public void main() {
  registerLanguage("SDF2 Modules", "sdf", parseModule);
  registerLanguage("SDF2 Definition", "def", parseDefinition);
}