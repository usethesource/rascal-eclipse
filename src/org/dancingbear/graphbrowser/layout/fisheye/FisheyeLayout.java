package org.dancingbear.graphbrowser.layout.fisheye;

import java.util.ArrayList;
import java.util.List;

import org.dancingbear.graphbrowser.layout.Layout;
import org.dancingbear.graphbrowser.layout.dot.PointDouble;
import org.dancingbear.graphbrowser.layout.model.CubicBezierCurve;
import org.dancingbear.graphbrowser.layout.model.DirectedGraph;
import org.dancingbear.graphbrowser.layout.model.Edge;
import org.dancingbear.graphbrowser.layout.model.Node;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

public class FisheyeLayout implements Layout {

	private double delta_x;
	private double delta_y;
	private double max_radius;
	private DirectedGraph graph;

	public FisheyeLayout(Node origin) {
		delta_x = -origin.getX();
		delta_y = -origin.getY();
		max_radius = 0;
	}

	/**
	 * Lays out the given graph
	 * 
	 */
	public void visit(DirectedGraph graph) {
		init(graph);
		translateInHyperbolicPlan();
		centering();
	}

	private void init(DirectedGraph graph) {
		this.graph = graph;
		List<PolarCoordinate> polars = new ArrayList<PolarCoordinate>();
		for (Node n : graph.getNodes()) {
			polars.add(new CartesianCoordinate(n.getX(), n.getY()).getPolar());
		}
		for (PolarCoordinate p : polars) {
			if (p.getRadius() > max_radius)
				max_radius = p.getRadius();
		}

	}

	public CartesianCoordinate calculateHyperbolicCoordinate(
			CartesianCoordinate c) {
		double factor = 10.0 / max_radius;
		return c.translate(delta_x, delta_y).getPolar().getHyperbolic(factor)
		.getCartesian().translate(500, 500);
	}

	public void calculateHyperbolicNode(Node n) {
		CartesianCoordinate new_coordinate = calculateHyperbolicCoordinate(new CartesianCoordinate(
				n.getX(), n.getY()));
		n.setX((int) new_coordinate.getAbscissa());
		n.setY((int) new_coordinate.getOrdinate());
	}

	public void translateInHyperbolicPlan() {
		for (Node n : graph.getNodes())
			calculateHyperbolicNode(n);
		for (Edge e : graph.getEdges()) {
			PointList old_points = e.getPoints();
			PointList new_points = new PointList(old_points.size());
			for (int i = 0; i < old_points.size(); i++) {
				Point p = old_points.getPoint(i);
				CartesianCoordinate new_coordinate = calculateHyperbolicCoordinate(new CartesianCoordinate(
						p.preciseX(), p.preciseY()));
				new_points.addPoint(new Point(new_coordinate.getAbscissa(),
						new_coordinate.getOrdinate()));
			}
			e.setPoints(new_points);
			if (e.getSpline() != null) {
				List<CubicBezierCurve> curves = e.getSpline().getCurves();
				int i = 0;
				for (CubicBezierCurve curve : curves) {
					List<PointDouble> newpoints = new ArrayList<PointDouble>();
					for (PointDouble p : curve.getControlPoints()) {
						CartesianCoordinate new_coordinate = calculateHyperbolicCoordinate(new CartesianCoordinate(
								p.getX(), p.getY()));
						newpoints.add(new PointDouble(new_coordinate
								.getAbscissa(), new_coordinate.getOrdinate()));
					}
					curves.set(i, new CubicBezierCurve(newpoints));
					i++;
				}
			}
		}
	}

	public void centering() {
		double min_x = Double.MAX_VALUE;
		double min_y = Double.MAX_VALUE;
		double max_x = 0;
		double max_y = 0;
		for (Node n : graph.getNodes()) {
			if (min_x > n.getX())
				min_x = n.getX();
			if (min_y > n.getY())
				min_y = n.getY();
			if (max_x < n.getX())
				max_x = n.getX();
			if (max_y < n.getY())
				max_y = n.getY();
		}
		double delta_x = 100;
		double delta_y = 100;
		if (min_x < 0)
			delta_x -= min_x;
		if (min_y < 0)
			delta_y -= min_y;
		for (Node n : graph.getNodes()) {
			CartesianCoordinate new_coordinate = new CartesianCoordinate(n
					.getX(), n.getY()).translate(delta_x, delta_y);
			n.setX((int) new_coordinate.getAbscissa());
			n.setY((int) new_coordinate.getOrdinate());
		}

		for (Edge e : graph.getEdges()) {
			PointList old_points = e.getPoints();
			PointList new_points = new PointList(old_points.size());
			for (int i = 0; i < old_points.size(); i++) {
				Point p = old_points.getPoint(i);
				CartesianCoordinate new_coordinate = new CartesianCoordinate(p
						.preciseX(), p.preciseY()).translate(delta_x, delta_y);
				new_points.addPoint(new Point(new_coordinate.getAbscissa(),
						new_coordinate.getOrdinate()));
			}
			e.setPoints(new_points);
			if (e.getSpline() != null) {
				List<CubicBezierCurve> curves = e.getSpline().getCurves();
				int i = 0;
				for (CubicBezierCurve curve : curves) {
					List<PointDouble> newpoints = new ArrayList<PointDouble>();
					for (PointDouble p : curve.getControlPoints()) {
						CartesianCoordinate new_coordinate = new CartesianCoordinate(
								p.getX(), p.getY()).translate(delta_x, delta_y);
						newpoints.add(new PointDouble(new_coordinate
								.getAbscissa(), new_coordinate.getOrdinate()));
					}
					curves.set(i, new CubicBezierCurve(newpoints));
					i++;
				}
			}
		}
	}

}
