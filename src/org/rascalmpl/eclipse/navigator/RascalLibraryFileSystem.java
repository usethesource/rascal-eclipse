/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.navigator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.util.RascalEclipseManifest;
import org.rascalmpl.uri.URIUtil;

// TODO: link this stuff with the rascal search path instead
public class RascalLibraryFileSystem extends FileSystem {
  public static final String SCHEME = "rascal-library";
	protected Map<String, IFileStore> roots = new HashMap<String, IFileStore>();

	public static RascalLibraryFileSystem getInstance() throws CoreException {
	  return (RascalLibraryFileSystem) EFS.getFileSystem(SCHEME);
	}
	
	public RascalLibraryFileSystem() {
	  IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
        .getExtensionPoint("rascal_eclipse", "rascalLibrary");

    if (extensionPoint == null) {
      return; // this may happen when nobody extends this point.
    }
    
    for (IExtension element : extensionPoint.getExtensions()) {
      String name = element.getContributor().getName();
      Bundle bundle = Platform.getBundle(name);
      List<String> src = new RascalEclipseManifest().getSourceRoots(bundle);
      roots.put(name, new RascalLibraryFileStore(bundle, src.get(0)));
    }
	}

	public Map<String, IFileStore> getRoots() {
	  return roots;
	}
	
	@Override
	public IFileStore getStore(URI uri) {
		if (!uri.getScheme().equals(SCHEME)) {
			return null;
		}
		
		if (uri.getPath() != null && uri.getPath().length() > 0) {
			return roots.get(uri.getAuthority()).getChild(uri.getPath());
		}
		else {
		  return roots.get(uri.getAuthority());
		}
	}
	
	public class RascalLibraryFileStore extends FileStore {
			private final RascalLibraryFileStore parent;
      private final Bundle bundle;
      private final String root;
      private final String path;
	
			public RascalLibraryFileStore(Bundle bundle, String path, RascalLibraryFileStore parent) {
				this.parent = parent;
			  this.bundle = bundle;
        this.path = path;
        this.root = parent.root;
			}
			
			public RascalLibraryFileStore(Bundle bundle, String root) {
				this.parent = null;
				this.bundle = bundle;
				this.root = root;
				this.path = "";
			}
			
			@Override
			public boolean equals(Object obj) {
				if (obj == null) {
					return false;
				}
				
				if (obj.getClass() == getClass()) {
					RascalLibraryFileStore other = (RascalLibraryFileStore) obj;
					return bundle.getBundleId() == other.bundle.getBundleId() 
					    && path.equals(other.path);
				}
				return false;
			}
			
			@Override
			public int hashCode() {
				return path.hashCode();
			}
			
			
			@Override
			public String[] childNames(int options, IProgressMonitor monitor)
			    throws CoreException {
        Enumeration<URL> found = bundle.findEntries(getFullPath().toString(), null, false);
			  List<String> list = new LinkedList<>();

			  while (found != null && found.hasMoreElements()) {
          Path elemPath = new Path(found.nextElement().getPath());
			    
          if (isDirectory(elemPath.toString())) {
            list.add(0, elemPath.lastSegment());
          }
          else if (isRascalFile(elemPath)) {
			      list.add(list.size(), elemPath.lastSegment());
			    }
			  }

			  return list.toArray(new String[list.size()]);
			}

      private boolean isRascalFile(Path elemPath) {
    	if (elemPath.getFileExtension() == null)
    		return false;
        return elemPath.getFileExtension().equals(IRascalResources.RASCAL_EXT);
      }

      private IPath getFullPath() {
        return new Path(root).append(path);
      }
			
			boolean isDirectory(String path) {
			  Enumeration<URL> contents = bundle.findEntries(path.toString(), null, false);
			  return contents != null && contents.hasMoreElements();
			}

			@Override
			public IFileInfo fetchInfo(int options, IProgressMonitor monitor)
					throws CoreException {
				FileInfo info = new FileInfo(getName());
				if (isDirectory(getFullPath().toString())) {
					info.setDirectory(true);
				} else {
					info.setDirectory(false);
				}
				
				info.setExists(bundle.getEntry(getFullPath().toString()) != null);
				info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
				info.setAttribute(EFS.ATTRIBUTE_IMMUTABLE, true);
				return info;
			}
			
			@Override
			public IFileStore getChild(String name) {
			  if (isDirectory(getFullPath().toString())) {
			    return new RascalLibraryFileStore(bundle, new Path(path).append(name).toString(), this);
			  }

			  return null;
			}
	
			@Override
			public String getName() {
			  return path.length() > 0 ? new Path(path).lastSegment() : "";
			}
	
			@Override
			public IFileStore getParent() {
				return parent;
			}
	
			@Override
			public InputStream openInputStream(int options, IProgressMonitor monitor)
					throws CoreException {
			  try {
			    URL entry = bundle.getEntry(getFullPath().toString());
			    if (entry != null) {
			      return entry.openStream();
			    }
			    else {
			      throw new FileNotFoundException(getFullPath().toString());
			    }
        } catch (IOException e) {
          throw new CoreException(new Status(IStatus.ERROR, "", e.getMessage()));
        }
			}
	
			@Override
			public URI toURI() {
			  return URIUtil.assumeCorrect("rascal-library", bundle.getSymbolicName(), "/" + path);
			}
		}

}
