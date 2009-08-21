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
private alias EntityMap = map[Entity fqn, str mapping];
private alias SupFMRel = rel[Entity super, FactMap facts];
private alias NodeChildRel = rel[Entity type, map[str methodName, Id method] children];

public alias additions = tuple[map[str fqn, str mapping] extraRTs, list[str] extraMeths];
public alias filters = tuple[set[str] exclFilePatterns, set[str] inclMethPatterns, set[str] exclMethPatterns];


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

public void buildDataScheme(str astPackagePath, str newModulePath, filters fs, additions ads) {
	EntityMap extraClasses = convertToEntityMap(ads.extraRTs);
	NodeChildRel nodes = buildDataSchemeHierarchically(getASTFiles(astPackagePath), fs, extraClasses);  
	toFile(newModulePath, nodes, extraClasses, ads.extraMeths);	
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
// getwithin the package
private NodeChildRel buildDataSchemeHierarchically(set[str] astFiles, filters fs, EntityMap extraClasses) {
	SupFMRel supFMs = getSuperTypedFactMaps(astFiles, fs.exclFilePatterns);
	NodeChildRel result = {};
	println("start build"); // TODO remove debug
	if (!isEmpty(supFMs)) {
		set[Entity] allRTClasses = getDeclaredTopTypes(unionFacts(range(supFMs))) + domain(extraClasses);
		
		set[FactMap] level = getFirstLevel(supFMs);
		for (FactMap fm <- level) {
			Entity type  = getOneFrom(getDeclaredTopTypes(fm)); // TODO remove debug
			println(type);// TODO remove debug
			result += {<getOneFrom(getDeclaredTopTypes(fm)), getMethods(fm, allRTClasses, fs)>};
		}

		println("halfway build"); // TODO remove debug
	
		level = getNextLevel(supFMs, level);
		while(!isEmpty(level)) {
			for (FactMap fm <- level) {
				Entity type  = getOneFrom(getDeclaredTopTypes(fm)); // TODO remove debug
				println(type);// TODO remove debug
				m = getOneFrom(result[getSuperClass(fm)]) + getMethods(fm, allRTClasses, fs); // TODO inline, debug
				result += {<getOneFrom(getDeclaredTopTypes(fm)), m>};
			}
			level = getNextLevel(supFMs, level);
		}
	}
	return result;
}

private map[str, Id] getMethods(FactMap fm, set[Entity] returnTypes, filters fs) {
	result = ();
	for(tuple[Entity clss, Entity type] declMethod <- getDeclaredMethods(fm)) {
		Id m = (declMethod.type.id - declMethod.clss.id)[0];
		if(method(str NAME,_,Entity RT) := m && isCompliant(fs, NAME) && RT in returnTypes) {		
			result += (NAME: m);
		} 
	}
	
	return result;
}



private SupFMRel getSuperTypedFactMaps(set[str] astFiles, set[str] excludeFiles) {
	SupFMRel result = {};
	for(str file <- astFiles) {
		if(!isEmpty(getComplement({file}, excludeFiles))) {
			fm = extractFrom(file);
			Entity super = getSuperClass(fm); 
			if (!isEmpty(super.id)) {
				result += {<super, fm>}; 
			}
		}
	}
	
	return result;
}

private Entity getSuperClass(FactMap fm) {
	// innerclasses occur here too thats why the domainR on toptypes
	rel[Entity, Entity] super = domainR(getExtends(fm), getDeclaredTopTypes(fm));
	if (!isEmpty(super)) {
		return getOneFrom(super).field1;
	}
	
	return entity([]); 
}

private set[FactMap] getFirstLevel(SupFMRel supFMs) {
	if(!isEmpty(supFMs)) {
		return range(domainX(supFMs, getDeclaredTopTypes(unionFacts(range(supFMs)))));
	}
	
	return {};
}

private set[FactMap] getNextLevel(SupFMRel supFMs, set[FactMap] currentLevel) {
	if(!isEmpty(currentLevel)) { 
		return range(domainR(supFMs, getDeclaredTopTypes(unionFacts(currentLevel))));
	}
	
	return {};
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
*                              PRINTING                                 *
*                                                                       *
\* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

private str toDataString(Entity fqn) {
 	str S = toString(fqn);
 	
 	result = "";
 	while (/^<before:[^\.]*>\.<after:.*$>/ := S) {
    	result += before + "_";
    	S = after;
  	}
  	return result + S;
}

private alias NodeChildRel = rel[Entity type, map[str methodName, Id method] children];


private void toFile(str modulePath, NodeChildRel nodes, EntityMap extraClasses, list[str] extraChildren) {
	list[str] datadefs = [];
	
	for(Entity entParent <- domain(nodes)) {
		str dsParent = toDataString(entParent); 
		str def = "data " + dsParent + " = ";
		def += toLowerCase(dsParent) + "(";
		
		bool separate = false;
		map[str, Id] children = getOneFrom(nodes[entParent]); // always one element 
		for(str name <- children) {
			if(separate) { def += ", "; } else { separate = true; }
			def += getChildEntry(children[name], extraClasses);
		}
		
		for(str additional <- extraChildren) {
			if(separate) { def += ", "; } else { separate = true; }
			def += additional;
		}
		
		def += ");";
		datadefs += [def];
	}
	
	if (/^\/?<proj:[^\/]*><middle:.*?><mod:[^\/]*><ext: \.rsc>$/ := modulePath) {
		printData(location(proj).url + middle + mod + ext, mod, datadefs);
	}
	// TODO throw malformed path error
	
}

private str getChildEntry(Id child, EntityMap extraClasses) {
		str result = "";

		Entity rt = child.returnType;
		if (rt in domain(extraClasses)) { //TODO pull domain() out of the loop
			result += extraClasses[rt];
		} else {
			result += toDataString(rt);
		}
		// name
		result += " " + child.name;
		
		return result;
}

private bool isCompliant(filters fs, str method) {
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

