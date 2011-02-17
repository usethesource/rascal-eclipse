package org.rascalmpl.eclipse.console;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.rascalmpl.eclipse.IRascalResources;

public class RascalModuleUpdateListener implements IResourceChangeListener {
	private RascalScriptInterpreter interpreter;

	public RascalModuleUpdateListener(RascalScriptInterpreter interpreter) {
		this.interpreter = interpreter;
	}

	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta[] deltas = event.getDelta().getAffectedChildren();

		try {
			for (IResourceDelta d : deltas) {

				d.accept(new IResourceDeltaVisitor() {
					public boolean visit(IResourceDelta delta)
							throws CoreException {
						IResource resource = delta.getResource();
						
						if (resource instanceof IFile) {
							IPath path = resource.getLocation();
							
							if (path.getFileExtension().equals(IRascalResources.RASCAL_EXT))  {
								switch (delta.getKind()) {
								case IResourceDelta.OPEN:
									break;
								case IResourceDelta.ADDED:
									break;
								case IResourceDelta.CHANGED:
									if (delta.getFlags() != IResourceDelta.MARKERS) {
										// only if its not just the markers
										notify(path);
									}
									break;
								case IResourceDelta.REMOVED:
									break;
								}
							}
							return false;
						}
						return true;
					}

					private void notify(IPath path) {
						IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
						IProject proj = file.getProject();
						if (proj != null && proj.exists()) {
							IFolder srcFolder = proj.getFolder(IRascalResources.RASCAL_SRC);
							if (srcFolder != null && srcFolder.exists()) {
								if (srcFolder.getProjectRelativePath().isPrefixOf(file.getProjectRelativePath())) {
									try{
										URI uri = new URI("project://" + proj.getName() + "/" + file.getProjectRelativePath().removeFirstSegments(1).toPortableString());
										interpreter.addDirtyModule(uri);
									}catch(URISyntaxException usex){
										usex.printStackTrace(); // TODO Change to something better.
									}
								}
							}
						}
					}
				});

			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}
