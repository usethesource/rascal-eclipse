/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.dancingbear.graphbrowser.layout;

import java.util.Iterator;
import java.util.Stack;

/**
 * Assigns the final rank assignment for a DirectedGraph with an initial
 * feasible spanning tree.
 * 
 * @author Randy Hudson
 * @since 2.1.2
 */
class RankAssignmentSolver extends SpanningTreeVisitor {

    DirectedGraph graph;
    EdgeList spanningTree;
    boolean searchDirection;

    int depthFirstCutValue(Edge edge, int count) {
        Node n = getTreeTail(edge);
        setTreeMin(n, count);
        int cutvalue = 0;
        int multiplier = (edge.getTarget() == n) ? 1 : -1;
        EdgeList list;

        list = n.getOutgoing();
        Edge e;
        for (int i = 0; i < list.size(); i++) {
            e = list.getEdge(i);
            if (e.tree && e != edge) {
                count = depthFirstCutValue(e, count);
                cutvalue += (e.cut - e.getWeight()) * multiplier;
            } else {
                cutvalue -= e.getWeight() * multiplier;
            }
        }
        list = n.getIncoming();
        for (int i = 0; i < list.size(); i++) {
            e = list.getEdge(i);
            if (e.tree && e != edge) {
                count = depthFirstCutValue(e, count);
                cutvalue -= (e.cut - e.getWeight()) * multiplier;
            } else {
                cutvalue += e.getWeight() * multiplier;
            }
        }

        edge.cut = cutvalue;
        if (cutvalue < 0)
            spanningTree.add(edge);
        setTreeMax(n, count);
        return count + 1;
    }

    /**
     * returns the Edge which should be entered.
     * 
     * @param branch
     * @return Edge
     */
    Edge enter(Node branch) {
        Node n;
        Edge result = null;
        int minSlack = Integer.MAX_VALUE;
        boolean incoming = getParentEdge(branch).getTarget() != branch;
        // searchDirection = !searchDirection;
        for (int i = 0; i < graph.getNodes().size(); i++) {
            if (searchDirection)
                n = graph.getNodes().getNode(i);
            else
                n = graph.getNodes().getNode(graph.getNodes().size() - 1 - i);
            if (subtreeContains(branch, n)) {
                EdgeList edges;
                if (incoming)
                    edges = n.getIncoming();
                else
                    edges = n.getOutgoing();
                for (int j = 0; j < edges.size(); j++) {
                    Edge e = edges.getEdge(j);
                    if (!subtreeContains(branch, e.opposite(n)) && !e.tree
                            && e.getSlack() < minSlack) {
                        result = e;
                        minSlack = e.getSlack();
                    }
                }
            }
        }
        return result;
    }

    int getTreeMax(Node n) {
        return n.getWorkingInts()[1];
    }

    int getTreeMin(Node n) {
        return n.getWorkingInts()[0];
    }

    void initCutValues() {
        Node root = graph.getNodes().getNode(0);
        spanningTree = new EdgeList();
        Edge e;
        setTreeMin(root, 1);
        setTreeMax(root, 1);

        for (int i = 0; i < root.getOutgoing().size(); i++) {
            e = root.getOutgoing().getEdge(i);
            if (!getSpanningTreeChildren(root).contains(e))
                continue;
            setTreeMax(root, depthFirstCutValue(e, getTreeMax(root)));
        }
        for (int i = 0; i < root.getIncoming().size(); i++) {
            e = root.getIncoming().getEdge(i);
            if (!getSpanningTreeChildren(root).contains(e))
                continue;
            setTreeMax(root, depthFirstCutValue(e, getTreeMax(root)));
        }
    }

    Edge leave() {
        Edge result = null;
        Edge e;
        int minCut = 0;
        int weight = -1;
        for (int i = 0; i < spanningTree.size(); i++) {
            e = spanningTree.getEdge(i);
            if (e.cut < minCut) {
                result = e;
                minCut = result.cut;
                weight = result.getWeight();
            } else if (e.cut == minCut && e.getWeight() > weight) {
                result = e;
                weight = result.getWeight();
            }
        }
        return result;
    }

    void networkSimplexLoop() {
        Edge leave, enter;
        int count = 0;
        while ((leave = leave()) != null && count < 900) {

            count++;

            Node leaveTail = getTreeTail(leave);
            Node leaveHead = getTreeHead(leave);

            enter = enter(leaveTail);
            if (enter == null)
                break;

            // Break the "leave" edge from the spanning tree
            getSpanningTreeChildren(leaveHead).remove(leave);
            setParentEdge(leaveTail, null);
            leave.tree = false;
            spanningTree.remove(leave);

            Node enterTail = enter.getSource();
            if (!subtreeContains(leaveTail, enterTail))
                // Oops, wrong end of the edge
                enterTail = enter.getTarget();
            Node enterHead = enter.opposite(enterTail);

            // Prepare enterTail by making it the root of its sub-tree
            updateSubgraph(enterTail);

            // Add "enter" edge to the spanning tree
            getSpanningTreeChildren(enterHead).add(enter);
            setParentEdge(enterTail, enter);
            enter.tree = true;

            repairCutValues(enter);

            Node commonAncestor = enterHead;

            while (!subtreeContains(commonAncestor, leaveHead)) {
                repairCutValues(getParentEdge(commonAncestor));
                commonAncestor = getTreeParent(commonAncestor);
            }
            while (leaveHead != commonAncestor) {
                repairCutValues(getParentEdge(leaveHead));
                leaveHead = getTreeParent(leaveHead);
            }
            updateMinMax(commonAncestor, getTreeMin(commonAncestor));
            tightenEdge(enter);
        }
    }

    private void normalizeForest() {
        NodeList tree = new NodeList();
        graph.resetNodeFlags();
        graph.getForestRoot().setFlag(true);
        EdgeList rootEdges = graph.getForestRoot().getOutgoing();
        Stack<Node> stack = new Stack<Node>();
        for (int i = 0; i < rootEdges.size(); i++) {
            Node node = rootEdges.getEdge(i).getTarget();
            node.setFlag(true);
            stack.push(node);
            while (!stack.isEmpty()) {
                node = stack.pop();
                tree.add(node);
                Iterator<Node> neighbors = node.iteratorNeighbors();
                while (neighbors.hasNext()) {
                    Node neighbor = neighbors.next();
                    if (!neighbor.isFlag()) {
                        neighbor.setFlag(true);
                        stack.push(neighbor);
                    }
                }
            }
            tree.normalizeRanks();
            tree.clear();
        }
    }

    void repairCutValues(Edge edge) {
        spanningTree.remove(edge);
        Node n = getTreeTail(edge);
        int cutvalue = 0;
        int multiplier = (edge.getTarget() == n) ? 1 : -1;
        EdgeList list;

        list = n.getOutgoing();
        Edge e;
        for (int i = 0; i < list.size(); i++) {
            e = list.getEdge(i);
            if (e.tree && e != edge)
                cutvalue += (e.cut - e.getWeight()) * multiplier;
            else
                cutvalue -= e.getWeight() * multiplier;
        }
        list = n.getIncoming();
        for (int i = 0; i < list.size(); i++) {
            e = list.getEdge(i);
            if (e.tree && e != edge)
                cutvalue -= (e.cut - e.getWeight()) * multiplier;
            else
                cutvalue += e.getWeight() * multiplier;
        }

        edge.cut = cutvalue;
        if (cutvalue < 0)
            spanningTree.add(edge);
    }

    void setTreeMax(Node n, int value) {
        n.getWorkingInts()[1] = value;
    }

    void setTreeMin(Node n, int value) {
        n.getWorkingInts()[0] = value;
    }

    boolean subtreeContains(Node parent, Node child) {
        return parent.getWorkingInts()[0] <= child.getWorkingInts()[1]
                && child.getWorkingInts()[1] <= parent.getWorkingInts()[1];
    }

    void tightenEdge(Edge edge) {
        Node tail = getTreeTail(edge);
        int delta = edge.getSlack();
        if (tail == edge.getTarget())
            delta = -delta;
        Node n;
        for (int i = 0; i < graph.getNodes().size(); i++) {
            n = graph.getNodes().getNode(i);
            if (subtreeContains(tail, n))
                n.setRank(n.getRank() + delta);
        }
    }

    int updateMinMax(Node root, int count) {
        setTreeMin(root, count);
        EdgeList edges = getSpanningTreeChildren(root);
        for (int i = 0; i < edges.size(); i++)
            count = updateMinMax(getTreeTail(edges.getEdge(i)), count);
        setTreeMax(root, count);
        return count + 1;
    }

    void updateSubgraph(Node root) {
        Edge flip = getParentEdge(root);
        if (flip != null) {
            Node rootParent = getTreeParent(root);
            getSpanningTreeChildren(rootParent).remove(flip);
            updateSubgraph(rootParent);
            setParentEdge(root, null);
            setParentEdge(rootParent, flip);
            repairCutValues(flip);
            getSpanningTreeChildren(root).add(flip);
        }
    }

    public void visit(DirectedGraph graph) {
        this.graph = graph;
        initCutValues();
        networkSimplexLoop();
        if (graph.getForestRoot() == null) {
            NodeList temp = graph.getNodes();
            temp.normalizeRanks();
            graph.setNodes(temp);
        } else
            normalizeForest();
    }

}
