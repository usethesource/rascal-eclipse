module util::ValueUI

@javaClass{org.rascalmpl.eclipse.library.util.ValueUI}
public void java text(value v, int indent);

public void text(value v) {
  text(v, 2);
}

@javaClass{org.rascalmpl.eclipse.library.util.ValueUI}
public void java tree(value v);