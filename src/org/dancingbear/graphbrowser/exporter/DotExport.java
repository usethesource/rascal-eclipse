/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.exporter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dancingbear.graphbrowser.model.IModelEdge;
import org.dancingbear.graphbrowser.model.IModelGraph;
import org.dancingbear.graphbrowser.model.IModelNode;
import org.dancingbear.graphbrowser.model.IModelSubgraph;

/**
 * Exports the graph model to a DOT formated file
 * 
 * @author Nickolas Heirbaut
 */
@SuppressWarnings("unchecked")
class DotExport implements IGraphExport {

    private static final char BEGIN_GRAPHBODY = '{';
    private static final char END_GRAPHBODY = '}';

    private static final char BEGIN_ATTRLIST = '[';
    private static final char END_ATTRLIST = ']';

    private static final char SEPERATOR_NEW_ATTRIBUTE = ',';
    private static final char SEPERATOR_KEY_VALUE = '=';

    private static final char NEW_LINE = '\n';
    private static final char NEW_TAB = '\t';
    private static final char NEW_SPACE = ' ';
    private static final char DOUBLE_QUOTE = '"';

    private static final String DIRECTED_EDGE = "->";
    private static final String UNDIRECTED_EDGE = "--";

    private static final String KEY_STRICT_PROPERTY = "isStrict";
    private static final String KEY_GRAPHTYPE_PROPERTY = "type";
    private static final String KEY_GRAPHNAME_PROPERTY = "name";

    private static final String DECLARATION_GRAPH = "graph";
    private static final String DECLARATION_SUBGRAPH = "subgraph";
    private static final String DECLARATION_STRICT = "strict";

    private static final String PREFIX_INTERNAL_SUBGRAPHNAME = "dancing_bear_internal_name_";

    private int indention = 1;

    /**
     * Exports the graph model to a DOT formated file using the given path
     * 
     * @throws ExportException Exception is thrown if the export fails
     */
    public void exportFromPath(String path, IModelGraph modelGraph)
            throws IOException, ExportException {
        FileWriter writer = new FileWriter(path);
        try {
            exportFromWriter(writer, modelGraph);
        } catch (Exception exception) {
            throw new ExportException(exception);
        } finally {
            writer.close();
        }
    }

    /**
     * Exports the graph model to a file using the given writer
     * 
     * @throws IOException Exception is thrown if the export fails
     */
    public void exportFromWriter(Writer writer, IModelGraph modelGraph)
            throws IOException, ExportException {

        try {
            String output = getGraphOutput(modelGraph);
            writer.write(output);
        } catch (Exception exception) {
            throw new ExportException(exception);
        } finally {
            writer.close();
        }
    }

    /**
     * Representation of the root GRAPH
     */
    private String getGraphOutput(IModelGraph graph) {
        String output = "";
        output += getGraphInfo(graph);
        output += getGraphBody(graph);
        return output;
    }

    /**
     * Representation of the GRAPH declaration
     * 
     * Strict property is optionally
     */
    private String getGraphInfo(IModelGraph graph) {
        String graphStrict = graph.getProperty(KEY_STRICT_PROPERTY);
        String graphType = graph.getProperty(KEY_GRAPHTYPE_PROPERTY);
        String graphName = graph.getProperty(KEY_GRAPHNAME_PROPERTY);

        String graphRoot = "";
        graphRoot += getStrictProperty(graphStrict);
        graphRoot += graphType;
        graphRoot += NEW_SPACE;
        graphRoot += graphName;

        return graphRoot;
    }

    /**
     * Representation of STATEMENTS
     */
    private String getGraphBody(IModelGraph graph) {
        String body = "";
        body += BEGIN_GRAPHBODY;
        body += NEW_LINE;
        body += getGraphProperties(graph);
        body += getNodes(graph, true);
        body += getEdges(graph);
        body += getSubgraphs(graph);
        body += END_GRAPHBODY;
        return body;
    }

    /**
     * Representation of GRAPH ATTRIBUTES
     */
    private String getGraphProperties(IModelGraph graph) {
        Map<String, String> graphProperties = new Hashtable<String, String>();
        graphProperties.putAll(graph.getNonDefaultProperties());

        // Filter unnecessary properties (part of graph declaration)
        graphProperties.remove(KEY_GRAPHNAME_PROPERTY);
        graphProperties.remove(KEY_GRAPHTYPE_PROPERTY);
        graphProperties.remove(KEY_STRICT_PROPERTY);

        String outputGraph = "";
        if (graphProperties.size() > 0) {
            outputGraph += NEW_LINE;
            outputGraph += getTabs();
            outputGraph += DECLARATION_GRAPH;
            outputGraph += getProperties(graphProperties);
            outputGraph += NEW_LINE;
            outputGraph += NEW_LINE;
        }
        return outputGraph;
    }

    /**
     * Representation of a list of NODES
     */
    private String getNodes(IModelGraph graph, boolean includeProperties) {
        ArrayList<IModelNode> nodes = new ArrayList<IModelNode>(graph.getNodes());
        Collections.reverse(nodes);

        StringBuilder builder = new StringBuilder();
        for (IModelNode node : nodes) {
            builder.append(getNode(node, includeProperties));
        }
        builder.append(NEW_LINE);
        return builder.toString();
    }

    /**
     * Representation of a single NODE including the local properties
     */
    private String getNode(IModelNode node, boolean includeProperties) {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.putAll(node.getNonDefaultProperties());

        String outputNode = "";
        outputNode += getTabs();
        outputNode += getQuotedString(node.getName());
        outputNode += NEW_SPACE;
        if (includeProperties) {
            outputNode += getProperties(properties);
        }
        outputNode += NEW_LINE;
        return outputNode;
    }

    /**
     * Representation of a list of EDGES
     */
    private String getEdges(IModelGraph graph) {
        ArrayList<IModelEdge> edges = new ArrayList<IModelEdge>(graph
                .getEdges());
        Collections.reverse(edges);

        StringBuilder builder = new StringBuilder();
        for (IModelEdge edge : edges) {
            builder.append(getEdge(edge));
        }
        return builder.toString();
    }

    /**
     * Representation of a single EDGE including the local proeprties
     */
    private String getEdge(IModelEdge edge) {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.putAll(edge.getNonDefaultProperties());
        String source = edge.getSource().getName();
        String target = edge.getTarget().getName();

        String outputEdge = "";
        outputEdge += NEW_TAB;
        outputEdge += getQuotedString(source);
        outputEdge += getEdgeConnection(edge);
        outputEdge += getQuotedString(target);
        outputEdge += getProperties(properties);
        outputEdge += NEW_LINE;
        return outputEdge;
    }

    /**
     * Representation of a list of SUBGRAPHS
     */
    private String getSubgraphs(IModelGraph graph) {
        List<IModelSubgraph> subgraphs = graph.getDirectSubgraphs();
        Collections.reverse(subgraphs);

        indention++;

        StringBuilder builder = new StringBuilder();
        for (IModelSubgraph subgraph : subgraphs) {
            builder.append(NEW_LINE);
            builder.append(getSubgraph(subgraph));
            builder.append(NEW_LINE);
        }

        indention--;

        return builder.toString();
    }

    /**
     * Representation of a single SUBGRAPH
     * 
     * Differs from the normal graph representation in that the graph
     * information is handled differently and no edges are required inside a
     * subgraph.
     */
    private String getSubgraph(IModelSubgraph subgraph) {
        String output = "";
        output += getSubGraphInfo(subgraph);
        output += getSubGraphBody(subgraph);
        return output;
    }

    /**
     * Representation of the SUBGRAPH declaration
     * 
     * The name of the subgraph is optionally. The importer assigns a name to
     * the subgraph if not specified. The exported should filter these internal
     * subgraph names.
     */
    private String getSubGraphInfo(IModelSubgraph subgraph) {
        String info = "";

        indention--;

        info += getTabs();
        info += DECLARATION_SUBGRAPH;

        if (!subgraph.getName().startsWith(PREFIX_INTERNAL_SUBGRAPHNAME)) {
            info += NEW_SPACE;
            info += subgraph.getName();
        }
        indention++;

        return info;
    }

    /**
     * Representation of the STATEMENTS of the subgraph
     * 
     * Edges aren't specified in a subgraph but in the root graph
     */
    private String getSubGraphBody(IModelSubgraph subgraph) {
        String body = "";
        body += BEGIN_GRAPHBODY;
        body += NEW_LINE;
        body += getGraphProperties(subgraph);
        body += getNodes(subgraph, false);
        body += getSubgraphs(subgraph);
        body += getTabs(indention - 1);
        body += END_GRAPHBODY;
        return body;
    }

    /**
     * Representation of local or global PROPERTIES
     * 
     * Edges aren't specified in a subgraph but in the root graph
     */
    private String getProperties(Map<String, String> propertyTable) {
        if (propertyTable.size() > 0) {
            Iterator<String> keys = propertyTable.keySet().iterator();
            indention++;

            StringBuilder builder = new StringBuilder();
            while (keys.hasNext()) {
                String key = keys.next();
                builder.append(key);
                builder.append(SEPERATOR_KEY_VALUE);
                builder.append(getQuotedString(propertyTable.get(key)));
                builder.append(SEPERATOR_NEW_ATTRIBUTE);
            }

            indention--;

            String propertyOutput = "";
            String properties = builder.toString();
            propertyOutput += BEGIN_ATTRLIST;
            propertyOutput += properties.substring(0, properties.length() - 1);
            propertyOutput += END_ATTRLIST;

            return propertyOutput;
        }
        return "";
    }

    /**
     * Representation of the STRICT property graph
     */
    private String getStrictProperty(String value) {
        boolean isStrict = Boolean.parseBoolean(value);
        if (isStrict) {
            return DECLARATION_STRICT + NEW_SPACE;
        }
        return "";
    }

    /**
     * Representation of an edge connection based on the type of the graph
     * (graph | digraph)
     */
    private String getEdgeConnection(IModelEdge edge) {
        String graphType = edge.getGraph().getProperty(KEY_GRAPHTYPE_PROPERTY);
        if (DECLARATION_GRAPH.equals(graphType)) {
            return UNDIRECTED_EDGE;
        }
        return DIRECTED_EDGE;
    }

    /**
     * Adds quotes to a string value
     */
    private String getQuotedString(String value) {
        return DOUBLE_QUOTE + value + DOUBLE_QUOTE;
    }

    /**
     * Returns a sequence of tabs based on the current indention
     */
    private String getTabs() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indention; i++) {
            builder.append(NEW_TAB);
        }
        return builder.toString();
    }

    private String getTabs(int indentions) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indentions; i++) {
            builder.append(NEW_TAB);
        }
        return builder.toString();
    }
}