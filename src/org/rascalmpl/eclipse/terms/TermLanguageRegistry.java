package org.rascalmpl.eclipse.terms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.values.uptr.Factory;
import org.rascalmpl.values.uptr.TreeAdapter;

public class TermLanguageRegistry {
	private final Map<String, Language> languages = new HashMap<String,Language>();
	private final Map<String, IEvaluatorContext> evals = new HashMap<String, IEvaluatorContext>();
	private final Map<String, ICallableValue> parsers = new HashMap<String,ICallableValue>();
	private final Map<String, ICallableValue> analyses = new HashMap<String,ICallableValue>();
	private final Map<String, ICallableValue> outliners = new HashMap<String,ICallableValue>();
	private final Map<String, ISet> contributions = new HashMap<String, ISet>();

	static private class InstanceKeeper {
		public static TermLanguageRegistry sInstance = new TermLanguageRegistry();
	}
	
	public static TermLanguageRegistry getInstance() {
		return InstanceKeeper.sInstance;
	}
	
	private TermLanguageRegistry() { }
	
	public void clear() {
		languages.clear();
		evals.clear();
		parsers.clear();
		analyses.clear();
		outliners.clear();
		contributions.clear();
	}
	
	public void clear(String value) {
		Language lang = LanguageRegistry.findLanguage(value);
		if (lang != null) {
			LanguageRegistry.deregisterLanguage(lang);
		}
		languages.remove(value);
		evals.remove(value);
		parsers.remove(value);
		analyses.remove(value);
		outliners.remove(value);
		contributions.remove(value);
	}
	
	public void registerLanguage(String name, String extension, ICallableValue parser, IEvaluatorContext ctx) {
		Language l = new Language(name, "", "demo editor for " + name, "Terms", "icons/rascal3D_2-32px.gif", "http://www.rascal-mpl.org","rascal-eclipse",extension,"",null);
		languages.put(extension, l);
		evals.put(name, ctx);
		parsers.put(name, parser);
		LanguageRegistry.registerLanguage(l);
	}
	
	public void registerAnnotator(String lang, ICallableValue function) {
		analyses.put(lang, function);
	}
	
	public void registerOutliner(String lang, ICallableValue builder) {
		outliners.put(lang, builder);
	}
	
	public void registerContributions(String lang, ISet set) {
		contributions.put(lang, set);
	}

	public Language getLanguage(String fileExtension) {
		return languages.get(fileExtension);
	}
	
	public Language getLanguage(ISourceLocation loc) {
		String path = loc.getURI().getPath();
		if (path != null) {
			int i = path.lastIndexOf('.');
			if (i != -1 && i != path.length() - 1) {
				String ext = path.substring(i+1);
				return getLanguage(ext);
			}
		}
		
		return null;
	}

	public Language getLanguage(IConstructor parseTree) {
		if (parseTree.getType() == Factory.Tree) {
			return getLanguage(TreeAdapter.getLocation(parseTree));
		}
		return null;
	}
	
	public IEvaluatorContext getEvaluator(Language lang) {
		return evals.get(lang.getName());
	}
	
	public ICallableValue getParser(Language lang) {
		return  parsers.get(lang.getName());
	}
	
	public ICallableValue getOutliner(Language lang) {
		return outliners.get(lang.getName());
	}
	
	public Map<String, ISet> getContributions() {
		return Collections.unmodifiableMap(contributions);
	}

	public ICallableValue getAnnotator(String name) {
		return analyses.get(name);
	}
}
