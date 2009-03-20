/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.ui.parts;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.dancingbear.graphbrowser.controller.EditorController;
import org.dancingbear.graphbrowser.controller.IGraphEditor;
import org.dancingbear.graphbrowser.editor.ErrorHandler;
import org.dancingbear.graphbrowser.editor.gef.GraphContextMenuContributor;
import org.dancingbear.graphbrowser.editor.gef.TopologicalKeyHandler;
import org.dancingbear.graphbrowser.editor.gef.factories.GraphEditPartFactory;
import org.dancingbear.graphbrowser.editor.gef.ui.parts.palette.PaletteFactory;
import org.dancingbear.graphbrowser.editor.jface.action.FilterAction;
import org.dancingbear.graphbrowser.editor.jface.action.RelayoutAction;
import org.dancingbear.graphbrowser.editor.ui.input.GraphEditorInput;
import org.dancingbear.graphbrowser.model.IModelGraph;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.GraphicalEditorWithPalette;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Graphical editor that allows the editing of graph objects.
 * 
 * @author Jeroen van Schagen
 * @author Erik Slagter
 * @author Jeroen van Lieshout
 * @date 11-03-2009
 */
public class GraphEditor extends GraphicalEditorWithPalette implements
        IGraphEditor {

    public static final String ID = "GraphBrowser.VisualEditor";

    private static final int MIN_ZOOM_LEVEL = 25;
    private static final int MAX_ZOOM_LEVEL = 200;
    private static final int ZOOM_LEVEL_RATE = 5;
    private static final boolean USE_ZOOM_LEVELS = true;
    private static final double ZOOM_SCALE = 100.0;

    protected IModelGraph graph = null;
    private ZoomManager manager = null;

    // TODO: Needs refactoring so the controller is not referrenced by the
    // Grapheditor. To do this, the TopologicalKeyHandler in
    // org.dancingbear.graphbrowser.editor.gef needs to be changed.
    private EditorController controller;
    private String fileName = "";

    private List<IEditorEventHandler> eventHandlers = new ArrayList<IEditorEventHandler>();

    public GraphEditor() {
        setEditDomain(new DefaultEditDomain(this));

        CommandStackListener listener = new CommandStackListener() {
            public void commandStackChanged(EventObject event) {
                firePropertyChange(PROP_DIRTY);
            }
        };
        getCommandStack().addCommandStackListener(listener);
    }

    /**
     * Configure contextmenu
     */
    private void configureContextMenu() {
        ContextMenuProvider provider = new GraphContextMenuContributor(this,
                getActionRegistry());
        getGraphicalViewer().setContextMenu(provider);
        getSite().registerContextMenu(provider, getViewer());
    }

    /**
     * Configure GraphicalViewer
     */
    @Override
    protected void configureGraphicalViewer() {
        getGraphicalViewer().setEditPartFactory(new GraphEditPartFactory());
        configureZoomManager();
        configureKeyHandler();
        configureContextMenu();

    }

    /**
     * Configure KeyHandlers
     */
    private void configureKeyHandler() {
        KeyHandler keyHandler = new TopologicalKeyHandler(getGraphicalViewer(),
                this);
        keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
                getActionRegistry().getAction(ActionFactory.DELETE.getId()));
        keyHandler.put(KeyStroke.getPressed('+', SWT.KEYPAD_ADD, 0),
                getActionRegistry().getAction(GEFActionConstants.ZOOM_IN));
        keyHandler.put(KeyStroke.getPressed('-', SWT.KEYPAD_SUBTRACT, 0),
                getActionRegistry().getAction(GEFActionConstants.ZOOM_OUT));
        getGraphicalViewer().setProperty(
                MouseWheelHandler.KeyGenerator.getKey(SWT.NONE),
                MouseWheelZoomHandler.SINGLETON);
        getGraphicalViewer().setKeyHandler(keyHandler);
    }

    /**
     * Configure ZoomManager
     */
    private void configureZoomManager() {
        // Set scalable root edit part
        ScalableRootEditPart rootEditPart = new ScalableRootEditPart();
        getGraphicalViewer().setRootEditPart(rootEditPart);

        // Initiate zoom functionality
        manager = rootEditPart.getZoomManager();
        getActionRegistry().registerAction(new ZoomInAction(manager));
        getActionRegistry().registerAction(new ZoomOutAction(manager));

        List<String> zoomContributions = new ArrayList<String>();
        zoomContributions.add(ZoomManager.FIT_ALL);
        zoomContributions.add(ZoomManager.FIT_HEIGHT);
        zoomContributions.add(ZoomManager.FIT_WIDTH);
        manager.setZoomLevelContributions(zoomContributions);

        manager.setZoomLevels(getZoomLevels());
        manager.setZoomAnimationStyle(ZoomManager.ANIMATE_ZOOM_IN_OUT);
    }

    /**
     * Get ZoomManager
     * 
     * @return zoomManager
     */
    public ZoomManager getZoomManager() {
        return manager;
    }

    /**
     * Get ZoomLevels
     * 
     * @return zoomLevels
     */
    private double[] getZoomLevels() {
        double[] zoomLevels = null;

        if (USE_ZOOM_LEVELS) {
            int zoomSize = (MAX_ZOOM_LEVEL - MIN_ZOOM_LEVEL) / ZOOM_LEVEL_RATE
                    + 1;
            zoomLevels = new double[zoomSize];
            for (int i = 0; i < zoomSize; i++) {
                zoomLevels[i] = (MIN_ZOOM_LEVEL + i * ZOOM_LEVEL_RATE)
                        / ZOOM_SCALE;
            }
        } else {
            zoomLevels = new double[2];
            zoomLevels[0] = 1;
        }
        return zoomLevels;
    }

    /**
     * Save operation
     * 
     * @param monitor ProgressMonitor to provide progress
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
        if ("".equals(this.fileName)) {
            doSaveAs();
        } else {

            fireSaveEvent();

            // Clear commandstack so editor is not dirty anymore
            getCommandStack().flush();
        }
    }

    /**
     * Save As operation
     */
    @Override
    public void doSaveAs() {
        FileDialog dialog = new FileDialog(this.getViewer().getControl()
                .getShell(), SWT.SAVE);
        String fileDialogResult = dialog.open();

        if (fileDialogResult != null) {
            this.fileName = fileDialogResult;
            fireSaveEvent();

            // Clear commandstack so editor is not dirty anymore
            getCommandStack().flush();
        }
    }

    /**
     * fires a Save Event to its registered handlers.
     */
    private void fireSaveEvent() {
        ArrayList<Object> values = new ArrayList<Object>();
        values.add(graph);
        values.add(fileName);
        fireEvent(EventType.Save, values);
    }

    /**
     * Function to activate save option in Eclipse
     * 
     * @return true
     */
    @Override
    public boolean isSaveAsAllowed() {
        return true;
    }

    /**
     * Close grapheditor
     * 
     * @return succesfullClose
     */
    public boolean close() {
        final GraphEditor editor = this;
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                getSite().getPage().closeEditor(editor, true);
            }
        });
        return true;
    }

    /**
     * Parse editor input
     * 
     * @param input
     * @throws CoreException
     */
    private EditorController createController(IEditorInput input)
            throws CoreException {
        EditorController controller = null;

        if (input instanceof FileEditorInput) {
            IFile file = ((FileEditorInput) input).getFile();
            InputStreamReader reader = new InputStreamReader(file.getContents());
            this.fileName = ((FileEditorInput) input).getPath().toString();
            try {
                controller = new EditorController(this.fileName, reader, this);
            } catch (Exception e) {
                ErrorHandler.showErrorMessageDialog(e, getViewer());
                close();
                return null;

            }
        } else if (input instanceof FileStoreEditorInput) {
            String path = ((FileStoreEditorInput) input).getURI().getPath();
            this.fileName = path;
            try {
                controller = new EditorController(path, this);
            } catch (Exception e) {
                ErrorHandler.showErrorMessageDialog(e, getViewer());
                close();
                return null;
            }
        } else if (input instanceof GraphEditorInput) {
            IModelGraph parsedGraph = (IModelGraph) input
                    .getAdapter(IModelGraph.class);
            controller = new EditorController(parsedGraph, this);
        }

        return controller;
    }

    /**
     * Get PaletteRoot
     * 
     * @return PaletteRoot
     */
    @Override
    protected PaletteRoot getPaletteRoot() {
        return PaletteFactory.createPaletteRoot();
    }

    /**
     * This method retrieves the dirty status of the editor
     * 
     * @return isDirty
     */
    @Override
    public boolean isDirty() {
        if ("".equals(this.fileName)) {
            return true;
        }

        return getCommandStack().isDirty();
    }

    /**
     * Initializes the graphical viewer
     */
    @Override
    protected void initializeGraphicalViewer() {
        Job job = new WorkspaceJob("Initializing graph editor.") {

            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor)
                    throws CoreException {
                try {
                    monitor.beginTask("Initializing graph", 3);

                    monitor.subTask("Parsing file");
                    controller = createController(getEditorInput());
                    if (controller == null) {
                        return Status.CANCEL_STATUS;
                    }
                    monitor.worked(1);

                    monitor.subTask("Applying layout");
                    controller.applyLayout();
                    monitor.worked(1);

                    monitor.subTask("Drawing graph");
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            getGraphicalViewer().setContents(getGraph());
                            setPartName(getGraph().getProperty("name"));
                        }
                    });
                    monitor.worked(1);
                } catch (Exception e) {
                    e.printStackTrace();
                    monitor.setCanceled(true);
                } finally {
                    monitor.done();
                }

                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }

                return Status.OK_STATUS;
            }

        };

        job.setPriority(Job.SHORT);
        job.setUser(true);
        job.schedule();
    }

    /**
     * Adapter to get ScalableRootEditPart
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class type) {
        if (type == ZoomManager.class) {
            return ((ScalableRootEditPart) getGraphicalViewer()
                    .getRootEditPart()).getZoomManager();
        }

        return super.getAdapter(type);
    }

    /**
     * Retrieve graphical viewer
     * 
     * @return viewer
     */
    public GraphicalViewer getViewer() {
        return super.getGraphicalViewer();
    }

    /**
     * Creates the GraphicalViewer on the specified <code>Composite</code>. This
     * method makes a HighLightScrollingGraphicalViewer() as viewer.
     * 
     * @param parent the parent composite
     */
    @Override
    protected void createGraphicalViewer(Composite parent) {
        GraphicalViewer viewer = new HighLightScrollingGraphicalViewer();
        viewer.createControl(parent);
        setGraphicalViewer(viewer);
        configureGraphicalViewer();
        hookGraphicalViewer();
        initializeGraphicalViewer();
    }

    /**
     * Set graph of this editor
     * 
     * @param graph Graph for this editor
     */
    public void setGraph(IModelGraph graph) {
        this.graph = graph;
    }

    /**
     * Get graph from this editor
     * 
     * @return graph
     */
    public IModelGraph getGraph() {
        return graph;
    }

    /**
     * Add an action to the context menu (for all types of selection)
     * 
     * @param action Action to add
     */
    public boolean addContextMenuActionItem(IAction action) {
        GraphContextMenuContributor contextMenuContributer = (GraphContextMenuContributor) (ContextMenuProvider) this
                .getGraphicalViewer().getContextMenu();
        contextMenuContributer.addAction(action);
        return contextMenuContributer.getActionList().contains(action);
    }

    /**
     * Remove an action from the context menu
     * 
     * @param action Action to remove
     */
    public void removeContextMenuAction(IAction action) {
        GraphContextMenuContributor contextMenuContributer = (GraphContextMenuContributor) (ContextMenuProvider) this
                .getGraphicalViewer().getContextMenu();
        contextMenuContributer.removeAction(action);
    }

    /**
     * Retrieve instance of controller which controls this editor
     * 
     * @return controller
     */
    public EditorController getController() {
        return this.controller;
    }

    /**
     * registers a new eventhandler
     * 
     * @param handler the eventhandler to register.
     * @return true
     */
    public boolean addEditorEventHandler(IEditorEventHandler handler) {
        return eventHandlers.add(handler);
    }

    /**
     * Fire an event based on the @param eventName
     * 
     * @param eventName the name of the event
     * @param values the newly updated value
     */
    private void fireEvent(EventType type, List<Object> values) {
        for (IEditorEventHandler eventHandler : eventHandlers) {
            if (eventHandler.getEventType() == type) {
                eventHandler.fireEvent(values);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dancingbear.graphbrowser.controller.IGraphEditor#Relayout()
     */
    public boolean relayout() {
        RelayoutAction relayoutAction = new RelayoutAction(this.getSite()
                .getPage());
        relayoutAction.run();

        return true;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dancingbear.graphbrowser.controller.IGraphEditor#Filter()
     */
    public boolean filter() {
        FilterAction filterAction = new FilterAction(this.getSite().getPage());
        filterAction.run();

        return true;

    }

}