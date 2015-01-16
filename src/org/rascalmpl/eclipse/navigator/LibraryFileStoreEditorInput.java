package org.rascalmpl.eclipse.navigator;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

class LibraryFileStoreEditorInput implements IStorageEditorInput {
    private LibraryFileStoreStorage storage;
    
    LibraryFileStoreEditorInput(LibraryFileStoreStorage storage) {
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
       return storage.getStore().toURI().toString();
    }
    
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
      return null;
    }
 }
