module SourceEditor

import ParseTree;

@reflect{Use the evaluator to parse editor contents and apply functions to parse trees}
@doc{This registers an extension with a parser for Eclipse}
@javaClass{org.rascalmpl.eclipse.library.SourceEditor}
public void java registerLanguage(str name, str extension, Tree (str input, loc origin) parse);

@doc{
  This registers a tree processor for annotating a tree with doc(s), link, message(s)
  annotations, etc. See also ParseTree for available annotations. The annotations are
  processed by the editor to generate visual effects such as error markers, hyperlinks
  and documentation hovers.
}
@javaClass{org.rascalmpl.eclipse.library.SourceEditor}
public void java registerAnnotator(str name, (&T<:Tree) (&T<:Tree input) annotator);

@doc{This registers an outliner function. An outliner maps a parse tree to a simpler
tree that summarizes the contents of a file. This summary is used to generate the outline
view in Eclipse. 

Use the "label", "loc" and "image" annotations on each node to guide how each outline
item is displayed, which item it links to and what image is displayed next to it.
}
@javaClass{org.rascalmpl.eclipse.library.SourceEditor}
public void java registerOutliner(str name, node (&T<:Tree input) outliner);

anno loc node@image; // an Eclipse URI to an image (not all Rascal schemes supported!)
anno str node@label; // an String label for an outline node
anno loc node@\loc;  // a link for an outline node

@doc{Use with caution! This will clear all registered languages (for debugging purposes)}
@javaClass{org.rascalmpl.eclipse.library.SourceEditor}
public void java clear();

@doc{Use with caution! This will clear a registered language (for debugging purposes)}
@javaClass{org.rascalmpl.eclipse.library.SourceEditor}
public void java clear(str name);