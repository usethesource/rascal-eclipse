module View

@doc{Show any value as a hierarchical graph}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java show(value v);

@doc{Show a chart}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java chart(str label, map[str, int] v);

@doc{Show the string representation of a value in a text editor}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java edit(value v);


