package org.rascalmpl.eclipse.repl;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate;
import org.eclipse.tm.terminal.view.ui.launcher.LauncherDelegateManager;
import org.rascalmpl.eclipse.Activator;

public class RascalTerminalRegistry {

    private static final class Instance {
        public static final RascalTerminalRegistry manager = new RascalTerminalRegistry();
    }
    
    public static RascalTerminalRegistry getInstance() {
        return Instance.manager;
    }
    
    private final List<WeakReference<RascalTerminalConnector>> connectors = new LinkedList<>();
    private WeakReference<RascalTerminalConnector> focussed = null;
    
    public void register(RascalTerminalConnector connector) {
        if (!connectors.stream().anyMatch(x -> x.get() == connector)) {
            connectors.add(new WeakReference<>(connector));
        }
    }
    
    public void unregister(RascalTerminalConnector connector) {
        for (int i = 0; i < connectors.size(); i++) {
            if (connectors.get(i).get() == connector) {
                connectors.remove(i);
                break;
            }
        }
    }
    
    public RascalTerminalConnector findByProject(String project) {
        assert project != null;
        return connectors.stream().map(x -> x.get()).filter(x -> x != null && project.equals(x.getProject())).findFirst().orElse(null);
    }

    public void setActive(RascalTerminalConnector connector) {
        this.focussed = new WeakReference<>(connector);
    }
    
    public RascalTerminalConnector getActiveConnector(String project) {
        if (project != null) {
            RascalTerminalConnector f = focussed != null ? focussed.get() : null;
            
            if (f != null && connectors.stream().anyMatch(x -> x.get() == f) && f.getProject().equals(project)) {
                return f;
            }
            
            return findByProject(project);
        }
        else if (connectors.size() > 0) {
            return connectors.stream().map(x -> x.get()).filter(x -> x != null).findFirst().orElse(null);
        }
        else {
            return null;
        }
    }
    
    public void queueCommand(String project, String cmd) {
        RascalTerminalConnector connector = RascalTerminalRegistry.getInstance().getActiveConnector(project);
        if (connector != null) {
            connector.setFocus();
            connector.queueCommand(cmd);
        }
        else {
            Activator.log("No terminal available for project " + project, new NullPointerException());
        }
    }
    
    public void queueCommands(String project, Iterable<String> cmds) {
        RascalTerminalConnector connector = RascalTerminalRegistry.getInstance().getActiveConnector(project);
        if (connector != null) {
            connector.setFocus();
            StringBuilder b = new StringBuilder();
            for (String cmd : cmds) {
                b.append(cmd);
                b.append('\n');
            }
            connector.queueCommand(b.toString());
        }
        else {
            Activator.log("No terminal available for project " + project, new NullPointerException());
        }
    }
    
    /**
     * @param project
     * @param mode
     * @param module
     */
    public static void terminalForProject(String project, String mode, String module) {
        ILauncherDelegate delegate = LauncherDelegateManager.getInstance().getLauncherDelegate("org.rascalmpl.eclipse.rascal.launcher", false);

        if (delegate != null) {
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(ITerminalsConnectorConstants.PROP_DELEGATE_ID, delegate.getId());
            properties.put("project", project);
            properties.put("mode", mode);
            properties.put("module", module);
            delegate.execute(properties, null);
        }
    }

    public static void terminal(ISelection selection) {
        terminalForProject(null, "debug", null);
        // TODO: start a terminal based on the current selection
    }
    
    public static void terminal() {
        terminalForProject(null, "debug", null);
    }

}
