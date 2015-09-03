package org.rascalmpl.eclipse.repl;

import java.util.LinkedList;
import java.util.List;

import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.commands.RascalTerminalLaunchHandler;

public class RascalTerminalRegistry {

    private static final class Instance {
        public static final RascalTerminalRegistry manager = new RascalTerminalRegistry();
    }
    
    public static RascalTerminalRegistry getInstance() {
        return Instance.manager;
    }
    
    private final List<RascalTerminalConnector> connectors = new LinkedList<>();
    private RascalTerminalConnector focussed = null;
    
    public void register(RascalTerminalConnector connector) {
        if (!connectors.contains(connector)) {
            connectors.add(connector);
        }
    }
    
    public void unregister(RascalTerminalConnector connector) {
        connectors.remove(connector);
    }
    
    public RascalTerminalConnector findByProject(String project) {
        assert project != null;
        return connectors.stream().filter(x -> project.equals(x.getProject())).findFirst().orElse(null);
    }

    public void setActive(RascalTerminalConnector connector) {
        this.focussed = connector;
    }
    
    public RascalTerminalConnector getActiveConnector(String project) {
        if (project != null) {
            if (connectors.contains(focussed) && focussed.getProject().equals(project)) {
                return focussed;
            }
            
            RascalTerminalConnector connector = findByProject(project);
            
            if (connector != null) {
                return connector;
            }
            
            return null;
        }
        else if (connectors.size() > 0) {
            return connectors.get(0);
        }
        else {
            return null;
        }
    }
    
    public void queueCommand(String project, String cmd) {
        RascalTerminalConnector connector = RascalTerminalRegistry.getInstance().getActiveConnector(project);
        if (connector != null) {
            connector.queueCommand(cmd);
            connector.setFocus();
        }
        else {
            Activator.log("No terminal available for project " + project, new NullPointerException());
        }
    }
    
    public void queueCommands(String project, Iterable<String> cmds) {
        RascalTerminalConnector connector = RascalTerminalRegistry.getInstance().getActiveConnector(project);
        if (connector != null) {
            for (String cmd : cmds) {
                connector.queueCommand(cmd);
            }
            connector.setFocus();
        }
        else {
            Activator.log("No terminal available for project " + project, new NullPointerException());
        }
    }
}
