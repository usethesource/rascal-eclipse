package org.dancingbear.graphbrowser.layout.hypergraph;


import java.util.ArrayList;
import java.util.List;

import org.dancingbear.graphbrowser.layout.ComputeSplines;
import org.dancingbear.graphbrowser.layout.CubicBezierCurve;
import org.dancingbear.graphbrowser.layout.DirectedGraph;
import org.dancingbear.graphbrowser.layout.DirectedGraphLayout;
import org.dancingbear.graphbrowser.layout.Edge;
import org.dancingbear.graphbrowser.layout.Node;
import org.dancingbear.graphbrowser.layout.PointDouble;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

public class HyperbolicLayout {
	
	private double delta_x, delta_y, max_radius;
	
	/**
	 * Lays out the given graph
	 * 
	 * @param graph the graph to layout
	 */
	public void visit(DirectedGraph graph) {
		List<PolarCoordinate> polars = new ArrayList<PolarCoordinate>();
		//setCenter(graph.getNode(1, 0));
		for(Node n: graph.getNodes()) {
			polars.add(new CartesianCoordinate(n.getX(), n.getY()).getPolar());
		}
		for(PolarCoordinate p: polars) {
			if (p.getRadius() >max_radius) max_radius = p.getRadius();
		}
			
		
		for(Node n: graph.getNodes()) modifyNode(n);
		for(Edge e: graph.getEdges()) {
			if (e.getVNodes()!=null) {
				for (Node n : e.getVNodes()) {
					modifyNode(n);
				}
			}
			PointList old_points = e.getPoints();
			PointList new_points = new PointList(old_points.size());
			for (int i = 0; i < old_points.size(); i++) {
				Point p = old_points.getPoint(i);
				CartesianCoordinate new_coordinate = newCoordinate(new CartesianCoordinate(p.preciseX(), p.preciseY()));
				new_points.addPoint(new Point(new_coordinate.getAbscissa(), new_coordinate.getOrdinate()));
			}
			e.setPoints(new_points);
			if (e.getSpline() != null) {
				List<CubicBezierCurve> curves = e.getSpline().getCurves();
				int i = 0;
				for (CubicBezierCurve curve : curves) {
					List<PointDouble> newpoints = new ArrayList<PointDouble>();
					for(PointDouble p: curve.getControlPoints()) {
						CartesianCoordinate new_coordinate = newCoordinate(new CartesianCoordinate(p.getX(), p.getY()));
						newpoints.add(new PointDouble(new_coordinate.getAbscissa(), new_coordinate.getOrdinate()));		
					}
					curves.set(i, new CubicBezierCurve(newpoints));
					i++;
				}
			}
		}
	}


	public CartesianCoordinate newCoordinate(CartesianCoordinate c) {
		return c.translate(delta_x,delta_y).getPolar().getHyperbolic(10.0 / max_radius).getCartesian().translate(500,500);
	}
	
	public void setCenter(Node n) {
		delta_x = - n.getX();
		delta_y = - n.getY();
	}
	
	public void modifyNode(Node n) {
		CartesianCoordinate new_coordinate = newCoordinate(new CartesianCoordinate(n.getX(), n.getY()));
		n.setX((int)new_coordinate.getAbscissa());
		n.setY((int)new_coordinate.getOrdinate());
	}



}
