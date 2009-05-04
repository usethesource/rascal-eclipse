package org.dancingbear.graphbrowser.controller;

import org.dancingbear.graphbrowser.editor.gef.ui.parts.IEditorEventHandler;
import org.dancingbear.graphbrowser.model.IModelGraph;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.jface.action.IAction;

/**
 * The interface for a GraphEditor
 * 
 * @author Erik Slagter
 * 
 */
public interface IGraphEditor {

    /**
     * Set the graph that is editted
     * 
     * @param graph the graph
     */
    public void setGraph(IModelGraph graph);

    /**
     * Get the graph that is editted
     * 
     * @return the graph
     */
    public IModelGraph getGraph();

    /**
     * Add an action to the editor
     * 
     * @param action the action
     * @return success or failure of adding ContextMenuActionItem
     */
    public boolean addContextMenuActionItem(IAction action);

    /**
     * Add an event handler
     * 
     * @param eventHandler the handler
     * @return success or failure of adding an EditorEventHandler
     */
    public boolean addEditorEventHandler(IEditorEventHandler eventHandler);

    /**
     * Get the GraphicalViewer
     * 
     * @return the GraphicalViewer instance
     */
    public GraphicalViewer getViewer();

    /**
     * Gets the zoomManager
     * 
     * @return zoomManager Instance of the zoommanager
     */
    public ZoomManager getZoomManager();

    /**
     * Performs a relayout on the current graph.
     * 
     * @return true if succeeded.
     */
    public boolean relayout();

    /**
     * Performs a filter. It gets the selected nodes and place them in a new
     * tab.
     * 
     * @return true if succeeded
     */
    public boolean filter();

}
