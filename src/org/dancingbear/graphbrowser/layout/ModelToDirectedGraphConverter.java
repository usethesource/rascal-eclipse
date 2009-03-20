/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.layout;

import java.util.ArrayList;
import java.util.Collection;

import org.dancingbear.graphbrowser.model.IModelEdge;
import org.dancingbear.graphbrowser.model.IModelGraph;
import org.dancingbear.graphbrowser.model.IModelNode;
import org.dancingbear.graphbrowser.model.IModelSubgraph;
import org.dancingbear.graphbrowser.model.ModelGraphRegister;
import org.eclipse.draw2d.geometry.Dimension;

public class ModelToDirectedGraphConverter {

    /**
     * Create a directed graph from a dot file containing the model
     * 
     * @param path the path where the dot file resides
     * @return the directed graph which has been created
     */
    public DirectedGraph convertToGraph(String path) {
        IModelGraph modelGraph = ModelGraphRegister.getInstance()
                .getModelGraph(path);

        ArrayList<IModelNode> subgraphNodes = new ArrayList<IModelNode>();
        for (IModelSubgraph sub : modelGraph.getSubgraphs()) {
            subgraphNodes.addAll(sub.getNodes());
        }

        // only direct nodes
        Collection<IModelNode> modelNodes = modelGraph.getNodes();
        NodeList directedGraphNodes = new NodeList();
        for (IModelNode modelNode : modelNodes) {
            if (!subgraphNodes.contains(modelNode)) {
                directedGraphNodes.add(createDirectedNode(modelNode));
            }
        }

        // subgraphs as nodes
        for (Node subGraph : buildSubGraphs(modelGraph)) {
            directedGraphNodes.add(subGraph);
        }

        // edges
        Collection<IModelEdge> modelEdges = modelGraph.getEdges();
        EdgeList directedGraphEdges = new EdgeList();
        for (IModelEdge modelEdge : modelEdges) {
            Edge edge = createDirectedEdge(directedGraphNodes, modelEdge);
            if (edge != null) { // do not add any edges to collapsed nodes
                directedGraphEdges.add(edge);
            }
        }

        // fill directed graph
        DirectedGraph graph = new DirectedGraph();
        graph.setNodes(directedGraphNodes);
        graph.setEdges(directedGraphEdges);

        return graph;
    }

    private ArrayList<Node> buildSubGraphs(IModelGraph modelGraph) {
        ArrayList<Node> directedSubGraphs = new ArrayList<Node>();

        for (IModelSubgraph sub : modelGraph.getDirectSubgraphs()) {
            // only filling data... not to be relied upon!
            Subgraph directedSubgraph = new Subgraph(sub.getName());// Id());
            directedSubgraph.setId(sub.getId().intValue());
            directedSubgraph.setProperties(sub.getProperties());
            directedSubgraph.setCluster(sub.isCluster());

            if ("false".equals(sub.getProperty("collapsed"))) {
                for (Node subsub : buildSubGraphs(sub)) {
                    directedSubgraph.addMember(subsub);
                }
                for (IModelNode modelNode : sub.getDirectNodes()) {
                    directedSubgraph.addMember(createDirectedNode(modelNode));
                }
            }

            directedSubGraphs.add(directedSubgraph);
        }

        return directedSubGraphs;
    }

    private Node createDirectedNode(IModelNode modelNode) {
        Node node = new Node(modelNode.getName());
        node.setId(modelNode.getId().intValue());
        int width = (int) Double.parseDouble(modelNode.getProperty("width"));
        int height = (int) Double.parseDouble(modelNode.getProperty("height"));
        node.setSize(new Dimension(width, height));

        node.setProperties(modelNode.getProperties());

        return node;
    }

    private Edge createDirectedEdge(NodeList nodes, IModelEdge modelEdge) {
        Node source = nodes.getNodeById(modelEdge.getSource().getId()
                .intValue());
        Node target = nodes.getNodeById(modelEdge.getTarget().getId()
                .intValue());
        if (source == null || target == null) {
            return null;
        }

        Edge edge = new Edge(source, target);
        edge.setId(modelEdge.getId().intValue());
        edge.setProperties(modelEdge.getProperties());

        return edge;
    }
}
