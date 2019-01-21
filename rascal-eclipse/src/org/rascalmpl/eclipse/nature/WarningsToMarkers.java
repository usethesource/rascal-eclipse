package org.rascalmpl.eclipse.nature;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.uri.URIResourceResolver;

import io.usethesource.impulse.builder.MarkerCreator;
import io.usethesource.vallang.ISourceLocation;

public class WarningsToMarkers implements IWarningHandler {
	private final IProject project;
	private final PrintWriter stderr;
	
	public WarningsToMarkers(IProject project, PrintWriter stderr) {
		this.project = project;
		this.stderr = stderr;
	}
	
	@Override
	public void warning(String msg, ISourceLocation src) {
		stderr.println("[WARNING] " + src + ":" + msg);
		
		try {
			IResource res = URIResourceResolver.getResource(src);

			if (res != null) {
				Map<String,Object> attrs = new HashMap<String,Object>();
				attrs.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
				attrs.put(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);

				if (res instanceof IFile) {
					new MarkerCreator((IFile) res, IRascalResources.ID_RASCAL_MARKER).handleSimpleMessage(msg, src.getOffset(), src.getOffset() + src.getLength(), src.getBeginColumn(), src.getEndColumn(), src.getBeginLine(), src.getEndLine(), attrs);
				}
			}
		}
		catch (Throwable e) {
			// handling error messages should be very robust 
			Activator.log("could not handle warning message: " + msg, e);
		}
	}

	@Override
	public void clean() {
		 try {
			project.accept(new IResourceVisitor() {
			     @Override
			     public boolean visit(IResource resource) throws CoreException {
			         if (IRascalResources.RASCAL_EXT.equals(resource.getFileExtension())) {
			             resource.deleteMarkers(IRascalResources.ID_RASCAL_MARKER, true, IResource.DEPTH_ONE);
			             return false;
			         }

			         return true;
			     }
			 });
		} catch (CoreException e) {
			Activator.log("could not clean markers", e);
		}
	}
}
