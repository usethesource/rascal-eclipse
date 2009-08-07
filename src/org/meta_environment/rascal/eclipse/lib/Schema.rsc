module Schema

import JDT;
import String;
import Resources;
import Java;
import List;

import IO; // TODO remove, debug!

private alias classMethods = rel[Entity class, Id method];


public classMethods buildExtendedSchema(str project, str directory, str astPackage, set[Entity] extendedClasses) {
	str prefix = getPrefix(directory, astPackage);
	list[str] astFiles = getASTFiles(project, prefix, astPackage);
	return getMethodsOfInterest(project, prefix, astFiles, getClassesOfInterest(project, prefix, astFiles) + extendedClasses);
} 

public classMethods buildSchema(str project, str directory, str astPackage) {
	str prefix = getPrefix(directory, astPackage);
	list[str] astFiles = getASTFiles(project, prefix, astPackage);
	return getMethodsOfInterest(project, prefix, astFiles, getClassesOfInterest(project, prefix, astFiles));
}

public set[Entity] createClassEntities(set[str] fullyQualifiedNames) {
	set[Entity] result = {};
	for(str fqn <- fullyQualifiedNames) {
		list[Id] ids = [];
		while(/^<pack:[^\.]*>\.<tail:.*>$/ := fqn) {
			ids += package(pack);
			fqn = tail;
		}  
		
		ids += class(fqn);
		result += entity(ids);
	}

	return result;
}

private str getPrefix(str directory, str astPackage) {
	return "/" + directory + "/" + astPackage;
}

private list[str] getASTFiles(str project, str prefix, str astPackage) {
	list[str] astFiles = [];
	str fullPath = location(project).url + prefix;
	for(loc file <- files(project)) {
		   
		if (/^.*\.java$/ := file.url) {
			if(size(file.url) > size(fullPath + ".java")) {
				if(substring(file.url, 0, size(fullPath)) == fullPath) { // TODO substring is not approved yet!
					astFiles += substring(file.url, size(fullPath));
				}
			}
		}	
	}

	return astFiles;
}

public str stripToClass(str extended) {
	if (/^.*\.<clss:[^\.]*>$/ := extended) {
		return clss;
	}
	
	return extended;
}

public void printData(str projectName, str path, classMethods datadef) {
	rel[str, str] printable = {}; 
	for(tuple[Entity class, Id method] entry <- datadef) {
		printable += < stripToClass(toString(entry.class)), stripToClass(toString(entry.method.returnType))>;
	}
	

	if(/^.*\/<home:.*\/.*>$/ := location(projectName).url) {
		printData(home + "/" + path, printable);
	}
}

private void java printData(str path, rel[str, str] datadef)
@javaClass{org.meta_environment.rascal.eclipse.lib.Schema}
;

private classMethods getMethodsOfInterest(str project, str prefix, list[str] astFiles, set[Entity] interestingReturnTypes) {;
	classMethods result = {};
	for(str file <- astFiles) {
		for(tuple[Entity path, Entity type] declMethod <- getDeclaredMethods(extractFacts(project, prefix + file))) {
			Id method = (declMethod.type.id - declMethod.path.id)[0];
			if(isMethod(method) && method.returnType in interestingReturnTypes) {				
				result += <declMethod.path, method>;
			} 
		}
	}
	
	return result;
}

public set[Entity] getClassesOfInterest(str project, str prefix, list[str] astFiles) {
	set[Entity] result = {};
	for(str file <- astFiles) {
		result += getDeclaredTopTypes(extractFacts(project, prefix + file));
	}
	
	return result;
}

private bool isMethod(Id method) {
	return !/^.*constructor(.*).*$/ := toString(method);
}
