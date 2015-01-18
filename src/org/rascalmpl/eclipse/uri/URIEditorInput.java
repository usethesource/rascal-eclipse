package org.rascalmpl.eclipse.uri;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

public class URIEditorInput implements IStorageEditorInput {
    private URIStorage storage;
    
    public URIEditorInput(URIStorage storage) {
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
       return storage.getURI().toString();
    }
    
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
      return null;
    }
 }
