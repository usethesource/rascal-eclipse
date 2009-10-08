package org.eclipse.imp.pdb.ui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

public class ValueEditorInput implements IStorageEditorInput {
	private final IValue value;
	private final String label;

	public ValueEditorInput(IValue value) {
		this.value = value;
		this.label = value.getType().toString();
	}
	
	public ValueEditorInput(String label, IValue value) {
		this.value = value;
		this.label = label;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() == ValueEditorInput.class) {
			return value.equals(((ValueEditorInput) obj).value);
		}
		return false;
	}
	
	public IValue getValue() {
		return value;
	}
	
	public boolean exists() {
		return value != null;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return label;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return getName();
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (IValue.class.equals(adapter)) {
			return value;
		}
		else if (IValue.class.isAssignableFrom(adapter)) {
			return value;
		}
		return null;
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
				return false;
			}

			public Object getAdapter(Class adapter) {
				return null;
			}
		};
	}
}
