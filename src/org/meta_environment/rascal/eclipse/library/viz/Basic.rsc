module viz::Basic

// Various views on values

@doc{Show the string representation of a value in a text editor}
@javaClass{org.meta_environment.rascal.eclipse.library.viz.Basic}
public void java graphView(value v);

@doc{Show any value as a hierarchical graph}
@javaClass{org.meta_environment.rascal.eclipse.library.viz.Basic}
public void java textView(value v);

@doc{Show a collapsable tree of a value}
@javaClass{org.meta_environment.rascal.eclipse.library.viz.Basic}
public void java treeView(value v);


