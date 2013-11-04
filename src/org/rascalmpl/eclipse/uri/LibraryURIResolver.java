package org.rascalmpl.eclipse.uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.navigator.RascalLibraryFileSystem;
import org.rascalmpl.uri.IURIInputStreamResolver;

public class LibraryURIResolver implements IURIInputStreamResolver {
  private final RascalLibraryFileSystem fs;
  
  public LibraryURIResolver() {
    RascalLibraryFileSystem fs = null;
    
    try {
      fs = RascalLibraryFileSystem.getInstance();
    } 
    catch (CoreException e) {
      Activator.log("could not start library resolver", e);
    }
    
    this.fs = fs;
  }
  
  @Override
  public InputStream getInputStream(URI uri) throws IOException {
    IFileStore file = resolve(uri);
    
    if (file == null || !file.fetchInfo().exists()) {
      throw new FileNotFoundException(uri.toASCIIString());
    }
    
    try {
      return file.openInputStream(EFS.NONE, null);
    } catch (CoreException e) {
      throw new IOException(e);
    }
  }

  private IFileStore resolve(URI uri) throws IOException {
    if (uri.getHost().length() <= 0) {
      throw new IOException();
    }
    
    Map<String, IFileStore> roots = fs.getRoots();
    IFileStore root = roots.get(uri.getHost());
    
    String path = uri.getPath();
    
    if (path == null || path.length() == 0 || path.equals("/")) {
      return root;
    }
    
    return root.getFileStore(new Path(path));
  }

  @Override
  public Charset getCharset(URI uri) throws IOException {
    return Charset.forName("UTF-8");
  }

  @Override
  public boolean exists(URI uri) {
    try {
      return resolve(uri).fetchInfo().exists();
    }
    catch (IOException e) {
      return false;
    }
  }

  @Override
  public long lastModified(URI uri) throws IOException {
    return resolve(uri).fetchInfo().getLastModified();
  }

  @Override
  public boolean isDirectory(URI uri) {
    try {
      return resolve(uri).fetchInfo().isDirectory();
    } catch (IOException e) {
      return false;
    }
  }

  @Override
  public boolean isFile(URI uri) {
    return !isDirectory(uri);
  }

  @Override
  public String[] listEntries(URI uri) throws IOException {
    try {
      return resolve(uri).childNames(EFS.NONE, null);
    } catch (CoreException e) {
      throw new IOException(e);
    }
  }

  @Override
  public String scheme() {
    return "rascal-library";
  }

  @Override
  public boolean supportsHost() {
    return false;
  }
}
