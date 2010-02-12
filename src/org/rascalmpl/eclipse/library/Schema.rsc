module Schema

import JDT;
import Java;
import String;
import Map;
import Relation;
import Set;
import Resources;
import List;

import IO; // TODO remove, debug
import SchemaTelemetry; // TODO remove, just for acquiring metadata

public alias EntityMap = map[Entity fqn, str mapping];
public alias Nodes = rel[Entity type, FactMap facts, rel[str, Entity] methods, set[Entity] children]; 

public alias Additions = tuple[map[tuple[str fqn, list[int]] tup, str mapping] extraRTs, list[str] extraMeths];
public alias Filters = tuple[set[str] exclFilePatterns, set[str] inclMethPatterns, set[str] exclMethPatterns];


/*---------------------------------------------------------------------*\
|                              PUBLIC SECTION                           |
\*---------------------------------------------------------------------*/

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
*                                                                       *
*                              JAVA LINKS                               *
*                                                                       *
\* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

private set[str] java getASTFiles(str path)
@javaClass{org.rascalmpl.eclipse.lib.Schema}
;

private void java printData(str path, str modulename, list[str] datadef)
@javaClass{org.rascalmpl.eclipse.lib.Schema}
;

private set[str] java getCompliantSet(set[str] universe, str searchString)
@javaClass{org.rascalmpl.eclipse.lib.Schema}
;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
*                                                                       *
*                              MAPPING                                  *
*                                                                       *
\* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

// basic rascal mapping values
public str BOOL = "bool";
public str INT = "int";
public str REAL = "real";
public str STR = "str";
public str LOC = "loc";
public str TUPLE = "tuple[value,value]";
public str LIST = "list[value]";
public str SET = "set[value]";
public str REL = "rel[value,value]";
public str MAP = "map[value,value]";
public str NODE = "node()";


public int PACKAGE = 0;
public int CLASS = 1;
public int INTERFACE = 2;
public int ENUM = 3;


// for some less common mapping values
public str getTuple(int arity) {
	return "tuple[" + getFilling(arity) + "]";
}

public str getRel(int arity) {
	return "rel[" + getFilling(arity) + "]";
}

public str getNode(int arity) {
	return "node(" + getFilling(arity) + ")";
}

private str getFilling(int arity) { 
	str result = "";
	int i = 0;
	while (i < arity) {
		if (i > 0) {
			result += ",";
		}
		result += "value";
		i += 1;
	} 
	result += "]";
	
	return result;	
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
*                                                                       *
*                              ENTRYPOINTS                              *
*                                                                       *
\* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

public void buildDataScheme(str astPackagePath, str newModulePath) {
	buildDataScheme(astPackagePath, newModulePath, <{},{".*"},{}>, <(),[]>); //include all, exclude nothing, add nothing 
}

// TODO remove! temporarily returns string for metadata
public str buildDataScheme(str astPackagePath, str newModulePath, Filters fs, Additions ads) {	
	return toFile(newModulePath, astPackagePath, buildDataSchemeHalfway(astPackagePath, fs, ads), ads); 	
}

public Nodes buildDataSchemeHalfway(str astPackagePath, Filters fs, Additions ads) {
	return buildDataSchemeHierarchically(getASTFiles(astPackagePath), fs, convertToEntities(domain(ads.extraRTs)));  
}

/*---------------------------------------------------------------------*\
|                              PRIVATE SECTION                          |
\*---------------------------------------------------------------------*/

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
*                                                                       *
*                              FIND CHILDREN                            *
*                                                                       *
\* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

// builds the node(child, child, ...) relation top-down. i.e. from classes that do not inherit from any 
// class within the package to the classes that do not have any classes that inherit from them 
// within the package
private Nodes buildDataSchemeHierarchically(set[str] astFiles, Filters fs, set[Entity] additionaltypes) {
	rel[Entity, FactMap] alltypes = getAllTypes(astFiles, fs.exclFilePatterns);
	rel[Entity, Entity] subtypes = getSubtypes(alltypes);
	rel[Entity, rel[str, Entity]] methods = getMethods(domain(alltypes) + additionaltypes, invert(subtypes), getHierarchy(alltypes, subtypes), fs);

	set[Entity] used = getUsedTypes(methods, subtypes);

	return relate(domainR(alltypes, used), domainR(methods, used), domainR(subtypes, used)); 
}

private Nodes relate(rel[Entity, FactMap] alltypes, rel[Entity, rel[str, Entity]] methods, rel[Entity, Entity] subtypes) {
	Nodes result = {};
	for(Entity type <- domain(alltypes)) {
		result += {<type, getOneFrom(alltypes[type]), getOneFrom(methods[type]), subtypes[type]>};
	}

	return result;
}

private set[Entity] getUsedTypes(rel[Entity, rel[str, Entity]] methods, rel[Entity, Entity] subtypes) {
	set[Entity] used;
	set[Entity] result = domain(methods);	
	
	do  {
		used = result;
		result = {};
			
		for(Entity type <- used) {
			result += subtypes[type];
		
			for(rel[str, Entity] perType <- methods[type]) {
				result += range(perType);
			}
		}	
	} while (size(result) < size(used));

	return result;
}


private rel[Entity, rel[str, Entity]] getMethods(set[Entity] returntypes, rel[Entity, Entity] supertypes, list[rel[Entity, FactMap]] hierarchy, Filters fs) {
	rel[Entity, rel[str, Entity]] methods = {};
	for(rel[Entity, FactMap] level <- hierarchy) {
		for(tuple[Entity e, FactMap f] type <- level) {
			rel[str, Entity] daddysMeths = {};
			for (rel[str, Entity] meth <- methods[supertypes[type.e]]) {
				daddysMeths += meth;
			}
			rel[str, Entity] ownMeths = getMethodsForType(type.e, type.f, returntypes, fs);
			methods += {<type.e, daddysMeths + ownMeths>};
		}
	}

	return methods;
}

private list[rel[Entity, FactMap]] getHierarchy(rel[Entity type, FactMap fm] alltypes, rel[Entity super, Entity type] subtypes) {
	list[rel[Entity, FactMap]] hierarchy = [];

	// We need those types just below java.lang.object 
	set[Entity] level = subtypes[domain(subtypes) - range(subtypes)];

	while (!isEmpty(level)) {
		hierarchy += [domainR(alltypes, level)];
				
		level = subtypes[level];
	}
 
 	return hierarchy;
}

private rel[Entity, Entity] getSubtypes(rel[Entity, FactMap] alltypes) {
	rel[Entity, Entity] subtypes = {};
	for(tuple[Entity type, FactMap fm] tup <- alltypes) {
		subtypes += {<getOneFrom(getExtends(tup.fm)[tup.type]) ,tup.type>};
	}

	return subtypes;
}

private rel[Entity, FactMap] getAllTypes(set[str] astFiles, set[str] excludeFiles) {
	rel[Entity, FactMap] allTypes = {};
	for(str file <- astFiles) {
		if(!isEmpty(getComplement({file}, excludeFiles))) {
			fm = extractFrom(file);
			for(Entity declaredType <- getDeclaredTopTypes(fm) + range(getDeclaredSubTypes(fm))) {
				if(!isHidden(declaredType, fm) && !isDeprecated(declaredType, fm)) {
					allTypes += {<declaredType, fm>};
				}
			}
		}
	}

	return allTypes;
}

/*
 * Only public members can be used for fact extraction, so there is 
 * no use indexing (package) private or protected members however 
 * important they may be.   
 */
private bool isHidden(Entity ent, FactMap fm) {
	return \public() notin getModifiers(fm)[ent];
}

private bool isDeprecated(Entity ent, FactMap fm) {
	return deprecated() in getModifiers(fm)[ent];
}

private bool isAbstract(Entity ent, FactMap fm) {
	return abstract() in getModifiers(fm)[ent];
}

/* 
 * We return only methods that:
 * 1) Do not take arguments. (If these methods were indeed significant, we can not know what values to provide for the arguments)
 * 2) Have a name which complies to the filters the user has provided.
 * 3) Have a return type that is either in the package or is provided by the user.
 * 4) Are public. (Hidden methods are not part of the API and therefore can not be used by us)
 * 5) Are not deprecated (Only Javadoc deprecated tag supported at this time). (If a method is deprecated it is not supposed to be used) 
 */
private rel[str, Entity] getMethodsForType(Entity type, FactMap fm, set[Entity] returnTypes, Filters fs) {
	rel[str, Entity] result = {};  
	for(Entity meth <- getDeclaredMethods(fm)[type]) {		
		if(/method(str NAME, list[value] ARGS, Entity RT) := meth) {
		 	if(isEmpty(ARGS) && isCompliant(fs, NAME) && RT in returnTypes && !isHidden(meth, fm) && !isDeprecated(meth,fm)) {		
				result += {<NAME, RT>};
		 	}
		} 
	}
	
	return result;
}

private FactMap extractFrom(str file) {
	if(/^\/<proj:[^\/]*><rest:.*>$/ := file) {
		return extractFacts(proj, rest);
	}
	
	return ();	
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
*                                                                       *
*                              CONVERT EXTRA CLASSES                    *
*                                                                       *
\* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
/*
private EntityMap convertToEntityMap(map[str, str] strMap) {
	EntityMap result = ();
	for(str fqname <- strMap) {
		result += (convertToEntity(fqname): strMap[fqname]);
	}
	
	return result;
}

private set[Entity] convertToEntities(set[str] fqns) {
	set[Entity] result = {};
	for(str fqn <- fqns) {
		result += {convertToEntity(fqn)};
	}

	return result;
}


private Entity convertToEntity(str fqn) {
	list[Id] ids = [];
	while(/^<pack:[^\.]*>\.<rest:.*>$/ := fqn) {
		ids += package(pack);
		fqn = rest;
	}  	
	ids += class(fqn);
	
	return entity(ids);
}
*/
//============================================================================

public EntityMap convertToEntityMap(map[tuple[str, list[int]], str] strMap) {
	EntityMap result = ();
	for (tuple[str, list[int]] tup <- strMap) {
		result += (convertToEntity(tup): strMap[tup]);
	}
	
	return result;
}

private set[Entity] convertToEntities(set[tuple[str, list[int]]] fqns) {
	set[Entity] result = {};
	for(tuple[str, list[int]] fqn <- fqns) {
		result += {convertToEntity(fqn)};
	}

	return result;
}

public Entity convertToEntity(tuple[str fqn, list[int] l] tup) {
	str temp = tup.fqn;
	list[Id] ids = [];
	int i = size(tup.l)-1;
	while(/^<prefix: .*>\.<id:[^\.]*>$/ := temp) {
		int idType = i >= 0? tup.l[i]: 0;
		ids = [getId(idType, id)] + ids;
		temp = prefix;
		i -= 1;
	}
	int idType = i >= 0? tup.l[i]: 0;
	ids = [getId(idType, temp)] + ids;
	
	return entity(ids);
}

public Id getId(int type, str name) {
	switch (type) {
		case PACKAGE:	return package(name);
		case CLASS:		return class(name);
		case INTERFACE:	return interface(name);
		case ENUM:		return enum(name);
	}
	
	return package(name);
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
*                                                                       *
*                              FILTERS                                  *
*                                                                       *
\* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

private bool isCompliant(Filters fs, str method) {
	return !isEmpty(getIntersection(getComplement({method}, fs.exclMethPatterns), fs.inclMethPatterns));
}

private set[str] getIntersection(set[str] universe, set[str] subsetPatterns) {
	set[str] result = {};
	for (str s <- subsetPatterns) {
		// no need to check against a string that is already included, it will always fail.
		result += getCompliantSet(universe - result, s); 
	}

	return result;
}

private set[str] getComplement(set[str] universe, set[str] subsetPatterns) {
	set[str] result = universe;
	for (str s <- subsetPatterns) {
		result -= getCompliantSet(result, s);
	}

	return result;
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
*                                                                       *
*                              PRINTING                                 *
*                                                                       *
\* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

//FIXME change patchToString to toString when toString on entity is fixed
public str toFile(str newModulePath, str astPackagePath, Nodes nodes, Additions ads) {
	list[str] datadefs = [];	
	
	Telemetry t = initTelemetry(); // TODO remove! just for acquiring metadata
	

	for(Entity type <- domain(nodes)) {	
		Nodes ofType = domainR(nodes, {type});	
		rel[str, Entity] methods = getOneFrom(ofType.methods);
		set[Entity] children = getOneFrom(ofType.children);
		FactMap facts = getOneFrom(ofType.facts);
		
		t = update(t, size(children), size(methods)); // TODO remove! just for acquiring metadata 

		str result = "data " + compact(astPackagePath, patchToString(type)) + " = ";
		if (!isAbstract(type, facts)) { // abstract classes can not be instantiated and as such are not useful to us.
			result += getNode(type, astPackagePath, methods, ads);
			if (!isEmpty(children)) {
				result += " | ";
			}
		}
		result += getChildren(astPackagePath, children) + ";";
		
		// This at least reduces any "schlemiel" problems.
		// Joel Spolsky # Back to Basics
		datadefs += [result];  
	}
	 
	
	if (/^\/?<proj:[^\/]*><middle:.*?><mod:[^\/]*><ext: \.rsc>$/ := newModulePath) {
		printData(location(proj).url + middle + mod + ext, mod, datadefs);
	}
	// TODO throw malformed path error
	
	return printTelemetry(t); // TODO remove! just for acquiring metadata
}

private str getNode(Entity type, str astPackagePath, rel[str, Entity] methods, Additions ads) {
		EntityMap extraRTs = convertToEntityMap(ads.extraRTs);
		
		str result = toLowerCase(compact(astPackagePath, patchToString(type))) + "(";
		
		bool separate = false;
		for (tuple [str s, Entity e] method <- methods) {
			if(separate){result += ", "; } else { separate = true;}
			result += (method.e in domain(extraRTs))? extraRTs[method.e] : compact(astPackagePath, patchToString(method.e));
			result += " " + method.s;
		}

		for (str method <- ads.extraMeths) {
			if(separate){result += ", "; } else { separate = true;}
			result += method;
		}

		result += ")";
		
		return result;
}

private str getChildren(str astPackagePath, set[Entity] children) {
		str result = "";
		bool separate = false;
		for (Entity child <- children) {
			if(separate){result += " | "; } else { separate = true;}
			
			str childStr = compact(astPackagePath, patchToString(child));
			result += toLowerCase(childStr) + "_labda(" + childStr + ")";
		}
		
		return result;
}


private str compact(str packagePath, str fqn) {
	if(/^<primaryFQNSection: [^\.]*>\.<fqnRemain:.*>$/ := fqn) {
		str packTemp = packagePath;
		while(/^<packSection: [^\/]*>\/<packRemain:.*>$/ := packTemp) {
			if(packSection == primaryFQNSection) {
				return getCompactedFQN(packTemp, fqn);
			} else {
				packTemp = packRemain;
			}
		}
	}
	
	return fqn; // fqn does not match the package
}

private str getCompactedFQN(str strippedPackagePath, str fqn) {
	str dottedPath = "";
	str tempPath = strippedPackagePath;
	while(/^\/?<section:[^\/]+><remain:.*?>$/ := tempPath) {
		dottedPath += section + ".";
		tempPath = remain;
	}
	
	str result;
	if (startsWith(fqn, dottedPath)) {
		result = substring(fqn, size(dottedPath));
	} else {
		result = fqn;
	}
	
	return makeSafe(result); // fqn does not match the package
}

//TODO are there any more characters that are allowed in paths but not in data declarations?
private str makeSafe(str input) {
	result = "";
	while (/^<before:[^.]*>\.<after:.*$>/ := input) {
		result += before + "_";
		input = after;
	}
	return result + input;	
}
