package org.rascalmpl.eclipse.box;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.dialogs.WizardExportResourcesPage;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.rascalmpl.library.box.MakeBox;

@SuppressWarnings("restriction")
public class BoxExportPage extends WizardExportResourcesPage {

	static final private MakeBox makeBox = new MakeBox();
	static final String defaultDir = System.getProperty("DEFAULTDIR", System
			.getProperty("user.home"));
	
	final String pageName;

	static private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		System.err.println("Found: console" + myConsole);
		return myConsole;
	}

	protected BoxExportPage(String pageName, IStructuredSelection selection) {
		super(pageName, selection);
        this.pageName = pageName;
		// TODO Auto-generated constructor stub
	}

	private Text containerNameField;

	private Button containerBrowseButton;

	@Override
	protected boolean hasExportableExtension(String s) {
		if (s == null)
			return true;
		return s.endsWith(".rsc");
	}

	@Override
	protected void createDestinationGroup(Composite parent) {
		// TODO Auto-generated method stub
		// container specification group
		Composite containerGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		containerGroup.setLayout(layout);
		containerGroup.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		containerGroup.setFont(parent.getFont());

		Label resourcesLabel = new Label(containerGroup, SWT.NONE);
		resourcesLabel.setText(IDEWorkbenchMessages.WizardImportPage_folder);
		resourcesLabel.setFont(parent.getFont());
		// container name entry field
		containerNameField = new Text(containerGroup, SWT.SINGLE | SWT.BORDER);
		containerNameField.setText(defaultDir);
		containerNameField.addListener(SWT.Modify, this);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		containerNameField.setLayoutData(data);
		containerNameField.setFont(parent.getFont());
		// container browse button
		containerBrowseButton = new Button(containerGroup, SWT.PUSH);
		containerBrowseButton
				.setText(IDEWorkbenchMessages.WizardImportPage_browse2);
		containerBrowseButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL));
		containerBrowseButton.addListener(SWT.Selection, this);
		containerBrowseButton.setFont(parent.getFont());
		setButtonLayoutData(containerBrowseButton);
	}

	public void handleEvent(Event event) {
		// TODO Auto-generated method stub
		if (event.widget == containerBrowseButton) {
			System.err.println("handleEvent:" + event);
		    destionationDir();
		}
	}

	IResource res;

	class LongRunningOperation implements IRunnableWithProgress {
		private static final int TOTAL_TIME = 10000;

		// private static final int INCREMENT = 500;

		private boolean indeterminate;

		public LongRunningOperation(boolean indeterminate) {
			this.indeterminate = indeterminate;
		}

		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
			monitor.beginTask("Running long running operation",
					indeterminate ? IProgressMonitor.UNKNOWN : TOTAL_TIME);
			System.setProperty("rascal.no_cwd_path", "true");
			URI destdir = null;
			try {
				destdir = new URI("file", currentDir, null);
				makeBox.toRichTxt(pageName, res.getLocationURI(), destdir);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			monitor.done();
			if (monitor.isCanceled())
				throw new InterruptedException(
						"The long running operation was cancelled");
		}
	}

	LongRunningOperation runnable = new LongRunningOperation(false);
	volatile String currentDir;

	void finish() {
		for (Object c : this.getSelectedResources()) {
			res = (IResource) c;
			if (res.getType() == IResource.FILE
					&& hasExportableExtension(res.getName())) {
				// System.err.println(res.getLocationURI());
				currentDir = containerNameField.getText();
				MessageConsole q = findConsole("BoxConsole");
				try {
					getContainer().run(true, true, runnable);
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}
		}
	}

	private void destionationDir() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setFilterPath(defaultDir);
		String dirName = dialog.open();
		if (dirName == null) {
			System.err.println("Canceled");
			System.exit(0);
		}
		containerNameField.setText(dirName);
	}

}
