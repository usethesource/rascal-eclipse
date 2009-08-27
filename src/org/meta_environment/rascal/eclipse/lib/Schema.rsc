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


public alias IntermediateRepresentation = tuple[str astPackagePath, Nodes nodes, EntityMap extraClasses, list[str] extraMethods];
public alias EntityMap = map[Entity fqn, str mapping];
public alias Nodes = tuple[TopNodes top, SubNodes sub];

public alias TopNodes = rel[Entity type, map[str methodName, Id method] children];
public alias SubNodes = rel[Entity type, Entity subType];
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
	toFile(newModulePath, buildDataSchemeHalfway(astPackagePath, fs, ads));	
}

public IntermediateRepresentation buildDataSchemeHalfway(str astPackagePath, filters fs, additions ads) {
	EntityMap extraClasses = convertToEntityMap(ads.extraRTs);
	Nodes nodes = buildDataSchemeHierarchically(getASTFiles(astPackagePath), fs, extraClasses); 
	nodes = removeUnusedNodes(nodes); 
	
	return <astPackagePath, nodes, extraClasses, ads.extraMeths>;
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
private Nodes buildDataSchemeHierarchically(set[str] astFiles, filters fs, EntityMap extraClasses) {
	SupFMRel supFMs = getSuperTypedFactMaps(astFiles, fs.exclFilePatterns);
	TopNodes tnResult = {};
	SubNodes snResult = {};
	if (!isEmpty(supFMs)) {
		set[Entity] allRTClasses = getUnhiddenTypes(unionFacts(range(supFMs))) + domain(extraClasses);
		
		set[FactMap] level = getFirstLevel(supFMs);
		for (FactMap fm <- level) {
			tnResult += {<getOneFrom(getDeclaredTopTypes(fm)), getMethods(fm, allRTClasses, fs)>};
		}
	
		level = getNextLevel(supFMs, level);
		while(!isEmpty(level)) {
			for (FactMap fm <- level) {
				Entity type = getOneFrom(getDeclaredTopTypes(fm));
				Entity supertype = getSuperClass(fm);
				snResult += {<supertype, type>};
				tnResult += {<type, getOneFrom(tnResult[supertype]) + getMethods(fm, allRTClasses, fs)>};
			}
			level = getNextLevel(supFMs, level);
		}
	}
	return <tnResult, snResult>;
}

private set[Entity] getUnhiddenTypes(FactMap allFacts) {
	set[Entity] unhidden = getDeclaredTopTypes(allFacts);
	for (Entity subType <- getDeclaredSubTypes(allFacts)) {
		if (!isHidden(subType, allFacts)) {
			unhidden += {subType};
		}
	}

	return unhidden;
} 

private bool isHidden(Entity ent, FactMap fm) {
	set[Modifier] mods = getModifiers(fm)[ent];
	return \private() in mods || protected() in mods;
}

private map[str, Id] getMethods(FactMap fm, set[Entity] returnTypes, filters fs) {
	result = ();  
	for(tuple[Entity clss, Entity meth] declMethod <- getDeclaredMethods(fm)) {
		Id m = (declMethod.meth.id - declMethod.clss.id)[0];
		if(method(str NAME,_,Entity RT) := m && isCompliant(fs, NAME) && RT in returnTypes && !isHidden(declMethod.meth, fm)) {		
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

private Nodes removeUnusedNodes(Nodes nodes) {
	Nodes current = nodes;
	Nodes last = <{},{}>;
	do {
		last = current;
	
		set[Entity] used = getUsedReturnTypes(last);
		current = <domainR(last.top, used), domainR(last.sub, used)>;

	} while(size(last.top) > size(current.top));
	
	return current;
}

private set[Entity] getUsedReturnTypes(Nodes nodes) {
	set[Entity] rts = range(nodes.sub);
	for (map[str, Id] mm <- nodes.top.children) {
		for(Id id <- range(mm)) {
			rts += id.returnType;
		}		
	}

	return rts;
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

public void toFile(str newModulePath, IntermediateRepresentation ir) {
	list[str] datadefs = [];
	
	for(Entity entParent <- domain(ir.nodes.top)) {
		str dsParent = compact(ir.astPackagePath, toString(entParent)); 
		str def = "data " + dsParent + " = ";
		def += toLowerCase(dsParent) + "(";
		
		bool separate = false;
		map[str, Id] children = getOneFrom(ir.nodes.top[entParent]); // always one element 
		for(str name <- children) {
			if(separate) { def += ", "; } else { separate = true; }
			def += getChildEntry(children[name], ir.extraClasses, ir.astPackagePath);
		}
		
		for(str additional <- ir.extraMethods) {
			if(separate) { def += ", "; } else { separate = true; }
			def += additional;
		}
		
		def += ")";
		
		for(Entity sub <- ir.nodes.sub[entParent]) {
			str dsSub = compact(ir.astPackagePath, toString(sub));
			def += " | " + toLowerCase(dsSub) + "_labda(" + dsSub + ")";
		}		

		def += ";";
		datadefs += [def];
	}
	
	if (/^\/?<proj:[^\/]*><middle:.*?><mod:[^\/]*><ext: \.rsc>$/ := newModulePath) {
		printData(location(proj).url + middle + mod + ext, mod, datadefs);
	}
	// TODO throw malformed path error
	
}

private str getChildEntry(Id child, EntityMap extraClasses, str packagePath) {
	str result = "";

	Entity rt = child.returnType;
	if (rt in domain(extraClasses)) {
		result += extraClasses[rt];
	} else {
		result += compact(packagePath, toString(rt));
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

