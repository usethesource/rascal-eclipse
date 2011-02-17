package org.rascalmpl.eclipse.terms;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.pdb.facts.IValue;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.result.AbstractFunction;

public class TermLanguageRegistry {
	private final Map<String, Language> languages = new HashMap<String,Language>();
	private final Map<String, IEvaluatorContext> evals = new HashMap<String, IEvaluatorContext>();
	private final Map<String, IValue> parsers = new HashMap<String,IValue>();
	private final Map<String, AbstractFunction> analyses = new HashMap<String,AbstractFunction>();

	static private class InstanceKeeper {
		public static TermLanguageRegistry sInstance = new TermLanguageRegistry();
	}
	
	public static TermLanguageRegistry getInstance() {
		return InstanceKeeper.sInstance;
	}
	
	private TermLanguageRegistry() { }
	
	public void registerLanguage(String name, String extension, IValue parser, IEvaluatorContext ctx) {
		Language l = new Language(name, "", "demo editor for " + name, "Terms", "icons/rascal3D_2-32px.gif", "http://www.rascal-mpl.org","rascal-eclipse",extension,"",null);
		languages.put(extension, l);
		evals.put(name, ctx);
		parsers.put(name, parser);
		LanguageRegistry.registerLanguage(l);
	}
	
	public void registerAnnotator(String lang, AbstractFunction function) {
		analyses.put(lang, function);
	}

	public Language getLanguage(String fileExtension) {
		return languages.get(fileExtension);
	}
	
	public IEvaluatorContext getEvaluator(Language lang) {
		return evals.get(lang.getName());
	}
	
	public AbstractFunction getParser(Language lang) {
		return (AbstractFunction) parsers.get(lang.getName());
	}

	public AbstractFunction getAnnotator(String name) {
		return analyses.get(name);
	}
	
}
