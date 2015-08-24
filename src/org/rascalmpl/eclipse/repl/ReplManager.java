package org.rascalmpl.eclipse.repl;

import java.util.LinkedList;
import java.util.List;

public class ReplManager {

    private static final class Instance {
        public static final ReplManager manager = new ReplManager();
    }
    
    public static ReplManager getInstance() {
        return Instance.manager;
    }
    
    private static final List<ReplConnector> connectors = new LinkedList<>();
    
    public void register(ReplConnector connector) {
        if (!connectors.contains(connector)) {
            connectors.add(connector);
        }
    }
    
    public void unregister(ReplConnector connector) {
        connectors.remove(connector);
    }
    
    public ReplConnector findByProject(String project) {
        assert project != null;
        return connectors.stream().filter(x -> project.equals(x.getProject())).findFirst().orElse(null);
    }
}
