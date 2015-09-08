package org.rascalmpl.eclipse.debug.core.sourcelookup;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;

public class DummyConsoleSourceContainer implements ISourceContainer {

  @SuppressWarnings("rawtypes")
  @Override
  public Object getAdapter(Class adapter) {
      return null;
  }

  @Override
  public void init(ISourceLookupDirector director) {  }

  @Override
  public Object[] findSourceElements(String name) throws CoreException {
    if (name.equals(RascalSourceLookupParticipant.RASCAL_CONSOLE_DUMMY) || name.startsWith("stdin:") || name.startsWith("prompt:")) {
      return new Object[] { new IFileStore() {
        
        @SuppressWarnings("rawtypes")
        @Override
        public Object getAdapter(Class adapter) {
            return null;
        }
        
        @Override
        public URI toURI() {
          return null;
        }
        
        @Override
        public File toLocalFile(int options, IProgressMonitor monitor) throws CoreException {
          return null;
        }
        
        @Override
        public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
          
        }
        
        @Override
        public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
          return null;
        }
        
        @Override
        public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
          return null;
        }
        
        @Override
        public void move(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
          
        }
        
        @Override
        public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
          return null;
        }
        
        @Override
        public boolean isParentOf(IFileStore other) {
          return false;
        }
        
        @Override
        public IFileStore getParent() {
          return null;
        }
        
        @Override
        public String getName() {
          return RascalSourceLookupParticipant.RASCAL_CONSOLE_DUMMY;
        }
        
        @Override
        public IFileSystem getFileSystem() {
          return null;
        }
        
        @Override
        public IFileStore getFileStore(IPath path) {
          return null;
        }
        
        @Override
        public IFileStore getChild(String name) {
          return null;
        }
        
        @Override
        public IFileStore getChild(IPath path) {
          return null;
        }
        
        @Override
        public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
          return null;
        }
        
        @Override
        public IFileInfo fetchInfo() {
          return null;
        }
        
        @Override
        public void delete(int options, IProgressMonitor monitor) throws CoreException {
          
        }
        
        @Override
        public void copy(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
          
        }
        
        @Override
        public IFileStore[] childStores(int options, IProgressMonitor monitor) throws CoreException {
          return null;
        }
        
        @Override
        public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
          return null;
        }
        
        @Override
        public IFileInfo[] childInfos(int options, IProgressMonitor monitor) throws CoreException {
          return null;
        }
      } };
    }
    
    return null;
  }

  @Override
  public String getName() {
    return "Dummy console editor";
  }

  @Override
  public ISourceContainer[] getSourceContainers() throws CoreException {
    return new ISourceContainer[] { new DummyConsoleSourceContainer() };
  }

  @Override
  public boolean isComposite() {
    return false;
  }

  @Override
  public ISourceContainerType getType() {
    return null;
  }

  @Override
  public void dispose() {
  }

}
