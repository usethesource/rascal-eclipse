package org.meta_environment.rascal.eclipse.library.scripting;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.meta_environment.rascal.eclipse.console.ConsoleFactory;
import org.meta_environment.rascal.eclipse.console.ConsoleFactory.IRascalConsole;
import org.meta_environment.rascal.eclipse.console.internal.OutputInterpreterConsole;

public class CommandScripter{
	private static final IWorkspaceRoot WORKSPACE = ResourcesPlugin.getWorkspace().getRoot();
	
	public CommandScripter(IValueFactory valueFactory){
		super();
	}
	
	public void execute(IList commandsList, IList projectsList){
		String[] commands = new String[commandsList.length()];
		for(int i = commandsList.length() - 1; i >= 0; i--){
			commands[i] = ((IString) commandsList.get(i)).getValue();
		}
		
		IProject[] projects = new IProject[projectsList.length()];
		for(int i = projectsList.length() - 1; i >= 0; i--){
			projects[i] = WORKSPACE.getProject(((ISourceLocation) projectsList.get(i)).getURI().getHost());
		}
		
		ConsoleFactory cf = ConsoleFactory.getInstance();
		
		for(int i = 0 ; i < projects.length; i++){
			IProject project = projects[i];
			OutputInterpreterConsole console = (OutputInterpreterConsole) cf.openRunOutputConsole(project);
			try{
				for(int j = 0; j < commands.length; j++){
					console.executeCommandAndWait(commands[j]+"\n");
				}
			}catch(RuntimeException rex){
				System.err.println("Failed to execute commands in project: "+project+", cause: "+rex.getMessage());
			}
			console.terminate();
		}
	}
}
