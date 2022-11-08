/*******************************************************************************
 * Copyright (c) 2009-2017 CWI
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

import static org.rascalmpl.eclipse.IRascalResources.ID_RASCAL_ECLIPSE_PLUGIN;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.values.IRascalValueFactory;
import org.rascalmpl.values.RascalValueFactory;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.functions.IFunction;
import org.rascalmpl.values.parsetrees.ITree;
import org.rascalmpl.values.parsetrees.TreeAdapter;

import io.usethesource.impulse.language.Language;
import io.usethesource.impulse.language.LanguageRegistry;
import io.usethesource.vallang.IBool;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISetWriter;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;

public class TermLanguageRegistry {
	private final Map<String, Language> languages = new HashMap<>();
	private final Map<String, IFunction> parsers = new HashMap<>();
	private final Map<String, IFunction> analyses = new HashMap<>();
	private final Map<String, IFunction> outliners = new HashMap<>();
	private final Map<String, ISet> contributions = new HashMap<>();
	private final Map<String, ISet> nonRascalContributions = new ConcurrentHashMap<>();

	static private class InstanceKeeper {
		public static TermLanguageRegistry sInstance = new TermLanguageRegistry();
	}
	
	public static TermLanguageRegistry getInstance() {
		return InstanceKeeper.sInstance;
	}
	
	private TermLanguageRegistry() { }
	
	public void clear() {
		languages.clear();
		parsers.clear();
		analyses.clear();
		outliners.clear();
		contributions.clear();
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
		parsers.remove(value);
		analyses.remove(value);
		outliners.remove(value);
		contributions.remove(value);
	}
	
	public void clearNonRascal(String value) {
		nonRascalContributions.remove(value);
	}
	
	public void registerLanguage(String name, String extension, IFunction parser) {
		Language l = new Language(name, "", "demo editor for " + name, "Terms", "icons/rascal_logo_32px.png", "http://www.rascal-mpl.org",ID_RASCAL_ECLIPSE_PLUGIN,extension,"",null);
		languages.put(extension.startsWith(".") ? extension.substring(0) : extension, l);
		parsers.put(name, parser);
		LanguageRegistry.registerLanguage(l);
	}

	public void registerAnnotator(String lang, IFunction function) {
		analyses.put(lang, function);
	}
	
	public void registerOutliner(String lang, IFunction builder) {
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
		String path = loc.getPath();
		if (path != null) {
			int i = path.lastIndexOf('.');
			if (i != -1 && i != path.length() - 1) {
				String ext = path.substring(i+1);
				return getLanguage(ext);
			}
		}
		
		return null;
	}

	public Language getLanguage(ITree parseTree) {
		if (parseTree.getType().isSubtypeOf(RascalValueFactory.Tree)) {
			return getLanguage(TreeAdapter.getLocation(parseTree));
		}
		return null;
	}
	
	public IFunction getParser(Language lang) {
		if (lang == null) {
			return null;
		}
		return  parsers.get(lang.getName());
	}
	
	public IFunction getOutliner(Language lang) {
		IFunction outliner = outliners.get(lang.getName());
		
		if (outliner != null) {
			return outliner;
		}
		
		ISet outliners = getContributions(lang, "outliner");

		if (outliners.size() > 1) {
			Activator.getInstance().logException("ignoring multiple outliners! for " + lang, new UnsupportedOperationException());
		}
		
		if (outliners.size() > 0) {
			IConstructor tree = (IConstructor) outliners.iterator().next();
			return (IFunction) tree.get("outliner");
		}
		
		return null;
	}
	
	public ISet getContentProposer(Language lang) {
		return getContentProposer(lang.getName());
	}
	
	public IBool getHasQuickFixes(Language lang) {
		return getHasQuickFixes(lang.getName());
	}

	public IBool getHasQuickFixes(String lang) {
		ISet props = getContributions(lang, "treeProperties");
		for (IValue v : props) {
			IConstructor p = (IConstructor)v;
			if (p.mayHaveKeywordParameters() && p.asWithKeywordParameters().hasParameter("hasQuickFixes")) {
				return (IBool) p.asWithKeywordParameters().getParameter("hasQuickFixes");
			}
		}
		return IRascalValueFactory.getInstance().bool(true);

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
			return ValueFactoryFactory.getValueFactory().set();
	}
	
	public ISet getBuilders(Language lang) {
		return getContributions(lang, "builder");
	}

	private ISet getContributions(Language lang, String cons) {
		return getContributions(lang.getName(), cons);
	}
	
	private ISet getContributions(String lang, String cons) {
		IValueFactory vf = IRascalValueFactory.getInstance();
			
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
	
	
	public IFunction getAnnotator(Language lang) {
		IFunction annotator = analyses.get(lang.getName());
		
		if (annotator != null) {
			return annotator;
		}
		
		ISet annotators = getContributions(lang, "annotator");

		if (annotators.size() > 1) {
			Activator.getInstance().logException("ignoring multiple annotator! for " + lang, new UnsupportedOperationException());
		}
		
		if (annotators.size() > 0) {
			IConstructor tree = (IConstructor) annotators.iterator().next();
			return (IFunction) tree.get("annotator");
		}
		
		return null;
	}

	public IFunction getLiveUpdater(Language lang) {
		ISet updaters = getContributions(lang, "liveUpdater");

		if (updaters.size() > 1) {
			Activator.getInstance().logException("ignoring multiple updater for " + lang, new UnsupportedOperationException());
		}
		
		if (updaters.size() > 0) {
			IConstructor tree = (IConstructor) updaters.iterator().next();
			return (IFunction) tree.get("updater");
		}
		return null;
	}

	public IConstructor getSyntaxProperties(Language lang) {
		ISet properties = getContributions(lang, "syntaxProperties");
		
		if (properties.size() > 1) {
			Activator.getInstance().logException("ignoring multiple syntax properties for " + lang, new UnsupportedOperationException());
		}
		
		if (properties.size() > 0) {
			return (IConstructor) properties.iterator().next();
		}
		
		return null;
	}
	
}
