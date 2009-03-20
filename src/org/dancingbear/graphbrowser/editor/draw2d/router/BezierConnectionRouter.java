/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.router;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.dancingbear.graphbrowser.editor.draw2d.PointUtilities;
import org.dancingbear.graphbrowser.model.CubicCurve;
import org.dancingbear.graphbrowser.model.Position;
import org.dancingbear.graphbrowser.model.Spline;
import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.AbstractRouter;
import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;

/**
 * Bezier connection router for splines.
 * 
 * @see "http://en.wikipedia.org/wiki/Bezier_curve"
 * 
 * @author Jeroen van Schagen
 * @date 19-02-2009
 */
public class BezierConnectionRouter extends AbstractRouter {

    private final Hashtable<Connection, Spline> constraints = new Hashtable<Connection, Spline>();

    /**
     * Route connection so it shapes a beautiful Bezier spline. TODO: Keep track
     * of source and target anchor positions so the line stays fluent.
     * 
     * @param connection Connection
     */
    public void route(Connection connection) {
        PointList points = connection.getPoints();
        points.removeAllPoints();

        // Retrieve constraints
        Spline spline = constraints.get(connection);
        if (spline == null || spline.size() == 0) {
            return;
        }
        List<Bendpoint> bendpoints = calculateBendPoints(spline);

        // Retrieve absolute source and target position
        Point source = PointUtilities.toPoint(spline.getStartPosition());
        connection.translateToAbsolute(source);
        Point target = PointUtilities.toPoint(spline.getEndPosition());
        connection.translateToAbsolute(target);

        // Attach relative source anchor
        PrecisionPoint referencePoint = new PrecisionPoint();
        referencePoint.setLocation(connection.getSourceAnchor().getLocation(
                source));
        connection.translateToRelative(referencePoint);
        points.addPoint(referencePoint);

        // Attach additional bend points
        for (Bendpoint bendpoint : bendpoints) {
            points.addPoint(bendpoint.getLocation());
        }

        // Attach relative target anchor
        referencePoint.setLocation(connection.getTargetAnchor().getLocation(
                target));
        connection.translateToRelative(referencePoint);
        points.addPoint(referencePoint);

        // Store bend points
        connection.setPoints(points);
    }

    /**
     * Calculate bend points of spline (collection of curves)
     * 
     * @param curves
     * @return bendpoints
     */
    private List<Bendpoint> calculateBendPoints(Spline curves) {
        List<Bendpoint> bendpoints = new ArrayList<Bendpoint>();

        for (CubicCurve curve : curves) {
            // Attach bend points of each curve
            bendpoints.addAll(calculateBendPoint(curve));
        }

        return bendpoints;
    }

    /**
     * 
     * @param curveFraction The fraction of the curve
     * @return The multiplier of the first control point
     */
    private double bezierPoint0(double curveFraction) {
        double reversedFraction = 1.0 - curveFraction;
        return (reversedFraction * reversedFraction * reversedFraction);
    }

    /**
     * 
     * @param curveFraction The fraction of the curve
     * @return The multiplier of the second control point
     */
    private double bezierPoint1(double curveFraction) {
        double reversedFraction = 1.0 - curveFraction;
        return 3 * curveFraction * (reversedFraction * reversedFraction);
    }

    /**
     * 
     * @param curveFraction The fraction of the curve
     * @return The multiplier of the third control point
     */
    private double bezierPoint2(double curveFraction) {
        double reversedFraction = 1.0 - curveFraction;
        return 3 * (curveFraction * curveFraction) * reversedFraction;
    }

    /**
     * 
     * @param curveFraction The fraction of the curve
     * @return The multiplier of the last control point
     */
    private double bezierPoint3(double curveFraction) {
        return curveFraction * curveFraction * curveFraction;
    }

    /**
     * 
     * @param position The position to be moved
     * @param curve The curve on which the position should be moved
     * @param forward Whether the point should be more forward or backward
     */
    private void movePoint(Position position, CubicCurve curve, boolean forward) {
        double xLength = curve.getTargetPosition().getX()
                - curve.getSourcePosition().getX();
        double yLength = curve.getTargetPosition().getY()
                - curve.getSourcePosition().getY();

        if (forward) {
            position.setX(position.getX() + xLength / 3);
            position.setY(position.getY() + yLength / 3);
        } else {
            position.setX(position.getX() - xLength / 3);
            position.setY(position.getY() - yLength / 3);
        }
    }

    /**
     * Calculate bend points of curve.
     * 
     * @param curve
     * @return bendpoints
     */
    private List<Bendpoint> calculateBendPoint(CubicCurve curve) {
        List<Bendpoint> bendpoints = new ArrayList<Bendpoint>();

        if (curve.isStraightLine()) {
            Position sourceVectorPosition = new Position(curve
                    .getSourcePosition().getX(), curve.getSourcePosition()
                    .getY());
            movePoint(sourceVectorPosition, curve, true);

            Position targetVectorPosition = new Position(curve
                    .getTargetPosition().getX(), curve.getTargetPosition()
                    .getY());
            movePoint(targetVectorPosition, curve, false);

            bendpoints.add(new AbsoluteBendpoint(PointUtilities
                    .toPoint(sourceVectorPosition)));
            bendpoints.add(new AbsoluteBendpoint(PointUtilities
                    .toPoint(targetVectorPosition)));

            return bendpoints;
        }

        final double CURVE_STEP_SIZE = 0.05;
        for (double curveFraction = 0; curveFraction < 1; curveFraction += CURVE_STEP_SIZE) {
            Position startPointScaled = curve.getSourcePosition().scale(
                    bezierPoint0(curveFraction));
            Position firstControlPointScaled = curve.getDirectionVector(
                    CubicCurve.SOURCE_VECTOR_INDEX).scale(
                    bezierPoint1(curveFraction));
            Position secondControlPointScaled = curve.getDirectionVector(
                    CubicCurve.TARGET_VECTOR_INDEX).scale(
                    bezierPoint2(curveFraction));
            Position endPointScaled = curve.getTargetPosition().scale(
                    bezierPoint3(curveFraction));

            Position bendPoint = startPointScaled.translate(
                    firstControlPointScaled)
                    .translate(secondControlPointScaled).translate(
                            endPointScaled);
            bendpoints.add(new AbsoluteBendpoint((int) bendPoint.getX(),
                    (int) bendPoint.getY()));
        }

        return bendpoints;
    }

    /**
     * Retrieve connection's spline.
     * 
     * @param connection Connection
     */
    @Override
    public Object getConstraint(Connection connection) {
        return constraints.get(connection);
    }

    /**
     * Set spline for connection.
     * 
     * @param connection Connection
     * @param constraint Spline
     */
    @Override
    public void setConstraint(Connection connection, Object constraint) {
        if (constraint instanceof Spline) {
            constraints.put(connection, (Spline) constraint);
        }
    }

    /**
     * Remove connection from router
     * 
     * @param connection Connection
     */
    @Override
    public void remove(Connection connection) {
        constraints.remove(connection);
    }

}