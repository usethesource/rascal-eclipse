module SourceEditor

import ParseTree;

@reflect{Use the evaluator to parse editor contents and apply functions to parse trees}
@doc{This temporarily registers an extension with a parser for Eclipse}
@javaClass{org.rascalmpl.eclipse.library.SourceEditor}
public void java registerLanguage(str name, str extension, type[&T] start);

@doc{This temporarily registers an extension with a parser for Eclipse}
@javaClass{org.rascalmpl.eclipse.library.SourceEditor}
public void java registerAnnotator(str name, (&T<:Tree) (&T<:Tree input) annotator);