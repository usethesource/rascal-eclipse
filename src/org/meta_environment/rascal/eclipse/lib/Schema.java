package org.meta_environment.rascal.eclipse.lib;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISetWriter;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.impl.fast.ValueFactory;
import org.eclipse.imp.pdb.facts.type.TypeFactory;

public class Schema {
	
	public static void printData(IString url, IString module, IList datadefs) throws IOException {
		String path = url.getValue().replaceFirst("file:", "");
		File file = new File(path);	
		
		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			
			PrintWriter pw; 
			pw = new PrintWriter(file);
			pw.println("module " + module.getValue() + "\n");
			
			for(IValue def: datadefs) {
				pw.println(((IString)def).getValue());
			}
			
			pw.close();
		} catch (IOException e) {
			System.err.println("Writing to " + path + " failed" + "\n");
			throw e;
		}
	}

	public static ISet getASTFiles(IString path) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		return getASTFiles(root.getContainerForLocation(root.findMember(new Path(path.getValue())).getLocation()));
	}
	
	private static ISet getASTFiles(IContainer cont) {	
		IValueFactory vf = ValueFactory.getInstance();	
		ISetWriter classes = vf.setWriter(TypeFactory.getInstance().stringType());
		
		if (cont != null) {
			try {
				for (IResource res: cont.members()) {
					if (res.getType() == IResource.FILE && res.getFileExtension().contentEquals("java")) {
						classes.insert(vf.string(res.getFullPath().toString()));
					} else if (res.getType() == IResource.FOLDER) {
						classes.insertAll(getASTFiles((IFolder)res));
					}
					// only other options are PROJECT or ROOT, so should never happen.
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		} 
		
		return classes.done();
	}
	
	public static ISet getCompliantSet(ISet universe, IString searchString) {
		ISetWriter result = ValueFactory.getInstance().setWriter(universe.getElementType());  
		
		Pattern pattern = Pattern.compile(searchString.getValue());
		
		for(IValue uString: universe) {
			if (pattern.matcher(((IString)uString).getValue()).find()) {
				result.insert(uString);
			} 
		}
		
		return result.done();
	}

}
