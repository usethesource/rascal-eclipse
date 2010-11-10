module JDTRefactoring

import Map;
import Node;
import Resources;
import Java;
import JDT;

@doc{Invokes the EncapsulateField refactoring, generating public getters and setters, on the fields at the locs in the set}
@javaClass{org.rascalmpl.eclipse.library.JDTRefactoring}
public rel[str,str] java encapsulateFields(set[int] fieldOffsetsFromLoc, loc file);

@doc{Invokes the ChangeSignature refactoring, making the method public}
@javaClass{org.rascalmpl.eclipse.library.JDTRefactoring}
public rel[str,str] java makeMethodsPublic(set[int] methodOffsetsFromLoc, loc file);

@doc{Invokes the Code CleanUp on the given file}
@javaClass{org.rascalmpl.eclipse.library.JDTRefactoring}
public rel[str,str] java cleanUpSource(loc file);

@doc{Invokes the Fully Qualify Type Names transformation on the given file}
@javaClass{org.rascalmpl.eclipse.library.JDTRefactoring}
public void java fullyQualifyTypeNames(loc file);
