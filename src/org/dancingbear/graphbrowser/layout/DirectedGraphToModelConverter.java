/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.layout;

import org.dancingbear.graphbrowser.model.CubicCurve;
import org.dancingbear.graphbrowser.model.IModelEdge;
import org.dancingbear.graphbrowser.model.IModelGraph;
import org.dancingbear.graphbrowser.model.IModelSubgraph;
import org.dancingbear.graphbrowser.model.ModelGraphRegister;
import org.dancingbear.graphbrowser.model.Position;

public class DirectedGraphToModelConverter {
    /**
     * Convert a direct graph to a model, injecting the specified name
     * 
     * @param directedGraph the graph
     * @param graphName the name to inject
     * @return the created model
     */
    public IModelGraph convertToModel(DirectedGraph directedGraph,
            String graphName) {

        IModelGraph modelGraph = ModelGraphRegister.getInstance()
                .getModelGraph(graphName);

        writeNodesToModel(modelGraph, directedGraph.getNodes());
        writeEdgesToModel(modelGraph, directedGraph.getEdges());

        return modelGraph;
    }

    @SuppressWarnings("deprecation")
    private void writeNodesToModel(IModelGraph modelGraph, NodeList nodes) {
        for (Node node : nodes) {
            if (node instanceof Subgraph) {
                Subgraph subgraph = (Subgraph) node;
                if ("false".equals(subgraph.getProperties().get("collapsed"))) {
                    writeSubgraphToModel(modelGraph, subgraph);
                } else {
                    modelGraph.getSubgraph(Integer.valueOf(subgraph.getId()))
                            .setPosition(subgraph.getX(), subgraph.getY());
                }
            } else if (!node.isVirtualNode()) {
                modelGraph.getNode(Integer.valueOf(node.getId())).setPosition(
                        node.getX(), node.getY());
            }
        }
    }

    private void writeSubgraphToModel(IModelGraph modelGraph, Subgraph subgraph) {
        IModelSubgraph modelSubgraph = modelGraph.getSubgraph(Integer
                .valueOf(subgraph.getId()));

        if (null != modelSubgraph && subgraph.isCluster()) {
            modelSubgraph.setProperty("width", Integer.toString(subgraph
                    .getWidth()));
            modelSubgraph.setProperty("height", Integer.toString(subgraph
                    .getHeight()));
            modelSubgraph.setPosition(subgraph.getX(), subgraph.getY());
        }

        writeNodesToModel(modelGraph, subgraph.getMembers());
    }

    private void writeEdgesToModel(IModelGraph modelGraph, EdgeList edges) {
        for (Edge edge : edges) {
            IModelEdge modelEdge = modelGraph.getEdge(Integer.valueOf(edge
                    .getId()));
            modelEdge.setSpline(buildSpline(edge));
        }
    }

    private org.dancingbear.graphbrowser.model.Spline buildSpline(Edge edge) {
        org.dancingbear.graphbrowser.model.Spline spline = new org.dancingbear.graphbrowser.model.Spline();

        if (edge.getSpline() == null) {
            // no spline has been defined for this edge, so draw a straight line
            CubicCurve curve = new CubicCurve();
            curve.setSourcePosition(new Position(edge.getSource().getX(), edge
                    .getSource().getY()));
            curve.setTargetPosition(new Position(edge.getTarget().getX(), edge
                    .getTarget().getY()));
            spline.add(curve);
            return spline;
        }

        for (CubicBezierCurve curve : edge.getSpline().getCurves()) {
            CubicCurve modelCurve = new CubicCurve();

            modelCurve.setSourcePosition(new Position(curve.getStartPoint()));
            modelCurve.setDirectionVector(CubicCurve.SOURCE_VECTOR_INDEX,
                    new Position(curve.getFirstControlPoint()));
            modelCurve.setDirectionVector(CubicCurve.TARGET_VECTOR_INDEX,
                    new Position(curve.getSecondControlPoint()));
            modelCurve.setTargetPosition(new Position(curve.getEndPoint()));

            spline.add(modelCurve);
        }

        return spline;
    }
}
