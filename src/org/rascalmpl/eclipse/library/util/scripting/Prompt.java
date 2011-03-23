package org.rascalmpl.eclipse.library.util.scripting;

import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;

public class Prompt {
	
	private final IValueFactory vf;

	public Prompt(IValueFactory vf) {
		this.vf = vf;
	}
	
	public IString prompt(final IString prompt) {
		final String[] value = new String[1];
		
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				final Shell shell = new Shell(activeShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				shell.setText(prompt.getValue());
				
				GridLayout gridLayout = new GridLayout();
				shell.setLayout(gridLayout);
				Label label = new Label(shell, SWT.WRAP);
				label.setText(prompt.getValue());

				GridData data = new GridData();
				Monitor monitor = activeShell.getMonitor();
				int maxWidth = monitor.getBounds().width * 2 / 3;
				int width = label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
				data.widthHint = Math.min(width, maxWidth);
				data.horizontalAlignment = GridData.FILL;
				data.grabExcessHorizontalSpace = true;
				label.setLayoutData (data);

				final Text valueText = new Text(shell, SWT.BORDER);
				if (value[0] != null) valueText.setText(value[0]);
				data = new GridData();
				width = valueText.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
				if (width > maxWidth) data.widthHint = maxWidth;
				data.horizontalAlignment = GridData.FILL;
				data.grabExcessHorizontalSpace = true;
				valueText.setLayoutData(data);

				Composite composite = new Composite(shell, SWT.NONE);
				data = new GridData();
				data.horizontalAlignment = GridData.CENTER;
				composite.setLayoutData (data);
				composite.setLayout(new GridLayout(2, true));

				Button button = new Button(composite, SWT.PUSH);
				button.setText("OK");
				button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				button.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						value[0] = valueText.getText();
						shell.close();
					}	
				});

				shell.pack();
				shell.open();

				Display display = activeShell.getDisplay();
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch()) display.sleep();
				}
			}
		});
		
		if (value[0] != null) {
			return vf.string(value[0]);
		}
		
		throw RuntimeExceptionFactory.io(vf.string("no input"), null, null);
	}
	
	public void alert(final IString prompt) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				final Shell shell = new Shell(activeShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				shell.setText(prompt.getValue());
				
				GridLayout gridLayout = new GridLayout();
				shell.setLayout(gridLayout);
				Label label = new Label(shell, SWT.WRAP);
				label.setText(prompt.getValue());

				GridData data = new GridData();
				Monitor monitor = activeShell.getMonitor();
				int maxWidth = monitor.getBounds().width * 2 / 3;
				int width = label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
				data.widthHint = Math.min(width, maxWidth);
				data.horizontalAlignment = GridData.FILL;
				data.grabExcessHorizontalSpace = true;
				label.setLayoutData (data);

				Composite composite = new Composite(shell, SWT.NONE);
				data = new GridData();
				data.horizontalAlignment = GridData.CENTER;
				composite.setLayoutData (data);
				composite.setLayout(new GridLayout(2, true));

				Button button = new Button(composite, SWT.PUSH);
				button.setText("OK");
				button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				button.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						shell.close();
					}	
				});

				shell.pack();
				shell.open();

				Display display = activeShell.getDisplay();
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch()) display.sleep();
				}
			}
		});
	}
}
