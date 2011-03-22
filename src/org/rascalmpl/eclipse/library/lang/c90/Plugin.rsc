module lang::c90::Plugin

import lang::c90::syntax::C;
import util::IDE;
import ParseTree;

public TranslationUnit parseTU(str input, loc l) {
  return parse(#TranslationUnit, input, l);
}

public void main() {
  registerLanguage("C90 program", "c", parseTU);
  registerLanguage("C90 header file", "h", parseTU);
}