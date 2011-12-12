@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI}
@contributor{Mark Hills - Mark.Hills@cwi.nl (CWI)}
module lang::java::jdt::JDTRefactoring

import Map;
import Node;
import util::Resources;
import lang::java::jdt::Java;
import lang::java::jdt::JDT;
import IO;

@doc{Invokes the EncapsulateField refactoring, generating public getters and setters, on the fields at the locs in the set}
@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.internal.JDTRefactoring}
public java rel[str,str] encapsulateFields(set[int] fieldOffsetsFromLoc, loc file);

@doc{Invokes the ChangeSignature refactoring, making the method public}
@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.internal.JDTRefactoring}
public java rel[str,str] makeMethodsPublic(set[int] methodOffsetsFromLoc, loc file);

@doc{Invokes the Code CleanUp on the given file}
@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.internal.JDTRefactoring}
public java rel[str,str] cleanUpSource(loc file);

@doc{Invokes the Fully Qualify Type Names transformation on the given file}
@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.internal.JDTRefactoring}
public java str fullyQualifyTypeNames(loc file);

public void fullyQualifyTypeNamesInFile(loc file) {
        str contents = fullyQualifyTypeNames(file);
        writeFile(file,contents);
}

@doc{Remove the methods at the given locs}
@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.internal.JDTRefactoring}
public java void removeMethods(set[int] methodOffsetsFromLoc, loc file);

@doc{Unqualify names qualified using fullyQualifyTypeNames}
@javaClass{org.rascalmpl.eclipse.library.lang.java.jdt.internal.JDTRefactoring}
public java void unqualifyTypeNames(loc file);
