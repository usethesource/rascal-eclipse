/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.importer;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;
import java.util.Map;

import org.dancingbear.graphbrowser.importer.dotparser.AttrList;
import org.dancingbear.graphbrowser.importer.dotparser.AttrStmt;
import org.dancingbear.graphbrowser.importer.dotparser.DOTParser;
import org.dancingbear.graphbrowser.importer.dotparser.EdgeRHS;
import org.dancingbear.graphbrowser.importer.dotparser.EdgeStmt;
import org.dancingbear.graphbrowser.importer.dotparser.Graph;
import org.dancingbear.graphbrowser.importer.dotparser.IdeqStmt;
import org.dancingbear.graphbrowser.importer.dotparser.NodeId;
import org.dancingbear.graphbrowser.importer.dotparser.NodeStmt;
import org.dancingbear.graphbrowser.importer.dotparser.ParseException;
import org.dancingbear.graphbrowser.importer.dotparser.Port;
import org.dancingbear.graphbrowser.importer.dotparser.SimpleNode;
import org.dancingbear.graphbrowser.importer.dotparser.Subgraph;
import org.dancingbear.graphbrowser.model.IModelGraph;
import org.dancingbear.graphbrowser.model.IModelNode;
import org.dancingbear.graphbrowser.model.IModelSubgraph;

/**
 * Imports a DOT file and builds a data model based on the {@link IModelGraph} *
 * 
 * This class makes use of JavaCC and JJTree to compile the DOT file to JAVA.
 * Afterwards, the output of JavaCC is converted to an @link {@link IModelGraph}
 * 
 * JavaCC is a LL top-down parser. Therefore there are limitations for the
 * dotfile. Test have shown that no more than 3700 nodes are allowed in the DOT
 * file. Exceeding this limit will cause the JVM to throw a Stackoverflow
 * Exception in the parser. Using another Parser such as JavaCUP would extends
 * the limits of the importer. If only edges are added to the DOT file, the
 * limit is around 70.000 edges.
 * 
 * @author Nickolas Heirbaut
 * @author Nico Schoenmaker
 * @date 04-03-2009
 */
@SuppressWarnings("unchecked")
class DotImport implements IGraphImport {

    private static final int POSITION_EDGERHS_IN_EDGERHS = 2;
    private static final int POSITION_NODEID_IN_EDGERHS = 1;
    private static final int POSITION_NODEID_IN_NODESTMT = 0;
    private static final int POSITION_PORT_IN_NODEID = 0;
    private static final int POSITION_ATTRLIST_IN_ATTRSTMT = 0;
    private static final int POSITION_ATTRLIST_IN_NODESTMT = 1;
    private static final int POSITION_ATTRLIST_IN_EDGESTMT = 2;

    private static final String KEY_PORT_ID = "port_id";
    private static final String KEY_PORT_COMPASS = "port_compass_point";

    private Hashtable<String, String> globalNodeProperties = new Hashtable<String, String>();
    private Hashtable<String, String> globalEdgeProperties = new Hashtable<String, String>();

    private IModelGraph modelGraph = null;
    private IModelGraph lastGraph;
    private NodeId lastParsedNode;

    /**
     * Converts a dot file to a IModelGraph
     * 
     * @param path Full path of the DOT file
     * @param modelGraph The datamodel {@link IModelGraph} where all nodes,
     * edges, subgraphs and corresponding properties from the DOT file are added
     * to.
     */
    public void importFromPath(String path, IModelGraph modelGraph)
            throws ImportException, IOException {
        FileReader reader = new FileReader(path);

        try {
            importFromReader(reader, modelGraph);
        } catch (ImportException exception) {
            throw new ImportException(exception);
        } finally {
            reader.close();
        }
    }

    /**
     * Converts a dot file to a IModelGraph
     * 
     * @param reader The reader used to read the DOT file
     * @param modelGraph The datamodel {@link IModelGraph} where all nodes,
     * edges, subgraphs and corresponding properties from the DOT file are added
     * to.
     * @throws ImportException The exception thrown if the import fails
     * @throws IOException
     * @throws IOException The exception thrown when the reader in unable to
     * read in the file
     */
    public void importFromReader(Reader reader, IModelGraph modelGraph)
            throws ImportException, IOException {

        SimpleNode graph = null;
        this.modelGraph = modelGraph;
        lastGraph = modelGraph;

        try {
            graph = DOTParser.parseDot(reader);
        } catch (ParseException parseException) {
            throw new ImportException(parseException);
        } finally {
            reader.close();
        }

        parseSimpleNode(graph);
    }

    /**
     * Parses all elements ({@link SimpleNode}) of the parsed DOT file to the
     * datamodel {@link IModelGraph}
     * 
     * @param node
     * @throws ImportException
     */
    private void parseSimpleNode(SimpleNode node) throws ImportException {
        // Parse graph
        if (node instanceof Graph) {
            parseGraph((Graph) node);
        }
        // Parse subgraphs
        else if (node instanceof Subgraph) {
            parseSubGraph((Subgraph) node);
        }
        // Parse nodes [NodeStmt]
        else if (node instanceof NodeStmt) {
            parseNodeStmt((NodeStmt) node);
        }
        // Parse edges [EdgeStmt]
        else if (node instanceof EdgeStmt) {
            parseEdgeStmt((EdgeStmt) node);
        }
        // Parse global properties for an edge, node or graph
        else if (node instanceof AttrStmt) {
            parseAttrStmt((AttrStmt) node);
        }
        // Parse global properties for (sub)graph
        else if (node instanceof IdeqStmt) {
            parseIdeqStmt((IdeqStmt) node);
        }

        // Iterate over all other nodes
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);
            parseSimpleNode(child);
        }
    }

    /**
     * Parses a graph
     * 
     * @param graph
     */
    private void parseGraph(Graph graph) {

        // Get the properties of the graph
        Hashtable<String, String> properties = (Hashtable<String, String>) graph
                .jjtGetValue();

        // Add the graph with properties to the model
        modelGraph.addProperties(properties);
    }

    /**
     * Parses a subgraph
     * 
     * @param subgraph
     */
    private void parseSubGraph(Subgraph subgraph) {
        // Get the properties of the parent for inheritance
        String subgraphName = (String) subgraph.jjtGetValue();
        // Get parent of the subgraph
        SimpleNode parent = getParent(subgraph);

        if (parent instanceof Subgraph) {
            // If the parent is a subgraph, search the parent subgraph in the
            // modelGraph and add the new subgraph to the parent subgraph.
            String parentName = (String) parent.jjtGetValue();
            IModelSubgraph modelParent = modelGraph.getSubgraph(parentName);

            // Get the properties for the subgraph
            Map<String, String> subgraphProperties = new Hashtable<String, String>();
            subgraphProperties.putAll(modelParent.getNonDefaultProperties());

            lastGraph = modelParent.addSubgraph(subgraphName,
                    subgraphProperties);
        } else {
            // Get the properties for the subgraph of the modelgraph
            Map<String, String> graphProperties = new Hashtable<String, String>();
            graphProperties.putAll(modelGraph.getNonDefaultProperties());
            graphProperties.remove("isStrict"); // Property not part of subgraph
            graphProperties.remove("name"); // Property not part of subgraph
            graphProperties.remove("type"); // Property not part of subgraph

            // Add to the model
            IModelSubgraph modelSubGraph = modelGraph.addSubgraph(subgraphName,
                    graphProperties);
            lastGraph = modelSubGraph;
        }
    }

    /**
     * Parses a Node Statement
     * 
     * Description of a single node, this includes the NodeId and an optional
     * AttrList. The NodeId contains the unique ID of the node and optional port
     * information. However, in the case of a NodeStmt, port information is not
     * usefull since this information is only used by edges.
     * "startNode:f1 [color=red];" is theoreticly correct but not usefull. Port
     * information is usefull for edges like "startNode:f1 -> endNode:f0;"
     * 
     * Since the DOT grammar does not deal with this problem, the DotParser used
     * in the package {@link DOTParser} does not throw an exception when port
     * information is specified in a NodeStmt. However, the user should be aware
     * that this information is never used.
     * 
     * @param node
     */
    private void parseNodeStmt(NodeStmt node) {
        addNode(node);
    }

    /**
     * Parses an Edge Statement
     * 
     * List of edges, these includes the NodeId (or a Subgraph), an EdgeRHS and
     * an optional AttrList. An edge statement can be a sequence of several
     * edges (node1->node2->node3->node4) or a single edge (startNode->endNode).
     * 
     * @param edgeStmt
     */
    private void parseEdgeStmt(EdgeStmt edgeStmt) {
        // Get the properties of the subgraph
        Hashtable<String, String> properties = getAttrList(edgeStmt);

        // Remember last parsed node
        NodeId sourceNode = (NodeId) edgeStmt.jjtGetChild(0);
        setNextSourceNode(sourceNode);

        // Parse the right hand side of the Edge (can contain multiple edges: a
        // -> b -> c -> d)
        parseEdgeRHS(
                (EdgeRHS) edgeStmt.jjtGetChild(POSITION_NODEID_IN_EDGERHS),
                properties);
    }

    /**
     * Parses an Edge Right Hand Side.
     * 
     * Since EdgeRHS can contain more than one edge, this method is used
     * recursivly until all edges are processed.
     * 
     * @param edge
     * @param properties
     */
    private void parseEdgeRHS(EdgeRHS edge, Hashtable<String, String> properties) {
        addEdge(edge, properties);

        // Iterate over all remaining edges (if any exists)
        if (edge.jjtGetNumChildren() > POSITION_EDGERHS_IN_EDGERHS) {
            parseEdgeRHS((EdgeRHS) edge
                    .jjtGetChild(POSITION_EDGERHS_IN_EDGERHS), properties);
        }
    }

    /**
     * Parses an Attribute statement for node, edge or (sub)graph
     * 
     * An attribute statement declares the global properties for a node, edge or
     * (sub)graph. This method saves these properties to be used later when a
     * node, edge or (sub)graph is parsed.
     * 
     * @param attrStmt
     * @throws ImportException
     */
    private void parseAttrStmt(AttrStmt attrStmt) {
        // Get the prefix (node, edge or graph)
        String prefix = (String) attrStmt.jjtGetValue();
        Hashtable<String, String> properties = getAttrList(attrStmt);

        // Global node properties
        if ("NODE".equalsIgnoreCase(prefix)) {
            globalNodeProperties = properties;
        }

        // Global edge properties
        else if ("EDGE".equalsIgnoreCase(prefix)) {
            globalEdgeProperties = properties;
        }

        // Global graph properties
        else if ("GRAPH".equalsIgnoreCase(prefix)) {
            lastGraph.addProperties(properties);
        }
    }

    /**
     * Parses an ID Equal statement
     * 
     * An IdeqStmt is a property of a (sub)graph
     * 
     * @param stmt
     */
    private void parseIdeqStmt(IdeqStmt stmt) {
        // Get the properties of the (sub)graph
        Hashtable<String, String> properties = (Hashtable<String, String>) stmt
                .jjtGetValue();

        // Get the (sub)graph for which the properties are specified
        SimpleNode node = getParent(stmt);

        // Add properties to the (sub)graph
        if (node instanceof Subgraph) {
            IModelSubgraph subgraph = modelGraph.getSubgraph((String) node
                    .jjtGetValue());
            subgraph.addProperties(properties);
        } else {
            modelGraph.addProperties(properties);
        }

    }

    /**
     * Adds the node to the model
     * 
     * Since a NodeId does not have local properties, the global properties are
     * applied to the node.
     * 
     * @param node
     */
    private IModelNode addNode(NodeId node) {
        Hashtable<String, String> properties = (Hashtable<String, String>) globalNodeProperties
                .clone();

        // Prevent from overriding local properties
        String nodeName = node.jjtGetValue().toString();
        IModelNode existingNode = modelGraph.getNode(nodeName);
        if (existingNode == null) {
            return addNode(node, properties);
        }
        return existingNode;
    }

    /**
     * Adds the node to the model
     * 
     * The local properties of the NodeStmt are combined with the global
     * properties. If duplicates exists, local properties override the global
     * properties.
     * 
     * @param nodeStmt
     */
    private IModelNode addNode(NodeStmt nodeStmt) {
        // Get the nodeId of this nodestmt
        NodeId node = (NodeId) nodeStmt
                .jjtGetChild(POSITION_NODEID_IN_NODESTMT);

        // Get atribute list of this nodestmt
        Hashtable<String, String> localProperties = getAttrList(nodeStmt);

        // Complement the attribute list with the global node properties
        Hashtable<String, String> properties = (Hashtable<String, String>) globalNodeProperties
                .clone();
        properties.putAll(localProperties);

        return addNode(node, properties);
    }

    /**
     * Adds the node to the model
     * 
     * The local properties are combined with the global properties. If
     * duplicates exists, local properties override the global properties.
     * 
     * @param name
     * @param defaultProperties
     */
    private IModelNode addNode(NodeId node, Hashtable<String, String> properties) {
        String name = node.jjtGetValue().toString();

        // Get the parent graph
        SimpleNode graph = getParent(node);

        // Save the node to the parent graph (graph or subgraph) in the model
        if (graph instanceof Subgraph) {
            IModelGraph subgraph = modelGraph.getSubgraph((String) graph
                    .jjtGetValue());
            return subgraph.addNode(name, properties);
        }
        return modelGraph.addNode(name, properties);
    }

    /**
     * Adds an edge to the model
     * 
     * The local properties are combined with the global properties. If
     * duplicates exists, local properties override the global properties.
     * 
     * @param node
     * @param defaultProperties
     */
    private void addEdge(EdgeRHS node, Hashtable<String, String> localProperties) {
        // Get the source and target node
        NodeId sourceNode = lastParsedNode;
        NodeId targetNode = (NodeId) node
                .jjtGetChild(POSITION_NODEID_IN_EDGERHS);

        // Complement the attribute list with the global node properties
        Hashtable<String, String> properties = (Hashtable<String, String>) globalEdgeProperties
                .clone();
        Hashtable<String, String> portProperties = getPortProperties(
                sourceNode, targetNode);
        properties.putAll(localProperties);
        properties.putAll(portProperties);

        // Set the next targetnode
        setNextSourceNode(targetNode);

        // Add nodes to the model
        IModelNode sourceModelNode = addNode(sourceNode);
        IModelNode targetModelNode = addNode(targetNode);

        modelGraph.addEdge(sourceModelNode, targetModelNode, properties);
    }

    /**
     * Extracts the attribute list of a node statement
     * 
     * @param nodeStmt
     * @return
     */
    private Hashtable<String, String> getAttrList(NodeStmt nodeStmt) {
        if (nodeStmt.jjtGetNumChildren() > POSITION_ATTRLIST_IN_NODESTMT) {
            AttrList attrList = (AttrList) nodeStmt
                    .jjtGetChild(POSITION_ATTRLIST_IN_NODESTMT);
            return (Hashtable<String, String>) attrList.jjtGetValue();
        }
        return new Hashtable<String, String>();
    }

    /**
     * Extracts the attribute list of an edge statement
     * 
     * @param nodeStmt
     * @return
     */
    private Hashtable<String, String> getAttrList(EdgeStmt edgeStmt) {
        if (edgeStmt.jjtGetNumChildren() > POSITION_ATTRLIST_IN_EDGESTMT) {
            AttrList attrList = (AttrList) edgeStmt
                    .jjtGetChild(POSITION_ATTRLIST_IN_EDGESTMT);
            return (Hashtable<String, String>) attrList.jjtGetValue();
        }
        return new Hashtable<String, String>();
    }

    /**
     * Extracts the attribute list of a attribute statement
     * 
     * @param attrStmt
     * @return
     */
    private Hashtable<String, String> getAttrList(AttrStmt attrStmt) {
        AttrList attrList = (AttrList) attrStmt
                .jjtGetChild(POSITION_ATTRLIST_IN_ATTRSTMT);
        return (Hashtable<String, String>) attrList.jjtGetValue();
    }

    /**
     * Returns the parent of a property (subgraph or graph)
     * 
     * @param stmt
     * @return
     */
    private SimpleNode getParent(SimpleNode node) {
        SimpleNode parent = node;
        do {
            parent = (SimpleNode) parent.jjtGetParent();
        } while (!(parent instanceof Subgraph) && !(parent instanceof Graph));

        return parent;
    }

    /**
     * Extracts the port information for an edge
     * 
     * Contains the information used by an edge to define what the anchor points
     * are for the edge at source and target node.
     * 
     * Caution: the position of the anchor points itselves are encapsulated in
     * the label property of a NodeStmt.
     * 
     * @param sourceNode
     * @param targetNode
     * @return
     */
    private Hashtable<String, String> getPortProperties(NodeId sourceNode,
            NodeId targetNode) {
        Hashtable<String, String> properties = new Hashtable<String, String>();

        properties.putAll(getPortProperties(sourceNode, "source_"));
        properties.putAll(getPortProperties(targetNode, "target_"));

        return properties;
    }

    //
    private Hashtable<String, String> getPortProperties(NodeId node,
            String portPrefix) {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        if (node.jjtGetNumChildren() > POSITION_PORT_IN_NODEID) {
            Port port = (Port) node.jjtGetChild(POSITION_PORT_IN_NODEID);
            Hashtable<String, String> portProperties = (Hashtable<String, String>) port
                    .jjtGetValue();

            if (portProperties.containsKey(KEY_PORT_COMPASS)) {
                properties.put(portPrefix + KEY_PORT_COMPASS, portProperties
                        .get(KEY_PORT_COMPASS));
            }

            if (portProperties.containsKey(KEY_PORT_ID)) {
                properties.put(portPrefix + KEY_PORT_ID, portProperties
                        .get(KEY_PORT_ID));
            }
        }
        return properties;
    }

    /**
     * Saves the last used node to be used for further calculations
     * 
     * @param node
     */
    private void setNextSourceNode(NodeId node) {
        lastParsedNode = node;
    }
}
