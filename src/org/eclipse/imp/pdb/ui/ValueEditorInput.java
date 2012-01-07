package org.eclipse.imp.pdb.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.io.StandardTextWriter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

public class ValueEditorInput implements IStorageEditorInput {
	private final IValue value;
	private final String label;
	private final boolean indent;
	private final int tabsize;

	public ValueEditorInput(IValue value, boolean indent, int tabsize) {
		this.value = value;
		this.label = value.getType().toString();
		this.indent = indent;
		this.tabsize = tabsize;
	}
	
	public ValueEditorInput(String label, IValue value, boolean indent, int tabsize) {
		this.value = value;
		this.label = label;
		this.indent = indent;
		this.tabsize = tabsize;
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
				try {
					StringWriter out = new StringWriter(10000);
					StandardTextWriter w = new StandardTextWriter(indent, tabsize);
					w.write(value, out);
					return new ByteArrayInputStream(out.toString().getBytes());
				} catch (IOException e) {
					throw new CoreException(Status.OK_STATUS);
				}
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
