package org.meta_environment.rascal.eclipse.lib;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.ui.graph.Editor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class View {
	
	public static void show(IValue v) {
		Editor.open(v);
	}
	
	public static void edit(final IValue v) {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		final IEditorInput input = new ValueEditorInput(v);

		if (win == null && wb.getWorkbenchWindowCount() != 0) {
			win = wb.getWorkbenchWindows()[0];
		}

		if (win != null) {
			final IWorkbenchPage page = win.getActivePage();
			if (page != null) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						try {
							IEditorPart part = page.openEditor(input, "org.eclipse.ui.DefaultTextEditor");
						} catch (PartInitException e) {
							// TODO Auto-generated catch block
						}
					}
				});
			}
		}
	}

	private static class ValueEditorInput implements IStorageEditorInput {
	    private final IValue value;

	    public ValueEditorInput(IValue value) {
	        this.value = value;
	    }

	    public boolean exists() {
	        return false;
	    }

	    public ImageDescriptor getImageDescriptor() {
	        return null;
	    }

	    public IPersistableElement getPersistable() {
	        return null;
	    }

	    @SuppressWarnings("unchecked")
		public Object getAdapter(Class adapter) {
	        return null;
	    }

	    public String getName() {
	    	return value.getType().toString();
	    }

	    public String getToolTipText() {
	    	return getName();
	    }

	    public IStorage getStorage() throws CoreException {
	        return new IStorage() {
	            public InputStream getContents() throws CoreException {
	                return new ByteArrayInputStream(value.toString().getBytes());
	            }

	            public IPath getFullPath() {
	                return null;
	            }

	            public String getName() {
	                return value.getType().toString();
	            }

	            public boolean isReadOnly() {
	                return true;
	            }

	            @SuppressWarnings("unchecked")
				public Object getAdapter(Class adapter) {
	                return null;
	            }
	        };
	    }
	}
}
