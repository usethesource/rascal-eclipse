package org.rascalmpl.eclipse.editor.highlight;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

class StringInput implements IStorageEditorInput {
		private IStorage storage;

		StringInput(IStorage storage) {
			this.storage = storage;
		}

		@Override
		public boolean exists() {
			return true;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public String getName() {
			return storage.getName();
		}

		@Override
		public IPersistableElement getPersistable() {
			return null;
		}

		@Override
		public IStorage getStorage() {
			return storage;
		}

		@Override
		public String getToolTipText() {
			return "String-based file: " + storage.getName();
		}

		@Override
		public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
			return null;
		}
	}
	