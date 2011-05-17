@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI}
@contributor{Davy Landman - Davy.Landman@cwi.nl - CWI}
module util::IDE

// Especially annotations defined in this module are relevant for util::IDE
import ParseTree;

@doc{Use this type to add items to the menus of the IDE (unfinished)}
data Contribution 
  = popup(Menu menu)
  | menu(Menu menu)
  ;
  
data Menu 
  = action(str label, void (Tree tree, loc selection) action)
  | click(str label, void (loc selection) click) // for non rascal menu's
  | edit(str label, str (Tree tree, loc selection) edit)
  | group(str label, list[Menu] members)
  | menu(str label, list[Menu] members)
  ;
  

     
anno str node@label; // an String label for an outline node
anno loc node@\loc;  // a link for an outline node

@reflect{Use the evaluator to parse editor contents and apply functions to parse trees}
@doc{This registers an extension with a parser for Eclipse}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public void java registerLanguage(str name, str extension, Tree (str input, loc origin) parse);

@doc{
  This registers a tree processor for annotating a tree with doc(s), link, message(s)
  annotations, etc. See also ParseTree for available annotations. The annotations are
  processed by the editor to generate visual effects such as error markers, hyperlinks
  and documentation hovers.
}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public void java registerAnnotator(str name, (&T<:Tree) (&T<:Tree input) annotator);

@doc{This registers an outliner function. An outliner maps a parse tree to a simpler
tree that summarizes the contents of a file. This summary is used to generate the outline
view in Eclipse. 

Use the "label", "loc" and "image" annotations on each node to guide how each outline
item is displayed, which item it links to and what image is displayed next to it.
}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public void java registerOutliner(str name, node (&T<:Tree input) outliner);

@doc{This registers a number of contributions to the menus of the IDE}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public void java registerContributions(str name, set[Contribution] contributions);

@doc{Use with caution! This will clear all registered languages (for debugging purposes)}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public void java clearLanguages();

@doc{Use with caution! This will clear a registered language (for debugging purposes)}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public void java clearLanguage(str name);

@doc{This registers a number of contributions to the menus of the a non rascal code editor
	@name: eclipse editor id
	@contributions: (edit is not supported), and Tree parameter of the callback will be empty)}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public void java registerNonRascalContributions(str name, set[Contribution] contributions);

@doc{This will clear all non rascal IDE contributions.}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public void java clearNonRascalContributions();

@doc{This will clear all non rascal IDE contributions for the specified editor.
	@name: eclipse editor id}
@javaClass{org.rascalmpl.eclipse.library.util.IDE}
public void java clearNonRascalContribution(str name);
