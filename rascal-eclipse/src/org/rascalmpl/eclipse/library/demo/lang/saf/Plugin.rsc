@license{
  Copyright (c) 2009-2013 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Tijs van der Storm - Tijs.van.der.Storm@cwi.nl}
@contributor{Atze van der Ploeg - Atze.van.der.Ploeg@cwi.nl}

module demo::lang::saf::Plugin

import util::IDE;
import util::Prompt;
import demo::lang::saf::SAF;
import demo::lang::saf::Parse;
import demo::lang::saf::Implode;
import demo::lang::saf::Check;
import demo::lang::saf::Run;
import demo::lang::saf::DumpXML;
import lang::xml::DOM;
import ParseTree;
import IO;

public void main() {
  registerLanguage("SAF", "saf", Tree(str src, loc l) { return parse(src, l); });
  registerAnnotator("SAF", checkSAF);
  registerOutliner("SAF", outlineSAF);
  registerContributions("SAF", {
    popup(menu("Super Awesome Fighters",     
      [action("Game on!", gameOn),
       action("Dump XML", dumpXML)])
    )
  });
}

// Anno declared on Tree does not work on syntax type start[Fighter]
private Tree checkSAF(Tree fighter) {
  ast = implode(fighter);
  errs = check(ast);
  fighter@messages = errs;
  return fighter;
}

private void gameOn(start[Fighter] fighter, loc _) {
  f1 = implode(fighter);
  path = prompt("Opponent: (include projectname and path)             ");
  f2 = implode(parse(|project://<path>|));
  game(f1, f2);
}

private void dumpXML(start[Fighter] fighter, loc _) {
  ast = implode(fighter);
  xml = saf2xml(ast);
  path = prompt("Project path to dump XML to: ");
  writeFile(|project://<path>|, xmlPretty(xml));
}

private node outlineSAF(start[Fighter] fighter) {
  // .specs (or any field) does not work start[Fighter]
  // <- on start does not work either
  attrs = ["attr"()[@label="<s.name>: <s.strength>"][@\loc=s@\loc] | /Spec s := fighter, s is attribute ];
  behvs = ["behv"(
         "move"()[@label="move: <s.move>"][@\loc=((s.move)@\loc)],
         "fight"()[@label="fight: <s.fight>"][@\loc=((s.fight)@\loc)]
      )[@label="<s.cond>"][@\loc=s@\loc] | /Spec s := fighter, s is behavior ];
  name = ( "" | "<f.name>" | /Fighter f := fighter );
  return "fighter"(
            "attrs"(attrs)[@label="Strengths"],
            "behvs"(behvs)[@label="Behaviors"]
          )[@label="<name>"][@\loc=fighter@\loc];
}

