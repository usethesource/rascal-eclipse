@doc{
Synopsis: provide access to m3 ASTs for Java using Eclipse project meta-data
}
module lang::java::jdt::m3::AST

extend lang::java::m3::AST;
extend lang::java::jdt::Project;



@doc{
Synopsis: Creates ASTs from a project
}
@reflect
@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.m3.internal.EclipseJavaCompiler}
public java Declaration createAstFromEclipseFile(loc file, bool collectBindings);

@doc{
Synopsis: Creates ASTs from a project
}
@reflect
@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.m3.internal.EclipseJavaCompiler}
public java set[Declaration] createAstsFromEclipseProject(loc project, bool collectBindings);