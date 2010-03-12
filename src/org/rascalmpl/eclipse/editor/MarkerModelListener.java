package org.rascalmpl.eclipse.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.ui.PlatformUI;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.values.uptr.TreeAdapter;

public class MarkerModelListener implements IModelListener {

	public AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.TYPE_ANALYSIS;
	}

	public void update(final IParseController parseController,
			final IProgressMonitor monitor) {
		try {
			IPath path = parseController.getPath();
			monitor.beginTask("Marking errors and warnings in " + path, 1);
			if (parseController.getCurrentAst() != null) {
				final IFile[] file = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
				if (file.length < 1) {
					return;
				}


				file[0].deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							processMarkers(file[0], (IConstructor) parseController.getCurrentAst());		
							monitor.worked(1);
						} catch (CoreException e) {
							Activator.getInstance().logException("failed marking the file", e);
						}
					}
				});
			}
		} catch (CoreException e) {
			Activator.getInstance().logException("failed marking the file", e);
		}
	}
	
	private void processMarkers(IFile file, IConstructor tree) throws CoreException {
		if (TreeAdapter.isAppl(tree) && !TreeAdapter.isLexToCf(tree)) {
			for (IValue child : TreeAdapter.getASTArgs(tree)) {
				processMarkers(file, (IConstructor) child);
			}
		}
		else if (TreeAdapter.isAmb(tree)) {
			for (IValue alt : TreeAdapter.getAlternatives(tree)) {
				processMarkers(file, (IConstructor) alt);
			}
		}

		IValue anno = tree.getAnnotation("marker");
		if (anno != null && anno.getType().isAbstractDataType() && anno.getType().getName().equals("Marker")) {
			IConstructor marker = (IConstructor) anno;
			ISourceLocation loc = TreeAdapter.getLocation(tree);
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
	}
}
