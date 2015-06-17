package org.rascalmpl.eclipse.perspective.actions;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.rascalmpl.checker.StaticChecker;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.nature.WarningsToMarkers;

public class StaticCheckerHelper {
	private static HashMap<IProject, StaticChecker> checkerMap = new HashMap<>();
	
	public void initChecker(RascalMonitor mon, StaticChecker checker, final IProject sourceProject) {
		checker.init();
		ProjectEvaluatorFactory.getInstance().configure(sourceProject, checker.getEvaluator());
		checker.enableChecker(mon);
	}
	
	public StaticChecker createChecker(RascalMonitor mon, IProject sourceProject) {
		PrintStream consoleStream = RuntimePlugin.getInstance().getConsoleStream();
		StaticChecker checker = new StaticChecker(new PrintWriter(consoleStream), new PrintWriter(consoleStream));
		checkerMap.put(sourceProject, checker);
		initChecker(mon, checker, sourceProject);
		return checker;
	}

	public StaticChecker createCheckerIfNeeded(RascalMonitor mon, IProject sourceProject) {
		StaticChecker checker = null;
		if (checkerMap.containsKey(sourceProject)) {
			checker = checkerMap.get(sourceProject);
		}
		if (checker == null) {
			checker = createChecker(mon, sourceProject);
		}
		return checker;
	}
	
	public StaticChecker reloadChecker(IProgressMonitor monitor, IProject sourceProject) {
		StaticChecker checker = null;
		if (checkerMap.containsKey(sourceProject)) {
			checkerMap.remove(sourceProject);
		}
		checker = createChecker(new RascalMonitor(monitor, new WarningsToMarkers()), sourceProject);
		return checker;
	}
}