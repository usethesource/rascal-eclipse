@license{
  Copyright (c) 2009-2012 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Mike Bierlee - mike.bierlee@lostmoment.com}
module util::ContentCompletion

import String;
import vis::Figure;
import vis::Render;
import List;
import ParseTree;

public str numeric 		= "0123456789";
public str alphabetLow 	= "abcdefghijklmnopqrstuvwxyz";
public str alphabetUp 	= "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
public str alphaNumeric = numeric + alphabetLow + alphabetUp;

@doc {
Synopsis: Completion datatype returned to the editor.

Description:
Completion proposals are passed on to the editor.
* A source proposal (/*1*/) is completed using the newText argument.
* A source proposal with a proposal string (/*2*/) is also completed with the newText argument 
	but the proposal string is displayed. This can be used to display extra information
	such as type information.
* An error proposal (/*3*/) is used to notify the user of problems encountered during the process of
	creating proposals. By default the framework will display an errorProposal stating "no
	propsals available" when you return no proposals.
}
data CompletionProposal 
				= sourceProposal(str newText) /*1*/
				| sourceProposal(str newText, str proposal) /*2*/
				| errorProposal(str errorText) /*3*/
				;

@doc {Helper datatype for storing symbol information.}				
data SymbolTree
				= symbol(str name, str symbolType)
				| symbol(str name, str symbolType, map[str, str] attributes)
				| scope(SymbolTree scopeSymbol, list[SymbolTree] children)
				| scope(list[SymbolTree] children)
				;
				
anno str SymbolTree @ label;
anno loc SymbolTree @ location;

@doc{
Synopsis: Create proposals from the symboltree from the label annotations. 

Description:
Create proposals from the symboltree from the label annotations.
The newText property of a proposal will be the symbol's name, the proposal text shown will be the label.
Remember to set the "label" annotation on the tree and all children.

Examples:
<screen>
import ParseTree;
import Type;
import util::ContentCompletion;
SymbolTree symbol = symbol("main", "method", ());
symbol@label = "main (Entry Point)";
createProposalsFromLabels(symbol);
</screen>
}
public list[CompletionProposal] createProposalsFromLabels(SymbolTree tree) {
	list[CompletionProposal] proposals = [];
	visit(tree) {
		case sym:symbol(str name, _): if (!isEmpty(sym@label)) proposals += sourceProposal(name, sym@label);
		case sym:symbol(str name, _, _): if (!isEmpty(sym@label)) proposals += sourceProposal(name, sym@label);
		case scop:scope(SymbolTree scopeSymbol, _): if (!isEmpty(scop@label)) proposals += sourceProposal(scopeSymbol.name, scop@label);
		case scop:scope(_): if (!isEmpty(scop@label)) proposals += sourceProposal(scop@label);
	}	
	return proposals;
}

@doc {Create proposals from a list of symbols using their label annotations.}
public list[CompletionProposal] createProposalsFromLabels(list[SymbolTree] tree) {
	list[CompletionProposal] proposals = [];
	for (SymbolTree symbol <- tree) {
		proposals += createProposalsFromLabels(symbol);
	}
	return proposals;
}

@doc{Create proposals from the names of symbols within a symboltree. The proposal text will simply be the name of the symbol.}
public list[CompletionProposal] createProposalsFromNames(SymbolTree tree) {
	list[CompletionProposal] proposals = [];
	visit(tree) {
		case symbol(str name, _): proposals += sourceProposal(name);
		case symbol(str name, _, _): proposals += sourceProposal(name);
	}	
	return proposals;
}

@doc{Create proposals in the form of "Name - SymbolType" as their proposal text.}
public list[CompletionProposal] createDefaultProposals(SymbolTree tree) {
	list[CompletionProposal] proposals = [];
	visit(tree) {
		case symbol(str name, str symType): proposals += sourceProposal(name, "<name> - <symType>");
		case symbol(str name, str symType, _): proposals += sourceProposal(name, "<name> - <symType>");
	}
	return proposals;
}

@doc{Create proposals in the form of "Name - SymbolType" as their proposal text.}
public list[CompletionProposal] createDefaultProposals(list[SymbolTree] symbols) {
	list[CompletionProposal] proposals = [];
	for (SymbolTree symbol <- symbols) {
		proposals += createDefaultProposals(symbol);
	}
	return proposals;
}

@doc{Removes the scopesymbol from a scope and replaces it with an anonymous scope.}
public SymbolTree excludeScopeSymbol(scope(_, children)) = scope(children);

@doc{
Synopsis: Compares completion proposals for the purpose of alphabetical ordering. 

Description: 
Compares completion proposals for the purpose of alphabetical ordering.
A proposal's newText property is used to decide order. 
Usable in the list sort function, see [List/sort].

Examples:
<screen>
import util::ContentCompletion;
import List;
list[CompletionProposal] proposals = [sourceProposal("banana"), sourceProposal("apple")];
sort(proposals, lessThan);
</screen>
}
public bool lessThan(CompletionProposal lhs, CompletionProposal rhs) = toLowerCase(lhs.newText) < toLowerCase(rhs.newText);

@doc {
Synopsis: Allows a list of proposals to be filtered based on a given prefix string.

Description:
Allows a list of proposals to be filtered based on a given prefix string.
A list of proposals will be returned that contain the prefix string anywhere in the proposals' newText property.
If the prefix string is empty, the complete list will be returned as-is.

Examples:
<screen>
import util::ContentCompletion;
list[CompletionProposal] proposals = [sourceProposal("apple"), sourceProposal("pineapple"), sourceProposal("banana")];
filterPrefix(proposals, "apple");
</screen>
}
public list[CompletionProposal] filterPrefix(list[CompletionProposal] proposals, str prefix) 
	= !isEmpty(prefix) 
	? [proposal | CompletionProposal proposal <- proposals, contains(toLowerCase(proposal.newText), toLowerCase(prefix))]
	: proposals;
	
@doc {
Synopsis: Filter a symboltree based on offset location.

Description:
Filters a symboltree by removing scope children if they are not visible from the offset location. 
Annonymous scopes will be emptied and scopes with scopesymbols will be replaced by the scopesymbol, removing the children.
Remember to set the @location annotation on ALL symbols in the tree.
}
public SymbolTree filterScopeByPosition(SymbolTree tree, int offset) {
	return top-down visit(tree) {
		case scope : scope(_): if (!isWithin(scope, offset)) {
			scope.children = [];
			insert scope;
		}
		case scope : scope(SymbolTree scopeSym, _): if (!isWithin(scope, offset)) insert scopeSym; 
	}
}

@doc {
Synopsis: Filter a symboltree based on a type filter.

Description:
The symboltree will be filtered on type. An empty list of types will return the whole tree as-is.
Scopes with scopesymbols will be excluded along with their children, even if those children are supposed to be included.
}
public SymbolTree filterScopeByType(scop : scope(SymbolTree scopeSymbol, _), list[str] types) {
	if (isEmpty(types)) return scop;
	
	switch(scopeSymbol) {
		case symbol(_, str symbolType): if (symbolType in types) {
			scop.children = filterSymbolsByType(scop.children, types);
			return scop;
		}
		case symbol(_, str symbolType, _): if (symbolType in types) {
			scop.children = filterSymbolsByType(scop.children, types);
			return scop;
		}
	}	
	
	return scope([]);
}

@doc {Filter anonymous scopes based on type. The children will be filtered.}
public SymbolTree filterScopeByType(scop : scope(list[SymbolTree] children), list[str] types) {
	if (isEmpty(types)) return scop;
	
	scop.children = filterSymbolsByType(scop.children, types);
	return scop;
}

@doc {Filter a list of symbols based on type. Scopes will be filtered using filterScopeByType.}
public list[SymbolTree] filterSymbolsByType(list[SymbolTree] symbols, list[str] types) {	
	if (isEmpty(types)) return symbols;
	
	list[SymbolTree] filteredSymbols = [];
	for (SymbolTree symbol <- symbols) {
		switch(symbol) {
			case symbol(_, str symbolType): if (symbolType in types) filteredSymbols += symbol;
			case symbol(_, str symbolType, _): if (symbolType in types) filteredSymbols += symbol;
			case scop : scope(_): filteredSymbols += filterScopeByType(scop, types);
			case scop : scope(_,_): filteredSymbols += filterScopeByType(scop, types);
			default: filteredSymbols += symbol;
		}
	}	
	return filteredSymbols;
}

@doc {Traverses a symboltree and flattens it into a list, removing all scopes.}
public list[SymbolTree] flattenTree(SymbolTree tree) {
	list[SymbolTree] symbols = [];
	
	switch(tree) {
		case s : symbol(_, _): symbols += s;
		case s : symbol(_, _, _): symbols += s;
		case s : scope(list[SymbolTree] children): {
			for (SymbolTree sym <- children) {
				symbols += flattenTree(sym);
			}
		}
		case s : scope(SymbolTree scopeSymbol, list[SymbolTree] children): {
			symbols += flattenTree(scopeSymbol);
			for (SymbolTree sym <- children) {
				symbols += flattenTree(sym);
			}
		}
	}
	
	return symbols;
}

@doc {Check whether a symbol is within a given offset. Requires the @location annotation to be set.}
public bool isWithin(SymbolTree symbol, int offset) {
	return isWithin(symbol@location, offset);
}

@doc {Check whether a parsetree node is within a given offset.}
public bool isWithin(Tree treeNode, int offset) {	
	return isWithin(treeNode@\loc, offset);
}

@doc {Check whether a given offset is within a location's range}
public bool isWithin(loc location, int offset) {
	if (offset < 0) return false;
	int begin = location.offset;
	int end = begin + location.length;	
	return begin <= offset && end > offset;
}

@doc {Visualizes a given SymbolTree}
public void visualizeTree(SymbolTree symtree) {
	render("Symbol Tree", createTreeFig(symtree));
}

@doc {Creates a renderable Figure of the given SymbolTree.}
public Figure createTreeFig(SymbolTree symtree) {
	switch(symtree) {
		case scope(list[SymbolTree] children): return tree(box(text("Anonymous Scope")), [createTreeFig(sym) | sym <- children], std(gap(20)));
		case scope(SymbolTree scopeSym, list[SymbolTree] children): return tree(createTreeFig(scopeSym), [createTreeFig(sym) | sym <- children], std(gap(20)));
		case symbol(str name, str \type, _): return box(text("<name> (<\type>)"));
		case symbol(str name, str \type): return box(text("<name> (<\type>)"));
		default: return box();
	}
}
