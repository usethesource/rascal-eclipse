package org.eclipse.imp.pdb.ui.tree;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.pdb.facts.IBool;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IDateTime;
import org.eclipse.imp.pdb.facts.IExternalValue;
import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.INode;
import org.eclipse.imp.pdb.facts.IRational;
import org.eclipse.imp.pdb.facts.IReal;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.impl.fast.ValueFactory;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.eclipse.imp.pdb.ui.PDBUIPlugin;
import org.eclipse.imp.pdb.ui.ValueEditorInput;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

public class Editor extends EditorPart {
	public static final String EditorId = "org.eclipse.imp.pdb.ui.tree.editor";
	private TreeViewer treeViewer;
	private static final Object[] empty = new Object[0];
	
	public Editor() {
	}

	@Override
	public String getTitle() {
		IEditorInput editorInput = getEditorInput();
		
		if (editorInput != null) {
			return editorInput.getName();
		}
		
		return "Value";
	}
	
	public TreeViewer getViewer() {
		return treeViewer;
	}

	public static void open(final IValue value) {
		if (value == null) {
			return;
		}
	 	IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

		if (win == null && wb.getWorkbenchWindowCount() != 0) {
			win = wb.getWorkbenchWindows()[0];
		}
		
		if (win != null) {
			final IWorkbenchPage page = win.getActivePage();

			if (page != null) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						try {
							page.openEditor(new ValueEditorInput(value, true, 2), Editor.EditorId);
						} catch (PartInitException e) {
							PDBUIPlugin.getDefault().logException("failed to open tree editor", e);
						}
					}
				});
			}
		}
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		if (!(input instanceof ValueEditorInput)) {
			throw new PartInitException("not a value input");
		}
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		treeViewer = new TreeViewer(parent);
		treeViewer.setContentProvider(new ValueContentProvider());
		treeViewer.setLabelProvider(new ValueLabelProvider());
		
		IEditorInput input = getEditorInput();
		treeViewer.setInput(((ValueEditorInput) input).getValue());
	}

	@Override
	public void setFocus() {
	}
	
	private class ValueContentProvider implements ITreeContentProvider {
		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getChildren(Object parentElement) {
			try {
				return ((IValue) parentElement).accept(new IValueVisitor<Object[]>() {
					

					public Object[] visitBoolean(IBool boolValue)
							throws VisitorException {
						return empty; 
					}

					public Object[] visitConstructor(IConstructor o)
							throws VisitorException {
						Object[] children = new Object[o.arity()];
						int i = 0;
						for (IValue child : o) {
							children[i++] = child;
						}
						return children;
					}

					public Object[] visitExternal(IExternalValue externalValue)
							throws VisitorException {
						return empty;
					}

					public Object[] visitInteger(IInteger o)
							throws VisitorException {
						return empty;
					}

					public Object[] visitRational(IRational o)
							throws VisitorException {
						return empty;
					}

					public Object[] visitList(IList o) throws VisitorException {
						Object[] children = new Object[o.length()];
						int i = 0;
						for (IValue child : o) {
							children[i++] = child;
						}
						return children;
					}

					public Object[] visitMap(IMap o) throws VisitorException {
						Object[] children = new Object[o.size()];
						int i = 0;
						for (IValue child : o) {
							children[i++] = ValueFactory.getInstance().tuple(child, o.get(child));
						}
						return children;
					}

					public Object[] visitNode(INode o) throws VisitorException {
						Object[] children = new Object[o.arity()];
						int i = 0;
						for (IValue child : o) {
							children[i++] = child;
						}
						return children;
					}

					public Object[] visitReal(IReal o) throws VisitorException {
						return empty;
					}

					public Object[] visitRelation(ISet o)
							throws VisitorException {
						return visitSet(o);
					}

					public Object[] visitSet(ISet o) throws VisitorException {
						Object[] children = new Object[o.size()];
						int i = 0;
						for (IValue child : o) {
							children[i++] = child;
						}
						return children;
					}

					public Object[] visitSourceLocation(ISourceLocation o)
							throws VisitorException {
						return empty;
					}

					public Object[] visitString(IString o) throws VisitorException {
						return empty;
					}

					public Object[] visitTuple(ITuple o) throws VisitorException {
						Object[] children = new Object[o.arity()];
						int i = 0;
						for (IValue child : o) {
							children[i++] = child;
						}
						return children;
					}

					public Object[] visitDateTime(IDateTime o)
							throws VisitorException {
						return empty;
					}

          public Object[] visitListRelation(IList o) throws VisitorException {
            return visitList(o);
          }
				});
			} catch (VisitorException e) {
				return null;
			}
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length != 0;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
	}
	
	private class ValueLabelProvider implements ILabelProvider {
		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			IValue value = (IValue) element;
			try {
				return value.accept(new IValueVisitor<String>() {

					public String visitBoolean(IBool boolValue)
							throws VisitorException {
						return boolValue.toString();
					}

					public String visitConstructor(IConstructor o)
							throws VisitorException {
						return o.getConstructorType().toString();
					}

					public String visitExternal(IExternalValue externalValue)
							throws VisitorException {
						return externalValue.getType().toString();
					}

					public String visitInteger(IInteger o) throws VisitorException {
						return o.toString();
					}

					public String visitRational(IRational o) throws VisitorException {
						return o.toString();
					}

					public String visitList(IList o) throws VisitorException {
						return o.getType().toString();
					}

					public String visitMap(IMap o) throws VisitorException {
						return o.getType().toString();
					}

					public String visitNode(INode o) throws VisitorException {
						return o.getName();
					}

					public String visitReal(IReal o) throws VisitorException {
						return o.toString();
					}

					public String visitRelation(ISet o)
							throws VisitorException {
						return o.getType().toString();
					}

					public String visitSet(ISet o) throws VisitorException {
						return o.getType().toString();
					}

					public String visitSourceLocation(ISourceLocation o)
							throws VisitorException {
						return o.toString();
					}

					public String visitString(IString o) throws VisitorException {
						return o.toString();
					}

					public String visitTuple(ITuple o) throws VisitorException {
						return o.getType().toString();
					}

					public String visitDateTime(IDateTime o)
							throws VisitorException {
						return o.toString();
					}

          public String visitListRelation(IList o) throws VisitorException {
            return o.getType().toString();
          }
				});
			} catch (VisitorException e) {
				return "...";
			}
		}
	}
}
