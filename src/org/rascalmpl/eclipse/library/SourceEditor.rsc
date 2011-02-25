module SourceEditor

import ParseTree;

@reflect{Use the evaluator to parse editor contents and apply functions to parse trees}
@doc{This temporarily registers an extension with a parser for Eclipse}
@javaClass{org.rascalmpl.eclipse.library.SourceEditor}
public void java registerLanguage(str name, str extension, Tree (str input) parse);

@doc{This temporarily registers an extension with a parser for Eclipse}
@javaClass{org.rascalmpl.eclipse.library.SourceEditor}
public void java registerAnnotator(str name, (&T<:Tree) (&T<:Tree input) annotator);

@doc{Use with caution! This will clear all registered languages (for debugging purposes)}
@javaClass{org.rascalmpl.eclipse.library.SourceEditor}
public void java clear();

@doc{Use with caution! This will clear a registered language (for debugging purposes)}
@javaClass{org.rascalmpl.eclipse.library.SourceEditor}
public void java clear(str name);