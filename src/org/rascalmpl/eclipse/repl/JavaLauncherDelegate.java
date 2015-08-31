package org.rascalmpl.eclipse.repl;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
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
import org.rascalmpl.eclipse.Activator;

@SuppressWarnings("restriction")
public class JavaLauncherDelegate extends AbstractLauncherDelegate {
    ILaunchConfiguration selected = null;
    
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
              panel.setLayout(new RowLayout());

              // Fill the rest of the panel with a label to be able to
              // set a height and width hint for the dialog
              Label label = new Label(panel, SWT.HORIZONTAL);
              label.setText("Choose a run configuration:");
              
              Combo configs = new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);

              ILaunchConfiguration[] launches = getJavaLaunchConfigs();
              for (ILaunchConfiguration config : launches) {
                  configs.add(config.getName());
              }

              configs.addSelectionListener(new SelectionAdapter() {
                  public void widgetSelected(SelectionEvent e) {
                      selected = launches[e.x];
                  }
              });

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
		properties.put(ITerminalsConnectorConstants.PROP_TITLE, "Java Terminal");
		properties.put(ITerminalsConnectorConstants.PROP_FORCE_NEW, Boolean.TRUE);
		properties.put("launchConfiguration", selected);
		ITerminalService terminal = TerminalServiceFactory.getService();
		if (terminal != null) {
			terminal.openConsole(properties, done);
		}
	}

	public static ILaunchConfiguration[] getJavaLaunchConfigs() {
         ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
         ILaunchConfigurationType type = launchManager.getLaunchConfigurationType("org.eclipse.jdt.launching.localJavaApplication");

         try {
             return launchManager.getLaunchConfigurations(type);
         } catch (CoreException e) {
             IStatus message = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
             Activator.getInstance().getLog().log(message);
             return new ILaunchConfiguration[0];
         }
     }
       
	@Override
	public ITerminalConnector createTerminalConnector(Map<String, Object> properties) {
		ITerminalConnector conn = TerminalConnectorExtension.makeTerminalConnector("rascal-eclipse.java.connector");
		ISettingsStore store = new SettingsStore();
		store.put("launchConfiguration", selected.getName());
		conn.load(store);
		return conn;
	}
}
