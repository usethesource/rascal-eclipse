module util::Prompt

import String;

@javaClass{org.rascalmpl.eclipse.library.util.scripting.Prompt}
public str java prompt(str msg); 

public int promptForInt(str msg) {
  return toInt(prompt(msg));
}

public real promptForReal(str msg) {
  return toReal(prompt(msg));
}