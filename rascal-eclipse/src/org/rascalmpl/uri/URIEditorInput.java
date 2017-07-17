/*******************************************************************************
 * Copyright (c) 2015 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.uri;

import org.eclipse.core.resources.IResource;
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
       return storage.getLocation().toString();
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Object getAdapter(Class adapter) {
    	if (adapter == IResource.class) {
    		return URIResourceResolver.getResource(storage.getLocation());
    	}
    	
    	return null;
    }
    
    @Override
    public boolean equals(Object obj) {
    	if (obj instanceof URIEditorInput) {
    		return ((URIEditorInput) obj).storage.getLocation().top().equals(storage.getLocation().top());
    	}
    	return false;
    }
    
    @Override
    public int hashCode() {
    	return storage.hashCode();
    }
 }
