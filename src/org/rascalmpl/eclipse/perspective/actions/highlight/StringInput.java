package org.rascalmpl.eclipse.perspective.actions.highlight;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

class StringInput implements IStorageEditorInput {
		private IStorage storage;

		StringInput(IStorage storage) {
			this.storage = storage;
		}

		public boolean exists() {
			return true;
		}

		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		public String getName() {
			return storage.getName();
		}

		public IPersistableElement getPersistable() {
			return null;
		}

		public IStorage getStorage() {
			return storage;
		}

		public String getToolTipText() {
			return "String-based file: " + storage.getName();
		}

		public Object getAdapter(Class adapter) {
			return null;
		}
	}
	