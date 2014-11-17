module lang::tutor::IDE

import lang::tutor::Syntax;
import util::IDE;
import CourseManager;
import IO;
import ParseTree;
import Message;

start[ConceptFile] parser(str s, loc l) 
  = parse(#start[ConceptFile], s, l);
  
set[Message] compiler(Tree t) {
  if (start[ConceptFile] file := t) {
    save("<file.top.name>", "<t>", !exists(t@\loc[extension="html"], writeBack=false));
    return {};
  }
  return {error("Tree is a not a concept file", t@\loc)};
}
  
void main() {
  registerLanguage("Tutor", "concept", parser);
  registerContributions("Tutor", { builder(compiler) });
}