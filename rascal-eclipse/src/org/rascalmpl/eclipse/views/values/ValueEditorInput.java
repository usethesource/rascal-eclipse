package org.rascalmpl.eclipse.views.values;

import static org.rascalmpl.values.uptr.RascalValueFactory.TYPE_STORE_SUPPLIER;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIStorage;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.io.StandardTextWriter;
import io.usethesource.vallang.io.binary.stream.IValueInputStream;

public class ValueEditorInput implements IStorageEditorInput {
	private final IValue value;
	private final String label;
	private final boolean indent;
	private final int tabsize;

	public ValueEditorInput(URIStorage loc, boolean indent, int tabsize) throws IOException, CoreException {
        this.value = parse(loc);
        this.label = loc.toString();
        this.indent = indent;
        this.tabsize = tabsize;
    }
	
    public ValueEditorInput(ISourceLocation loc, boolean indent, int tabsize) throws IOException {
        this.value = parse(loc);
        this.label = loc.toString();
        this.indent = indent;
        this.tabsize = tabsize;
    }
	
	private IValue parse(ISourceLocation loc) throws IOException {
        try (IValueInputStream s = new IValueInputStream(URIResolverRegistry.getInstance().getInputStream(loc), ValueFactoryFactory.getValueFactory(), TYPE_STORE_SUPPLIER)) {
            return s.read();
        } 
    }
	
	private IValue parse(URIStorage loc) throws IOException, CoreException {
	    try (IValueInputStream s = new IValueInputStream(loc.getContents(), ValueFactoryFactory.getValueFactory(), TYPE_STORE_SUPPLIER)) {
            return s.read();
        } 
    }
	
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
		if (obj != null && obj.getClass() == ValueEditorInput.class) {
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
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
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
				ByteArrayOutputStream out = new ByteArrayOutputStream(10_000);
				try (OutputStreamWriter outWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
					StandardTextWriter w = new StandardTextWriter(indent, tabsize);
					w.write(value, outWriter);
					outWriter.flush();
					return new ByteArrayInputStream(out.toByteArray());
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

			@SuppressWarnings("unchecked")
            public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
				return null;
			}
		};
	}
}
