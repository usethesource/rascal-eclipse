package org.eclipse.imp.pdb.ui.graph;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class ValueEditorInput implements IEditorInput {
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
		return null;
	}
}
