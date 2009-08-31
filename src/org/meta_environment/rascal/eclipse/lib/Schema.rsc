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

private alias classMethods = rel[Entity class, set[Id] method];
private alias SupFMRel = rel[Entity super, FactMap facts];


//public alias IntermediateRepresentation = tuple[str astPackagePath, Nodes nodes, EntityMap extraClasses, list[str] extraMethods];
public alias EntityMap = map[Entity fqn, str mapping];
public alias Nodes = rel[Entity type, FactMap facts, rel[str, Entity] methods, set[Entity] children]; //tuple[TopNodes top, SubNodes sub];

// public alias TopNodes = rel[Entity type, map[str methodName, Id method] children];
// public alias SubNodes = rel[Entity type, Entity subType];
public alias Additions = tuple[map[str fqn, str mapping] extraRTs, list[str] extraMeths];
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
@javaClass{org.meta_environment.rascal.eclipse.lib.Schema}
;

private void java printData(str path, str modulename, list[str] datadef)
@javaClass{org.meta_environment.rascal.eclipse.lib.Schema}
;

private set[str] java getCompliantSet(set[str] universe, str searchString)
@javaClass{org.meta_environment.rascal.eclipse.lib.Schema}
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

public void buildDataScheme(str astPackagePath, str newModulePath, Filters fs, Additions ads) {	
	toFile(newModulePath, astPackagePath, buildDataSchemeHalfway(astPackagePath, fs, ads), ads);	
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

	set[Entity] used = getUsedTypes(methods);

	return relate(domainR(used,alltypes), domainR(used,methods), domainR(subtypes)); 
}

private Nodes relate(rel[Entity, FactMap] alltypes, rel[Entity, rel[str, Entity]] methods, rel[Entity, Entity] subtypes) {
	Nodes result = {};
	for(Entity type <- alltypes) {
		result += {<type, getOneFrom(alltypes[type]), getOneFrom(methods[type]), subtypes[type]>};
	}

	return result;
}

private set[Entity] getUsedTypes(rel[Entity, rel[str, Entity]] methods) {
	set[Entity] result;	
	set[Entity] used = domain(methods);

	do {
		result = used;
		used = {};
		for(rel[str, Entity] perType <- rangeR(methods, result)) {
			used += range(perType);
		}
	} while(size(result) > size(used));

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
			for(Entity declaredType <- getDeclaredTopTypes(fm) + getDeclaredSubTypes(fm)) {
				if(!isHidden(declaredType, fm)) {
					allTypes += {<declaredType, fm>};
				}
			}
		}
	}

	return allTypes;
}

private bool isHidden(Entity ent, FactMap fm) {
	set[Modifier] mods = getModifiers(fm)[ent];
	return \private() in mods || protected() in mods;
}

private rel[str, Entity] getMethodsForType(Entity type, FactMap fm, set[Entity] returnTypes, Filters fs) {
	rel[str, Entity] result = ();  
	for(Entity meth <- getDeclaredMethods(fm)[type]) {		
		if(/method(str NAME,_,Entity RT) := meth && isCompliant(fs, NAME) && RT in returnTypes && !isHidden(meth, fm)) {		
			result += {<NAME,RT>};
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
		result += {convertEntity(fqn)};
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
		// no need to check a string if it is already included
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

public void toFile(str newModulePath, str astPackagePath, Nodes nodes, Additions ads) {
	list[str] datadefs = [];
	EntityMap extraRTs = convertToEntityMap(ads.extraRTs);	

	for(Enity type <- domain(nodes)) {
		str result = "";
		
		// begin
		str typeStr = compact(astPackagePath, toString(type));
		result += "data " + typeStr + " = " + toLowerCase(typeStr) + "(";
		
		// methods
		bool separate = false;
		for(tuple[str s, Entity e] method <- nodes[type].methods) {
			if(separate){result += ", "; } else { separate = true;}
			result += (e in domain(extraRTs))? extraRTs[e] : compact(astPackagePath, toString(e)) + " " + s;
		}

		for (str method <- ads.extraMeths) {
			if(separate){result += ", "; } else { separate = true;}
			result += method;
		}

		result += ")";
		
		// children
		for(Entity child <- nodes[type].children) {
			str childStr = compact(astPackagePath, toString(child));
			result += " | " + toLowerCase(childStr) + "_labda(" + childStr + ")";
		}
		result += ";";
		datadefs += [result];
	}

	
	if (/^\/?<proj:[^\/]*><middle:.*?><mod:[^\/]*><ext: \.rsc>$/ := newModulePath) {
		printData(location(proj).url + middle + mod + ext, mod, datadefs);
	}
	// TODO throw malformed path error
	
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
	
	if (startsWith(fqn, dottedPath)) {
		return substring(fqn, size(dottedPath));
	}
	
	return fqn; // fqn does not match the package
}
