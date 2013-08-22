package org.rascalmpl.eclipse.perspective.actions;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;

import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.rascalmpl.checker.StaticChecker;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;

public class StaticCheckerHelper {
	private static HashMap<ISourceProject, StaticChecker> checkerMap = new HashMap<ISourceProject, StaticChecker>();
	
	public void initChecker(StaticChecker checker, final ISourceProject sourceProject) {
		checker.init();
		ProjectEvaluatorFactory.getInstance().configure(sourceProject.getRawProject(), checker.getEvaluator());
		checker.enableChecker(null);
	}
	
	public StaticChecker createChecker(ISourceProject sourceProject) {
		PrintStream consoleStream = RuntimePlugin.getInstance().getConsoleStream();
		StaticChecker checker = new StaticChecker(new PrintWriter(consoleStream), new PrintWriter(consoleStream));
		checkerMap.put(sourceProject, checker);
		initChecker(checker, sourceProject);
		return checker;
	}

	public StaticChecker createCheckerIfNeeded(ISourceProject sourceProject) {
		StaticChecker checker = null;
		if (checkerMap.containsKey(sourceProject)) {
			checker = checkerMap.get(sourceProject);
		}
		if (checker == null) {
			checker = createChecker(sourceProject);
		}
		return checker;
	}
	
	public StaticChecker reloadChecker(ISourceProject sourceProject) {
		StaticChecker checker = null;
		if (checkerMap.containsKey(sourceProject)) {
			checkerMap.remove(sourceProject);
		}
		checker = createChecker(sourceProject);
		return checker;
	}
}