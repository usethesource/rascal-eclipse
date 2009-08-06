package org.dancingbear.graphbrowser.layout.zest;

import java.util.List;

import org.dancingbear.graphbrowser.layout.Layout;
import org.dancingbear.graphbrowser.layout.dot.FullEdge;
import org.dancingbear.graphbrowser.layout.dot.PointDouble;
import org.dancingbear.graphbrowser.layout.model.CubicBezierCurve;
import org.dancingbear.graphbrowser.layout.model.DirectedGraph;
import org.dancingbear.graphbrowser.layout.model.Edge;
import org.dancingbear.graphbrowser.layout.model.Spline;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.zest.layouts.InvalidLayoutConfiguration;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.LayoutRelationship;
import org.eclipse.zest.layouts.algorithms.AbstractLayoutAlgorithm;

/* 
 * This class delegates the layout to a given zest layout algorithm
 */
public class ZestLayout implements Layout {

	private AbstractLayoutAlgorithm algorithm;

	public ZestLayout(AbstractLayoutAlgorithm algorithm ) {
		this.algorithm = algorithm;
	}

	public void visit(DirectedGraph graph) {
		List entities = graph.getEntities();
		List relationships = graph.getRelationships();

		final LayoutEntity[] layoutEntities = new LayoutEntity[entities.size()];
		entities.toArray(layoutEntities);
		final LayoutRelationship[] layoutRelationships = new LayoutRelationship[relationships.size()];
		relationships.toArray(layoutRelationships);

		try {
			//TODO: find a way to determine the optimal size for the graph
			algorithm.applyLayout(layoutEntities, layoutRelationships, 0, 0, graph.getLayoutSize().height * 3, graph.getLayoutSize().width * 3, false, false);
		} catch (InvalidLayoutConfiguration e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//it seems that the algorithm only updates the nodes but not the points of the edge
		//we need an extra phase to update the edges
		for (Edge e: graph.getEdges()) {

			PointList pts = new PointList();
			pts.addPoint(e.getSource().getX(), e.getSource().getY());
			pts.addPoint(e.getTarget().getX(),  e.getTarget().getY());
			e.setPoints(pts);

			//construct a straight spline
			FullEdge fullEdge = new FullEdge(e, graph);
			PointDouble start = fullEdge.getEdgeStartPosition();
			PointDouble end = fullEdge.getEdgeEndPosition();
			double endScale = 1.0 / 3.0;
			double startScale = 1.0 - endScale;
			PointDouble control1 = start.scale(startScale).add(end.scale(endScale));
			PointDouble control2 = end.scale(startScale).add(start.scale(endScale));
			e.setSpline(new Spline(new CubicBezierCurve(start, control1, control2, end)));

		}

	}

}
