package org.rascalmpl.eclipse.library.vis;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Panel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.library.vis.FigurePApplet;

public class FigureViewer extends EditorPart {
	protected static final String editorId = "rascal-eclipse.Figure.viewer";

	public FigureViewer() {
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
		
		
		if (input instanceof FigureEditorInput) {
			setSite(site);
			setInput(input);
		}
		else {
			throw new PartInitException("Input of Figure visualization is not a Figure object");
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

	@SuppressWarnings("serial")
	@Override
	public void createPartControl(Composite parent) {		
		Composite composite = new Composite(parent, SWT.DOUBLE_BUFFERED | SWT.EMBEDDED);
		final FigurePApplet pa = ((FigureEditorInput) getEditorInput()).getFigurePApplet();
		Frame frame = SWT_AWT.new_Frame(composite); 
		frame.setLocation(100,100);
		frame.add(pa);
		pa.init();
		
		composite.addDisposeListener(new DisposeListener() {
			 public void widgetDisposed(DisposeEvent event) {
				 pa.destroy();
			 }
    });
		
		Panel panel = new Panel(new BorderLayout()) {
		     @Override
			public void update(java.awt.Graphics g) {
		       /* Do not erase the background */
		       paint(g);
		     }
		   };
		frame.add(panel);
		frame.setVisible(true);
		frame.pack();
	}

	@Override
	public void setFocus() {
	}
	
	public static void open(final FigurePApplet applet) {
		if (applet == null) {
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
							page.openEditor(new FigureEditorInput(applet), editorId);
						} catch (PartInitException e) {
							Activator.getInstance().logException("failed to open Figure viewer", e);
						}
					
					}
				});
			}
		}
	}
}
