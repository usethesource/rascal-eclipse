package org.rascalmpl.eclipse.nature;

import java.util.Stack;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.rascalmpl.interpreter.IRascalMonitor;

public class RascalMonitor implements IRascalMonitor {
	private final Stack<IProgressMonitor> monitorStack = new Stack<IProgressMonitor>();

	public RascalMonitor(IProgressMonitor monitor) {
		monitorStack.push(monitor);
	}
	
	public void endJob(boolean succeeded) {
		if (!monitorStack.empty())
			monitorStack.pop().done();
	}

	public void event(String name) {
		event(name, 1);
	}

	public void event(String name, int inc) {
		event(inc);
		monitorStack.peek().subTask(name);
	}

	public void event(int inc) {
		monitorStack.peek().worked(inc);
	}

	public void startJob(String name, int totalWork) {
		if (monitorStack.size() == 1) {
			monitorStack.peek().beginTask(name, totalWork);
		}
		else {
			monitorStack.push(new SubProgressMonitor(monitorStack.peek(), totalWork));
		}
	}
}
