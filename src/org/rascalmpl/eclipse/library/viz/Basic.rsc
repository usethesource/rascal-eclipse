module viz::Basic

// Various views on values

@doc{Show the string representation of a value in a text editor}
@javaClass{org.rascalmpl.eclipse.library.viz.Basic}
public void java graphView(value v);

@doc{Show any value as a hierarchical graph}
@javaClass{org.rascalmpl.eclipse.library.viz.Basic}
public void java textView(value v);

@doc{Show a collapsable tree of a value}
@javaClass{org.rascalmpl.eclipse.library.viz.Basic}
public void java treeView(value v);


