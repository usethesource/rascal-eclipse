package org.rascalmpl.eclipse.repl;

import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
import org.eclipse.tm.terminal.view.core.TerminalServiceFactory;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService.Done;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanel;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;
import org.eclipse.tm.terminal.view.ui.internal.SettingsStore;
import org.eclipse.tm.terminal.view.ui.launcher.AbstractLauncherDelegate;
import org.eclipse.tm.terminal.view.ui.panels.AbstractExtendedConfigurationPanel;

@SuppressWarnings("restriction")
public class RascalLauncherDelegate extends AbstractLauncherDelegate {

	public RascalLauncherDelegate() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean needsUserConfiguration() {
		return false;
	}

	@Override
	public IConfigurationPanel getPanel(IConfigurationPanelContainer container) {
		return new AbstractExtendedConfigurationPanel(container){

			@Override
			public void setupPanel(Composite parent) {
			  Composite panel = new Composite(parent, SWT.NONE);
			  panel.setLayout(new GridLayout());
			  panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));


			  // Fill the rest of the panel with a label to be able to
			  // set a height and width hint for the dialog
			  Label label = new Label(panel, SWT.HORIZONTAL);
			  GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
			  layoutData.widthHint = 300;
			  layoutData.heightHint = 80;
			  label.setLayoutData(layoutData);

			  setControl(panel);
			}

			@Override
			protected void saveSettingsForHost(boolean add) {
			}

			@Override
			protected void fillSettingsForHost(String host) {
			}

			@Override
			protected String getHostFromSettings() {
				return null;
			}

		};
	}

	@Override
	public void execute(Map<String, Object> properties, Done done) {
		properties.put(ITerminalsConnectorConstants.PROP_TITLE, computeTitle(properties));
		properties.put(ITerminalsConnectorConstants.PROP_FORCE_NEW, Boolean.TRUE);
		ITerminalService terminal = TerminalServiceFactory.getService();
		// If not available, we cannot fulfill this request
		if (terminal != null) {
			terminal.openConsole(properties, done);
		}

	}

	private String computeTitle(Map<String, Object> properties) {
	    return "Rascal [project: " + properties.get("project") + ", mode: " + properties.get("mode") + "]";
    }

    @Override
	public ITerminalConnector createTerminalConnector(Map<String, Object> properties) {
		ITerminalConnector conn = TerminalConnectorExtension.makeTerminalConnector("rascal-eclipse.connector1");
		ISettingsStore store = new SettingsStore();
		String project = (String) properties.get("project");
		String mode = (String) properties.get("mode");
		String module = (String) properties.get("module");
		store.put("project", project);
		store.put("mode", mode);
		store.put("module", module);
		conn.load(store);
		return conn;
	}

}
