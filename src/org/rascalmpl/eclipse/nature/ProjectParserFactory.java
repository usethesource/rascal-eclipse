package org.rascalmpl.eclipse.nature;

import java.io.PrintWriter;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.values.ValueFactoryFactory;

/**
 * Maps projects to evaluators to parse Rascal files
 * @author jurgenv
 *
 */
public class ProjectParserFactory {
	private final WeakHashMap<IProject, Evaluator> parserForProject = new WeakHashMap<IProject, Evaluator>();
	private final WeakHashMap<IProject, ModuleReloader> reloaderForProject = new WeakHashMap<IProject, ModuleReloader>();
	private final PrintWriter out = new PrintWriter(RuntimePlugin.getInstance().getConsoleStream());
	
	private ProjectParserFactory() {
      // TODO: add listeners to remove when projects are deleted or closed!
	}
	
	private static class InstanceHolder {
	      public static final ProjectParserFactory sInstance = new ProjectParserFactory();
	}
	
	public static ProjectParserFactory getInstance() {
		return InstanceHolder.sInstance;
	}
	
	public void clear() {
		reloaderForProject.clear();
		parserForProject.clear();
	}
	
	public void resetParser(IProject project) {
		parserForProject.remove(project);
		reloaderForProject.remove(project);
	}
	
	public Evaluator getParser(IProject project) {
		Evaluator parser = parserForProject.get(project);
		
		if (parser == null) {
			GlobalEnvironment heap = new GlobalEnvironment();
			parser = new Evaluator(ValueFactoryFactory.getValueFactory(), out, out, new ModuleEnvironment("***parser***", heap), heap);
			reloaderForProject.put(project, new ModuleReloader(parser));
			parserForProject.put(project, parser);
			return parser;
		}
		
		try {
			reloaderForProject.get(project).updateModules();
		}
		catch (StaticError e) {
			// things may go wrong while reloading modules, simply because the modules still have parse errors in them.
			// these are safely ignored here, the user will have had feedback on those errors elsewhere
			// TODO: remove this debug code
			System.err.println("ignored error: " + e.getMessage());
			e.printStackTrace();
		}
		
		return parser;
	}
}
