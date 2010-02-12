package org.rascalmpl.eclipse.debug.ui.breakpoints;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.ui.texteditor.ITextEditor;


public class RascalEditorAdapterFactory implements IAdapterFactory {

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof UniversalEditor) {
			ITextEditor editorPart = (ITextEditor) adaptableObject;
			IResource resource = (IResource) editorPart.getEditorInput().getAdapter(IResource.class);
			if (resource != null) {
				String extension = resource.getFileExtension();
				if (extension != null && extension.equals("rsc")) {
				    if (adapterType.equals(IToggleBreakpointsTarget.class)) {
				        return new RascalBreakpointAdapter();
				    }
				}
			}			
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[]{IToggleBreakpointsTarget.class};
	}

}
