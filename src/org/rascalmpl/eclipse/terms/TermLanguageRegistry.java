package org.rascalmpl.eclipse.terms;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.result.OverloadedFunctionResult;
import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;

public class TermLanguageRegistry {
	private final Map<String, Language> languages = new HashMap<String,Language>();
	private final Map<String, IEvaluatorContext> evals = new HashMap<String, IEvaluatorContext>();
	private final Map<String, IConstructor> starts = new HashMap<String,IConstructor>();
	private final Map<String, OverloadedFunctionResult> analyses = new HashMap<String,OverloadedFunctionResult>();

	static private class InstanceKeeper {
		public static TermLanguageRegistry sInstance = new TermLanguageRegistry();
	}
	
	public static TermLanguageRegistry getInstance() {
		return InstanceKeeper.sInstance;
	}
	
	private TermLanguageRegistry() { }
	
	public void registerLanguage(String name, String extension, IConstructor start, IEvaluatorContext ctx) {
		Language l = new Language(name, "", "demo editor for " + name, "Terms", "icons/rascal3D_2-32px.gif", "http://www.rascal-mpl.org","rascal-eclipse",extension,"",null);
		languages.put(extension, l);
		evals.put(name, ctx);
		
		if (!start.getName().equals("non-terminal")) {
			throw RuntimeExceptionFactory.illegalArgument(start, ctx.getCurrentAST(), ctx.getStackTrace());
		}
		starts.put(name, (IConstructor) start.get(0));
		LanguageRegistry.registerLanguage(l);
	}
	
	public void registerAnnotator(String lang, OverloadedFunctionResult function) {
		analyses.put(lang, function);
	}

	public Language getLanguage(String fileExtension) {
		return languages.get(fileExtension);
	}
	
	public IEvaluatorContext getEvaluator(Language lang) {
		return evals.get(lang.getName());
	}
	
	public IConstructor getStart(Language lang) {
		return starts.get(lang.getName());
	}

	public OverloadedFunctionResult getAnnotator(String name) {
		return analyses.get(name);
	}
	
}
