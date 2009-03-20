/*******************************************************************************
 * Copyright (c) 2000, 2007, 2009 IBM Corporation, University of Amsterdam and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation (copied from ScrollingGraphicalViewer)
 *     Dancing Bears: Taco Witte and Jeroen Bach - enhanced navigation for the graphbrowser project
 *******************************************************************************/
package org.dancingbear.graphbrowser.editor.gef;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.dancingbear.graphbrowser.controller.IGraphEditor;
import org.dancingbear.graphbrowser.editor.gef.editparts.EdgeEditPart;
import org.dancingbear.graphbrowser.editor.gef.editparts.NodeEditPart;
import org.dancingbear.graphbrowser.editor.gef.ui.parts.HighLightScrollingGraphicalViewer;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

/**
 * An extended KeyHandler which processes default keystrokes for common
 * navigation in a GraphicalViewer. This class can be used as a KeyHandler too;
 * Unrecognized keystrokes are sent to the super's implementation. This class
 * will process key events containing the following:
 * <UL>
 * <LI>Arrow Keys (UP, DOWN, LEFT, RIGHT) with optional SHIFT and CONTROL
 * modifiers
 * <LI>Arrow Keys (UP, DOWN) same as above, but with ALT modifier.
 * </UL>
 * <P>
 * All processed key events will do nothing other than change the selection
 * and/or focus editpart for the viewer.
 * 
 * @author hudsonr
 * @author jeroenbach
 * @author Jeroen van Lieshout
 * @author Taco Witte
 */
public class TopologicalKeyHandler extends KeyHandler {
    // TODO: check the documentation if it is complete

    /**
     * When selecting different edges we use this counter to keep track of the
     * selected edge
     */
    private int connectionCounter;

    /**
     * When navigating through connections, a "Node" EditPart is used as a
     * reference.
     */
    private WeakReference<GraphicalEditPart> cachedNode;

    /**
     * When navigating through the graph, the previous direction is rememberd.
     */
    private int previousVerticalDirection;

    private GraphicalViewer viewer;
    private IGraphEditor graphEditor;

    /**
     * Constructs a key handler for the given viewer.
     * 
     * @param viewer the viewer
     */
    public TopologicalKeyHandler(GraphicalViewer viewer) {
        this.viewer = viewer;
    }

    /**
     * Constructs a key handler for the given viewer.
     * 
     * @param viewer the viewer
     */
    public TopologicalKeyHandler(GraphicalViewer viewer, IGraphEditor page) {
        this.viewer = viewer;
        this.graphEditor = page;
    }

    /**
     * @return <code>true</code> if the keys pressed indicate to traverse inside
     * a container
     */
    boolean acceptIntoContainer(KeyEvent event) {
        return ((event.stateMask & SWT.ALT) != 0)
                && (event.keyCode == SWT.ARROW_DOWN);
    }

    /**
     * If the keys pressed that indicate a change of direction in
     * traversing/selecting the connections
     * 
     * @param event
     * @return true if the previous direction is not the same as the current
     * selected direction
     */
    boolean acceptChangeDirection(KeyEvent event) {

        if (getFocusEditPart() instanceof ConnectionEditPart) {
            int key = event.keyCode;
            int direction;

            if (key == SWT.ARROW_UP) {
                direction = PositionConstants.TOP;
            } else if (key == SWT.ARROW_DOWN) {
                direction = PositionConstants.BOTTOM;
            } else {
                return false;
            }

            if (direction != getPreviousVerticalDirection()) {
                return true;
            }
        }

        return false;

    }

    /**
     * @return <code>true</code> if the viewer's contents has focus and one of
     * the arrow keys is pressed
     */
    boolean acceptLeaveContents(KeyEvent event) {
        int key = event.keyCode;
        return getFocusEditPart() == getViewer().getContents()
                && pressedArrowKey(key);
    }

    /**
     * @return <code>true</code> if the filter hotkey is pressed
     */
    boolean acceptFilterToNewTab(KeyEvent event) {
        return ((event.stateMask & SWT.CTRL) != 0) && (event.keyCode == 't');

    }

    /**
     * @return <code>true</code> if the keys pressed indicate to traverse to the
     * parent of the currently focused EditPart
     */
    boolean acceptOutOf(KeyEvent event) {
        return ((event.stateMask & SWT.ALT) != 0)
                && (pressedArrowUp(event.keyCode));
    }

    boolean acceptScroll(KeyEvent event) {
        return ((event.stateMask & SWT.CTRL) != 0
                && (event.stateMask & SWT.SHIFT) != 0 && (pressedArrowKey(event.keyCode)));
    }

    /**
     * @return <code>true</code> if the keys pressed indicate a relayout
     */
    boolean acceptRelayout(KeyEvent event) {
        return ((event.stateMask & SWT.CTRL) != 0) && (event.keyCode == 'r');
    }

    /**
     * @return <code>true</code> if the keys pressed indicate a deselect
     */
    boolean acceptDeselect(KeyEvent event) {
        return ((event.stateMask & SWT.CTRL) != 0) && (event.keyCode == 'd');
    }

    /**
     * @return <code>true</code> if the keys pressed indicate a reset of the
     * zoom.
     */
    boolean acceptResetZoom(KeyEvent event) {
        return ((event.stateMask & SWT.CTRL) != 0) && (event.keyCode == '0');
    }

    /**
     * @return <code>true</code> if the keys pressed indicate that the view
     * should be centralized.
     */
    boolean acceptCenterView(KeyEvent event) {
        return event.keyCode == SWT.HOME;
    }

    /**
     * Figures' navigation points are used to determine their direction compared
     * to one another, and the distance between them.
     * 
     * @return the center of the given figure
     */
    Point getNavigationPoint(IFigure figure) {
        return figure.getBounds().getCenter();
    }

    /**
     * Calculates the Absolute NavigationPoint of a GraphicalEditPart
     * 
     * @param epStart
     * @return
     */
    Point getAbsoluteNavigationPoint(GraphicalEditPart epStart) {
        IFigure figure = epStart.getFigure();
        Point pStart = getNavigationPoint(figure);
        figure.translateToAbsolute(pStart);

        return pStart;
    }

    /**
     * Returns the cached node. It is possible that the node is not longer in
     * the viewer but has not been garbage collected yet.
     */
    protected GraphicalEditPart getCachedNode() {
        if (cachedNode == null)
            return null;
        if (cachedNode.isEnqueued())
            return null;
        return cachedNode.get();
    }

    protected void setCachedNode(GraphicalEditPart node) {
        if (node == null)
            cachedNode = null;
        else
            cachedNode = new WeakReference<GraphicalEditPart>(node);
    }

    /**
     * Returns the previous vertical direction.
     */
    protected int getPreviousVerticalDirection() {
        return previousVerticalDirection;
    }

    /**
     * Sets the previous Direction.
     * 
     * @param direction The direction can only be PositionConstants.TOP or
     * PositionConstants.BOTTOM
     */
    protected void setPreviousVerticalDirection(int direction) {
        boolean isValidValue = false;

        if (PositionConstants.TOP == direction
                || PositionConstants.BOTTOM == direction) {
            isValidValue = true;
        }
        assert isValidValue : "Direction can only be TOP or BOTTOM";

        previousVerticalDirection = direction;
    }

    /**
     * Get editpart that has focus
     * 
     * @return the EditPart that has focus
     */
    protected GraphicalEditPart getFocusEditPart() {
        return (GraphicalEditPart) getViewer().getFocusEditPart();
    }

    /**
     * Returns the list of editparts which are conceptually at the same level of
     * navigation as the currently focused editpart. By default, these are the
     * siblings of the focused part.
     * <p>
     * This implementation returns a list that contains the EditPart that has
     * focus.
     * </p>
     * 
     * @return a list of navigation editparts
     * @since 3.4
     */
    @SuppressWarnings("unchecked")
    protected List<EditPart> getNavigationSiblings() {
        EditPart focusPart = getFocusEditPart();
        if (focusPart.getParent() != null) {
            List<EditPart> children = focusPart.getParent().getChildren();
            return children;
        }
        List<EditPart> list = new ArrayList<EditPart>();
        list.add(focusPart);
        return list;
    }

    /**
     * Returns the viewer on which this key handler was created.
     * 
     * @return the viewer
     */
    protected GraphicalViewer getViewer() {
        return viewer;
    }

    /**
     * Returns the IGraphEditor, if this is not set in the constructor this will
     * be null.
     * 
     * @return the viewer
     */
    protected IGraphEditor getGraphEditor() {
        return graphEditor;

    }

    /**
     * Get status of mirror of viewer
     * 
     * @return <code>true</code> if the viewer is mirrored
     * @since 3.4
     */
    protected boolean isViewerMirrored() {
        return (viewer.getControl().getStyle() & SWT.MIRRORED) != 0;
    }

    /**
     * Extended to process key events described above.
     * 
     * @see org.eclipse.gef.KeyHandler#keyPressed(org.eclipse.swt.events.KeyEvent)
     */
    public boolean keyPressed(KeyEvent event) {

        if (acceptCenterView(event)) {
            processCenterView(event);
            return true;
        } else if (acceptResetZoom(event)) {
            processResetZoom(event);
            return true;
        } else if (acceptIntoContainer(event)) {
            navigateIntoContainer(event);
            return true;
        } else if (acceptOutOf(event)) {
            navigateOut(event);
            return true;
        } else if (acceptScroll(event)) {
            scrollViewer(event);
            return true;
        } else if (acceptChangeDirection(event)) {
            navigateChangeDirectionSelectedPath(event);
            return true;
        } else if (acceptLeaveContents(event)) {
            navigateIntoContainer(event);
            return true;
        } else if (acceptRelayout(event)) {
            processRelayout(event);
            return true;
        } else if (acceptFilterToNewTab(event)) {
            processFilterToNewTab(event);
            return true;
        } else if (acceptDeselect(event)) {
            processDeselect(event);
            return true;
        }

        switch (event.keyCode) {
        case SWT.ARROW_LEFT:
            if (navigateConnections(event, PositionConstants.LEFT))
                return true;
            break;
        case SWT.ARROW_RIGHT:
            if (navigateConnections(event, PositionConstants.RIGHT))
                return true;
            break;
        case SWT.ARROW_UP:

            if (selectPath(event, PositionConstants.TOP))
                return true;
            break;
        case SWT.ARROW_DOWN:
            if (selectPath(event, PositionConstants.BOTTOM))
                return true;
            break;

        case SWT.Selection: // Selection is ENTER/RETURN
            if (navigateSelectedPath(event))
                return true;
            break;

        // default case intentionally omitted
        }
        return super.keyPressed(event);
    }

    private boolean processDeselect(KeyEvent event) {
        if (getGraphEditor() == null) {
            return false;
        }

        // Get editor and tell viewer of editor that we want all items
        // deselected
        IGraphEditor editor = getGraphEditor();
        editor.getViewer().deselectAll();
        return true;

    }

    private boolean pressedArrowKey(int keyCode) {
        return pressedArrowUp(keyCode) || pressedArrowRight(keyCode)
                || pressedArrowDown(keyCode) || pressedArrowLeft(keyCode);
    }

    private boolean pressedArrowUp(int keyCode) {
        return keyCode == SWT.ARROW_UP;
    }

    private boolean pressedArrowRight(int keyCode) {
        return keyCode == SWT.ARROW_RIGHT;
    }

    private boolean pressedArrowDown(int keyCode) {
        return keyCode == SWT.ARROW_DOWN;
    }

    private boolean pressedArrowLeft(int keyCode) {
        return keyCode == SWT.ARROW_LEFT;
    }

    /**
     * This method traverses to the closest child of the currently focused
     * EditPart, if it has one.
     */
    @SuppressWarnings("unchecked")
    void navigateIntoContainer(KeyEvent event) {
        GraphicalEditPart focus = getFocusEditPart();
        List<EditPart> childList = focus.getChildren();
        Point tl = focus.getContentPane().getBounds().getTopLeft();

        int minimum = Integer.MAX_VALUE;
        int current;
        GraphicalEditPart closestPart = null;

        for (int i = 0; i < childList.size(); i++) {
            GraphicalEditPart ged = (GraphicalEditPart) childList.get(i);
            if (!ged.isSelectable())
                continue;
            Rectangle childBounds = ged.getFigure().getBounds();

            current = (childBounds.x - tl.x) + (childBounds.y - tl.y);
            if (current < minimum) {
                minimum = current;
                closestPart = ged;
            }
        }
        if (closestPart != null)
            navigateTo(event, closestPart);
    }

    /**
     * Navigates to the parent of the currently focused EditPart.
     */
    void navigateOut(KeyEvent event) {
        if (getFocusEditPart() == null
                || getFocusEditPart() == getViewer().getContents()
                || getFocusEditPart().getParent() == getViewer().getContents())
            return;
        navigateTo(event, getFocusEditPart().getParent());
    }

    /**
     * Navigates to the source or target of the currently focused
     * ConnectionEditPart.
     */
    void navigateOutOfConnection(KeyEvent event) {
        GraphicalEditPart cached = getCachedNode();
        ConnectionEditPart conn = (ConnectionEditPart) getFocusEditPart();
        if (cached != null
                && (cached == conn.getSource() || cached == conn.getTarget()))
            navigateTo(event, cached);
        else
            navigateTo(event, conn.getSource());
    }

    /**
     * Navigate the currently selected path. This changes the focused node to
     * the node reached by the path.
     */
    boolean navigateSelectedPath(KeyEvent event) {

        navigateOutOfConnection(event);
        selectPath(event, getPreviousVerticalDirection());

        return true;
    }

    /**
     * This method navigates through connections based on the keys pressed.
     * 
     * @param event the key event
     * @param horizontalDirection the PositionConstants.LEFT or
     * PositionConstants.RIGHT direction
     * 
     * 
     */
    boolean navigateConnections(KeyEvent event, int horizontalDirection) {
        GraphicalEditPart focus = getFocusEditPart();

        if (focus instanceof ConnectionEditPart) {

            GraphicalEditPart currentNode = findCurrentNodeOfSelection();
            navigateTo(event, currentNode);

            ConnectionEditPart focusConnectionPart = (ConnectionEditPart) focus;
            GraphicalEditPart selectedEdge = findSelectedConnection(
                    currentNode, focusConnectionPart, horizontalDirection,
                    getPreviousVerticalDirection());

            selectEdgeAndHighlightPath(event, getPreviousVerticalDirection(),
                    currentNode, selectedEdge);

            return true;
        }

        return false;
    }

    /**
     * Cancels the currently selected path and selects the opposite. For
     * example: if the incomming edges were selected as path, now the outgoing
     * will be and vica versa.
     * 
     * @author jeroenbach
     */
    boolean navigateChangeDirectionSelectedPath(KeyEvent event) {

        GraphicalEditPart currentNode = findCurrentNodeOfSelection();

        navigateTo(event, currentNode);

        // revert direction
        int direction = PositionConstants.BOTTOM;
        if (PositionConstants.BOTTOM == getPreviousVerticalDirection()) {
            direction = PositionConstants.TOP;
        }

        selectPath(event, direction);

        return true;
    }

    /**
     * This method is invoked when the user presses the space bar. It focuses on
     * the current selected node.
     * 
     * @param event the key event received
     */
    boolean processCenterView(KeyEvent event) {
        if (!(getViewer().getControl() instanceof FigureCanvas))
            return false;

        FigureCanvas figCanvas = (FigureCanvas) getViewer().getControl();
        org.eclipse.swt.graphics.Rectangle clientArea = figCanvas.getBounds();
        GraphicalEditPart currentNode = findCurrentNodeOfSelection();
        Point point = getNavigationPoint(currentNode.getFigure());

        // Find out the current zoom level
        double zoom = 1.0;
        IGraphEditor graphEditor = getGraphEditor();
        if (graphEditor != null) {
            ZoomManager manager = graphEditor.getZoomManager();
            if (manager != null) {
                zoom = manager.getZoom();
            }
        }

        // TODO: find out how to really calculate this
        double displayedWidth = clientArea.width / (Math.pow(zoom, 0.10));
        double displayedHeight = clientArea.height / (Math.pow(zoom, 0.10));

        double topLeftX = Math.max(0, (zoom * point.x - displayedWidth / 2.0));
        double topLeftY = Math.max(0, (zoom * point.y - displayedHeight / 2.0));

        figCanvas.scrollSmoothTo((int) topLeftX, (int) topLeftY);

        return true;
    }

    boolean processResetZoom(KeyEvent event) {
        IGraphEditor graphEditor = getGraphEditor();
        if (graphEditor == null) {
            return false;
        }

        ZoomManager manager = graphEditor.getZoomManager();
        if (manager == null) {
            return false;
        }

        manager.setZoom((1.0));

        return true;
    }

    boolean scrollViewer(KeyEvent event) {
        if (!(getViewer().getControl() instanceof FigureCanvas))
            return false;

        FigureCanvas figCanvas = (FigureCanvas) getViewer().getControl();
        Point loc = figCanvas.getViewport().getViewLocation();
        Rectangle area = figCanvas.getViewport().getClientArea(
                Rectangle.SINGLETON).scale(.1);
        switch (event.keyCode) {
        case SWT.ARROW_DOWN:
            figCanvas.scrollToY(loc.y + area.height);
            break;
        case SWT.ARROW_UP:
            figCanvas.scrollToY(loc.y - area.height);
            break;
        case SWT.ARROW_LEFT:
            if (isViewerMirrored())
                figCanvas.scrollToX(loc.x + area.width);
            else
                figCanvas.scrollToX(loc.x - area.width);
            break;
        case SWT.ARROW_RIGHT:
            if (isViewerMirrored())
                figCanvas.scrollToX(loc.x - area.width);
            else
                figCanvas.scrollToX(loc.x + area.width);
        }

        return true;
    }

    /**
     * Highlights the path of edges and selects the edge closed to a vertical
     * line.
     * 
     * @param direction You can use two directions PositionConstants.TOP and
     * PositionConstants.BOTTOM. TOP for incomming connections and BOTTOM for
     * outgoing connections.
     * 
     * @author jeroenbach
     */
    boolean selectPath(KeyEvent event, int direction) {

        if (getFocusEditPart() instanceof ConnectionEditPart) {
            return false;
        }

        GraphicalEditPart currentNode = findCurrentNodeOfSelection();

        GraphicalEditPart selectedEdge = findInitialSelectionConnection(
                currentNode, direction);

        selectEdgeAndHighlightPath(event, direction, currentNode, selectedEdge);

        return true;

    }

    /**
     * Performs a relayout
     * 
     * @param event
     * 
     * @author jcbach
     */
    boolean processRelayout(KeyEvent event) {

        if (getGraphEditor() == null) {
            return false;
        }

        IGraphEditor editor = getGraphEditor();
        editor.relayout();

        return true;
    }

    /**
     * Filters the nodes to a new tab
     * 
     * @param event
     */
    boolean processFilterToNewTab(KeyEvent event) {

        if (getGraphEditor() == null) {
            return false;
        }

        IGraphEditor editor = getGraphEditor();
        editor.filter();

        return true;
    }

    /**
     * Navigates to the given EditPart
     * 
     * @param part the EditPart to navigate to
     * @param event the KeyEvent that triggered this traversal
     */
    void navigateTo(KeyEvent event, EditPart part) {
        if (part == null)
            return;
        if ((event.stateMask & SWT.SHIFT) != 0) {
            getViewer().appendSelection(part);
            getViewer().setFocus(part);
        } else if ((event.stateMask & SWT.CONTROL) != 0)
            getViewer().setFocus(part);
        else
            getViewer().select(part);
        getViewer().reveal(part);
    }

    /**
     * Finds the current node of the selection. Also if a path is active (and an
     * edge is selected) it gets the node from which we're navigating.
     * 
     * @return the current node
     * 
     * @author jeroenbach
     */
    private GraphicalEditPart findCurrentNodeOfSelection() {

        GraphicalEditPart selectedElement = getFocusEditPart();

        if (selectedElement instanceof NodeEditPart) {
            return selectedElement;
        }

        int direction = PositionConstants.BOTTOM;
        if (PositionConstants.BOTTOM == getPreviousVerticalDirection()) {
            direction = PositionConstants.TOP;
        }

        GraphicalEditPart currentNode = findTargetOrSourceOfEdge(
                selectedElement, direction);

        return currentNode;
    }

    /**
     * Finds a connection to select of the current node.
     * 
     * @param currentNode
     * @param focusConnectionPart
     * @param horizontalDirection
     * @param verticalDirection
     * @return
     * 
     * @author jeroenbach
     */
    private GraphicalEditPart findSelectedConnection(
            GraphicalEditPart currentNode,
            ConnectionEditPart focusConnectionPart, int horizontalDirection,
            int verticalDirection) {

        // TODO: rewrite so that the right and left are dependent on the x and y
        // position and clockwise instead of position in array.

        List<GraphicalEditPart> edges = findEdges(getNavigationSiblings(),
                verticalDirection, currentNode);

        if (edges.isEmpty()) {
            return null;
        }

        if (PositionConstants.RIGHT == horizontalDirection) {
            connectionCounter++;
        } else if (PositionConstants.LEFT == horizontalDirection) {
            connectionCounter--;
        } else {
            return null;
        }

        while (connectionCounter < 0) {
            connectionCounter += edges.size();
        }

        connectionCounter %= edges.size();

        return (ConnectionEditPart) edges.get(connectionCounter % edges.size());

    }

    /**
     * Finds the edge in the list of edges closest to 6 o'clock of the node.
     * That is, the edge that is the most vertical. In a horizontal graph this
     * is the edge that is the closest to 3 o'clock. That is, the edges that are
     * the most horizontal.
     * 
     * @param edges all the edges to search
     * @param start the point of the node is selected and from which the edges
     * should be messured.
     * @return the initial selected connection
     * 
     * @author jeroenbach
     */
    private GraphicalEditPart findInitialSelectionConnection(
            GraphicalEditPart currentNode, int verticalDirection) {

        List<GraphicalEditPart> edges = findEdges(getNavigationSiblings(),
                verticalDirection, currentNode);

        // Point pStart = getAbsoluteNavigationPoint(currentNode);

        if (edges.size() == 0) {
            return null;
        }

        // TODO: calculate the edge closest to 12 or 6
        // Don't forget to keep in mind that the graph can also be horizontal
        // oriented.
        GraphicalEditPart selectedEdge = edges.get(0); // start with the first.

        // EdgeEditPart edgepart = new EdgeEditPart();
        // edgepart.getCastedFigure();
        //
        // for (GraphicalEditPart edge : edges) {
        // // should be of Polyline otherwise forget it
        // if (edge.getFigure() instanceof Polyline) {
        // Polyline polyLine = (Polyline) edge.getFigure();
        //
        // // / either the x coordinates are the closest to the same
        // // / (depending on the graph direction)
        //
        // // voeg toe aan de edgeeditpart! dat hij het percentage gedraait
        // // terugvind
        // // of iets dat ziet hoe verticaal of horizontaal de line staat
        // // om verder te rekenen
        //
        // // / or the y coordinates are the closest to the same (depending
        // // / on the graph direction)
        // }
        // }

        return selectedEdge;// just return the first (for know)
    }

    /**
     * Finds the target or source of a connection.
     * 
     * @param selectedEdge of which to find the source or target
     * @param direction PositionConstants.TOP for Source and
     * PositionConstants.BOTTOM for Target
     * 
     * @return The source or target node of a connection, if none can be found
     * null is returned.
     * 
     * @author jeroenbach
     */
    private GraphicalEditPart findTargetOrSourceOfEdge(
            GraphicalEditPart selectedEdge, int direction) {

        if (selectedEdge instanceof EdgeEditPart) {
            EdgeEditPart edgePart = (EdgeEditPart) selectedEdge;
            EditPart targetOrSourcePart;

            if (direction == PositionConstants.TOP)
                targetOrSourcePart = edgePart.getSource();
            else
                targetOrSourcePart = edgePart.getTarget();

            if (targetOrSourcePart instanceof GraphicalEditPart) {
                return (GraphicalEditPart) targetOrSourcePart;
            }
        }
        return null;
    }

    /**
     * Finds all the edges of a GraphicalEditPart in a particular direction.
     * 
     * @param navigationSiblings all the NavigationSiblings
     * @param direction either the PositionConstants.TOP or
     * PositionConstants.BOTTOM direction (indicating the incomming and outgoing
     * edges)
     * @param epStart
     * @return a list of edges.
     * 
     * @author jeroenbach
     */
    @SuppressWarnings("unchecked")
    private List<GraphicalEditPart> findEdges(List navigationSiblings,
            int direction, GraphicalEditPart epStart) {

        GraphicalEditPart focus = epStart;

        List uncastedConnections;

        if (direction == PositionConstants.TOP)
            uncastedConnections = focus.getTargetConnections();
        else
            uncastedConnections = focus.getSourceConnections();

        List<GraphicalEditPart> castedConnections = new ArrayList<GraphicalEditPart>();

        for (Object ucastedConnection : uncastedConnections) {
            if (ucastedConnection instanceof GraphicalEditPart) {
                castedConnections.add((GraphicalEditPart) ucastedConnection);
            }

        }

        return castedConnections;

    }

    /**
     * Takes care of the selecting and the highlighting of the node and edges.
     * 
     * @param event
     * @param direction
     * @param currentNode
     * @param selectedEdge
     * @return true or false indicating that the method succeeded
     * 
     * @author jeroenbach
     */
    private boolean selectEdgeAndHighlightPath(KeyEvent event, int direction,
            GraphicalEditPart currentNode, GraphicalEditPart selectedEdge) {
        // TODO: a few of this methodes parameters can be removed.

        if (selectedEdge == null)
            return false;
        if (currentNode == null)
            return false;

        // remember the node to which should be navigated, this cached node
        // is used by other methods that handle the next navigation step
        GraphicalEditPart targetOrSource = findTargetOrSourceOfEdge(
                selectedEdge, direction);
        setCachedNode(targetOrSource);
        setPreviousVerticalDirection(direction);

        List<GraphicalEditPart> edges = findEdges(getNavigationSiblings(),
                direction, currentNode);
        highLightEdges(edges);

        // TODO: fix this hack
        event.stateMask = SWT.SHIFT;

        navigateTo(event, selectedEdge);

        return true;
    }

    /**
     * Highlights all the edges in the edges list.
     * 
     * @param edges list of edges that should be highlighted, these edges should
     * implement the HighlightEditPart interface (for know) untill the highlight
     * is included in the main EditPart interface
     * 
     * @author jeroenbach
     */
    private boolean highLightEdges(List<GraphicalEditPart> edges) {
        if (edges == null) {
            return false;
        }

        // because we have no access to the AbstractEditPartViewer we can't
        // implement the "highlight methodes" in that class. That is why we cast
        // the viewer here to the class that does implement the
        // "highlight methodes": HighLightScrollingGraphicalViewer. Eventually
        // these "highlight methodes" should be implemented in
        // AbstractEditPartViewer.

        GraphicalViewer viewer = getViewer();
        if (viewer instanceof HighLightScrollingGraphicalViewer) {
            HighLightScrollingGraphicalViewer highlightableViewer = (HighLightScrollingGraphicalViewer) getViewer();

            for (GraphicalEditPart editPart : edges) {
                highlightableViewer.appendHighlight(editPart);

            }
        }

        return true;

    }

}
