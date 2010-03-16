package org.rascalmpl.eclipse.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.values.uptr.ParsetreeAdapter;
import org.rascalmpl.values.uptr.TreeAdapter;

public class MarkerModelListener {

	public void update(IConstructor parseTree, IParseController parseController, final IProgressMonitor monitor) {
		try {
			IPath path = parseController.getPath();
			IProject project = parseController.getProject().getRawProject();
			
			monitor.beginTask("Marking errors and warnings in " + path, 1);
			if (parseTree != null) {
				IFile res = (IFile) project.findMember(path, false);

				res.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
				processMarkers(res, parseTree); 
				res.refreshLocal(IResource.DEPTH_ZERO, monitor);
				monitor.worked(1);
			}
		} catch (CoreException e) {
			Activator.getInstance().logException("failed marking the file", e);
		}
	}
	
	private void processMarkers(IFile file, IConstructor tree) throws CoreException {
		if (ParsetreeAdapter.isParseTree(tree)) {
			processMarkers(file, ParsetreeAdapter.getTop(tree));
			return;
		}
		
		if (TreeAdapter.isAppl(tree) && !TreeAdapter.isLexToCf(tree)) {
			IValue anno = tree.getAnnotation("message");
			if (anno != null && anno.getType().isAbstractDataType() && anno.getType().getName().equals("Message")) {
				IConstructor marker = (IConstructor) anno;
				ISourceLocation loc = TreeAdapter.getLocation(tree);
				processMarker(file, marker, loc);
			}
			
			anno = tree.getAnnotation("messages");
			if (anno != null && anno.getType().isSetType()) {
				Type elemType = anno.getType().getElementType();
				
				if (elemType.isAbstractDataType() && elemType.getName().equals("Message")) {
					
					for (IValue message : ((ISet) anno)) {
						IConstructor marker = (IConstructor) message;
						ISourceLocation loc = (ISourceLocation) marker.get(1);
						if (loc == null) {
							loc = (ISourceLocation) tree.getAnnotation("loc");
						}
						processMarker(file, marker, loc);
					}
				}
				
				// we do not recurse if we found messages
				return;
			}
			
			for (IValue child : TreeAdapter.getArgs(tree)) {
				processMarkers(file, (IConstructor) child);
			}
		}
		else if (TreeAdapter.isAmb(tree)) {
			for (IValue alt : TreeAdapter.getAlternatives(tree)) {
				processMarkers(file, (IConstructor) alt);
			}
		}
	}

	private void processMarker(IFile file, IConstructor marker,
			ISourceLocation loc) throws CoreException {
		int severity = IMarker.SEVERITY_INFO;

		if (marker.getName().equals("error")) {
			severity = IMarker.SEVERITY_ERROR;
		}
		else if (marker.getName().equals("warning")) {
			severity = IMarker.SEVERITY_WARNING;
		}

		IMarker m = file.createMarker(IMarker.PROBLEM);
		m.setAttribute(IMarker.TRANSIENT, true);
		m.setAttribute(IMarker.CHAR_START, loc.getOffset());
		m.setAttribute(IMarker.CHAR_END, loc.getOffset() + loc.getLength());
		m.setAttribute(IMarker.MESSAGE, ((IString) marker.get(0)).getValue());
		m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
		m.setAttribute(IMarker.SEVERITY, severity);
	}

	public int compareTo(IModelListener o) {
		if (o instanceof StaticCheckModelListener) {
			return 1;
		}
		return 0;
	}
}
