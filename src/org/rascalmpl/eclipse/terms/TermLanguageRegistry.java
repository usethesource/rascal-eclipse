/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Tijs van der Storm - Tijs.van.der.Storm@cwi.nl
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.terms;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISetWriter;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.nature.ModuleReloader;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.uptr.Factory;
import org.rascalmpl.values.uptr.TreeAdapter;

import static org.rascalmpl.eclipse.IRascalResources.ID_RASCAL_ECLIPSE_PLUGIN;

public class TermLanguageRegistry {
	private final Map<String, Language> languages = new HashMap<String,Language>();
	private final Map<String, IEvaluatorContext> evals = new HashMap<String, IEvaluatorContext>();
	private final Map<String, ICallableValue> parsers = new HashMap<String,ICallableValue>();
	private final Map<String, ICallableValue> analyses = new HashMap<String,ICallableValue>();
	private final Map<String, ICallableValue> outliners = new HashMap<String,ICallableValue>();
	private final Map<String, ISet> contributions = new HashMap<String, ISet>();
	private final Map<String, ISet> nonRascalContributions = new ConcurrentHashMap<String, ISet>();
	private Map<String, ModuleReloader> reloaders = new HashMap<>();

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
		reloaders.clear();
	}
	
	public void clearNonRascal() {
		nonRascalContributions.clear();
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
		reloaders.remove(value);
	}
	
	public void clearNonRascal(String value) {
		nonRascalContributions.remove(value);
	}
	
	public void registerLanguage(String name, String extension, ICallableValue parser, IEvaluatorContext ctx) {
		Language l = new Language(name, "", "demo editor for " + name, "Terms", "icons/rascal3D_2-32px.gif", "http://www.rascal-mpl.org",ID_RASCAL_ECLIPSE_PLUGIN,extension,"",null);
		languages.put(extension, l);
		evals.put(name, ctx);
		reloaders.put(name, new ModuleReloader((Evaluator) ctx.getEvaluator()));
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
	
	public void registerNonRascalContributions(String lang, ISet set) {
		nonRascalContributions.put(lang, set);
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
		return getEvaluator(lang.getName());
	}
	
	private IEvaluatorContext getEvaluator(String lang) {
		return evals.get(lang);
	}
	
	public ICallableValue getParser(Language lang) {
		if (lang == null) {
			return null;
		}
		return  parsers.get(lang.getName());
	}
	
	public ModuleReloader getReloader(Language language) {
		if (language == null) {
			return null;
		}
		return reloaders.get(language.getName());
	}
	
	public ICallableValue getOutliner(Language lang) {
		ICallableValue outliner = outliners.get(lang.getName());
		
		if (outliner != null) {
			return outliner;
		}
		
		ISet outliners = getContributions(lang, "outliner");

		if (outliners.size() > 1) {
			Activator.getInstance().logException("ignoring multiple outliners! for " + lang, new UnsupportedOperationException());
		}
		
		if (outliners.size() > 0) {
			IConstructor tree = (IConstructor) outliners.iterator().next();
			return (ICallableValue) tree.get("outliner");
		}
		
		return null;
	}
	
	public ISet getContentProposer(Language lang) {
		return getContentProposer(lang.getName());
	}
	
	public ISet getContentProposer(String lang) {
		return getContributions(lang, "proposer");
	}
	
	public ISet getContributions(Language lang) {
		return getContributions(lang.getName());
	}
	
	private ISet getContributions(String lang) {
		if (contributions.containsKey(lang))
			return contributions.get(lang);
		else
			return ValueFactoryFactory.getValueFactory().set(TypeFactory.getInstance().voidType());
	}
	
	public ISet getBuilders(Language lang) {
		return getContributions(lang, "builder");
	}

	private ISet getContributions(Language lang, String cons) {
		return getContributions(lang.getName(), cons);
	}
	
	private ISet getContributions(String lang, String cons) {
		IValueFactory vf = getEvaluator(lang).getValueFactory();
			
		ISetWriter result = vf.setWriter(); 
		for (IValue contribution: getContributions(lang)) {
			IConstructor tree = (IConstructor) contribution;
			if (tree.getName().equals(cons)) {
				result.insert(tree);
			}
		}
		
		return result.done();
	}

	public ISet getNonRascalContributions(String editorId) {
		return nonRascalContributions.get(editorId);
	}
	
	
	public ICallableValue getAnnotator(Language lang) {
		ICallableValue annotator = analyses.get(lang.getName());
		
		if (annotator != null) {
			return annotator;
		}
		
		ISet annotators = getContributions(lang, "annotator");

		if (annotators.size() > 1) {
			Activator.getInstance().logException("ignoring multiple annotator! for " + lang, new UnsupportedOperationException());
		}
		
		if (annotators.size() > 0) {
			IConstructor tree = (IConstructor) annotators.iterator().next();
			return (ICallableValue) tree.get("annotator");
		}
		
		return null;
	}

	
}
