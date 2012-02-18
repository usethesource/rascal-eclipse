@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI}
@contributor{Davy Landman - Davy.Landman@cwi.nl - CWI}

@doc{
Synopsis: IDE module
}
module util::IDE

// Especially annotations defined in this module are relevant for util::IDE
import ParseTree;
import vis::Figure;

@doc{
Synopsis: Data type to describe contributions to the menus of the IDE.

Pitfalls:

This data type is not yet complete.

The categories do not support changing the font name or the font size.
}
data Contribution 
     = popup(Menu menu)
     | menu(Menu menu)
     | categories(map[str categoryName, FontProperties fontStyle] styleMap)
     ;
  
data Menu 
     = action(str label, void (Tree tree, loc selection) action)
     | action(str label, void (str selection, loc selection) handler) // for non rascal menu's
     | edit(str label, str (Tree tree, loc selection) edit)
     | group(str label, list[Menu] members)
     | menu(str label, list[Menu] members)
     ;
  

@doc{
Synopsis: Annotate an outline node with a label.
}

anno str node@label;

@doc{
Synopsis: Annotate an outline node with a link.
}
anno loc node@\loc;  // a link for an outline node


@doc{
Synopsis: Register a language extension and a parser for use in Eclipse.
}
@reflect{Use the evaluator to parse editor contents and apply functions to parse trees}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public java void registerLanguage(str name, str extension, Tree (str input, loc origin) parse);

@doc{
Synopsis: Register an annotator.

Description:

  Register a tree processor for annotating a tree with [$ParseTree/doc],
  [$ParseTree/link], or [$ParseTree/message]
  annotations. See [ParseTree] for available annotations. The annotations are
  processed by the editor to generate visual effects such as error markers, hyperlinks
  and documentation hovers.
}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public java void registerAnnotator(str name, (&T<:Tree) (&T<:Tree input) annotator);

@doc{
Synopsis: Register an outliner.

Description:

Register an outliner function. An outliner maps a parse tree to a simpler
tree that summarizes the contents of a file. This summary is used to generate the outline
view in Eclipse. 

Use the  [$IDE/label], [$IDE/loc] and [$IDE/image] annotations on each node to guide how each outline
item is displayed, which item it links to and what image is displayed next to it.
}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public java void registerOutliner(str name, node (&T<:Tree input) outliner);

@doc{

Synopsis: Register contributions to Eclipse menus.

Description:

Register a number of contributions to the menus of the IDE.
}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public java void registerContributions(str name, set[Contribution] contributions);

@doc{
Synopsis: Clear all registered languages.

Description:
Remove all registered languages.

Pitfalls:
Use with caution! This will clear all registered languages (for debugging purposes).
}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public java void clearLanguages();

@doc{Synopsis: Clear a registered language.

Description:
Remove a registered language.

Pitfalls:
Use with caution! This will clear a registered language (for debugging purposes).
}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public java void clearLanguage(str name);

@doc{
Synopsis: Register contributions to menus of a non-Rascal editor.

Description:
Register a number of contributions to the menus of a non-Rascal code editor:
* `name`: eclipse editor id
* `contributions`: (edit is not supported), and Tree parameter of the callback will be empty).
}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public java void registerNonRascalContributions(str name, set[Contribution] contributions);

@doc{
Synopsis: Clear all non-Rascal IDE contributions.
}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public java void clearNonRascalContributions();

@doc{
Synopsis: Clear all non-Rascal IDE contributions for a specific editor.

Description:

* `name`: Eclipse editor id.
}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public java void clearNonRascalContribution(str name);


@doc{
Synopsis: Create a console with a Rascal handler.
}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public java void createConsole(str name, str startText, str (str) newLineCallback);
