package org.rascalmpl.eclipse.builder;

import java.io.PrintWriter;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.repl.CommandExecutor;

public class RascalChecker extends IncrementalProjectBuilder {
	private final CommandExecutor exec = new CommandExecutor(new PrintWriter(System.err), new PrintWriter(System.out));
	
	public RascalChecker() {
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		super.clean(monitor);
	}
	
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		return null;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		super.setInitializationData(config, propertyName, data);
	}
}
