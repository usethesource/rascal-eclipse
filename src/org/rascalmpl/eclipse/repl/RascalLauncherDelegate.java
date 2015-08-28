package org.rascalmpl.eclipse.repl;

import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
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
				// TODO Auto-generated method stub

			}

			@Override
			protected void saveSettingsForHost(boolean add) {
				// TODO Auto-generated method stub

			}

			@Override
			protected void fillSettingsForHost(String host) {
				// TODO Auto-generated method stub

			}

			@Override
			protected String getHostFromSettings() {
				// TODO Auto-generated method stub
				return null;
			}

		};
	}

	@Override
	public void execute(Map<String, Object> properties, Done done) {
		// TODO Auto-generated method stub
		properties.put(ITerminalsConnectorConstants.PROP_TITLE, "Rascal REPL");
		properties.put(ITerminalsConnectorConstants.PROP_FORCE_NEW, Boolean.TRUE);
		ITerminalService terminal = TerminalServiceFactory.getService();
		// If not available, we cannot fulfill this request
		if (terminal != null) {
			terminal.openConsole(properties, done);
		}

	}

	@Override
	public ITerminalConnector createTerminalConnector(Map<String, Object> properties) {
		ITerminalConnector conn = TerminalConnectorExtension.makeTerminalConnector("rascal-eclipse.connector1");
		ISettingsStore store = new SettingsStore();
		
		ISelection sel = (ISelection) properties.get(ITerminalsConnectorConstants.PROP_SELECTION);
		
		if (sel != null) {
			if (sel instanceof StructuredSelection) {
				StructuredSelection s = (StructuredSelection) sel;
				Object r = s.getFirstElement();
				
				if (r instanceof IResource) {
					store.put("project", ((IResource) r).getProject().getName());
				}
			}
		}
		
		
		
		conn.load(store);
		return conn;
	}

}
