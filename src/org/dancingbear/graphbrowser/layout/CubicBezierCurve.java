/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Hartog, Lars de Ridder
 * 
 */
public class CubicBezierCurve implements Cloneable {

    final static double DELTA = 1.0e-6;

    private final static int MAX_REFINE_ITERATIONS = 40;
    private final static double REFINEMENT_FACTOR_INCREMENT = 1.2;

    private final static double STRAIGHTEN_FACTOR_INCREMENT = 0.1;

    private final static double CURVE_SAMPLE_SIZE = 0.05;

    private final int LEFT = 0;
    private final int RIGHT = 1;
    private final int CROSS_L_R = 2;
    private final int CROSS_R_L = 3;
    private final int[] refinements = { LEFT, RIGHT, CROSS_L_R, CROSS_R_L };

    private PointDouble start;
    private ControlPoints controls;
    private PointDouble end;

    private boolean isStartTangentFixed;

    private class ControlPoints {
        private PointDouble first;
        private PointDouble second;

        public ControlPoints(PointDouble control1, PointDouble control2) {
            first = control1;
            second = control2;
        }

        public PointDouble getFirst() {
            return first;
        }

        public PointDouble getSecond() {
            return second;
        }

        public ControlPoints translateLeft(double factor) {
            PointDouble control1 = isStartTangentFixed() ? first : first
                    .translateLeft(factor);
            return new ControlPoints(control1, second.translateLeft(factor));
        }

        public ControlPoints translateRight(double factor) {
            PointDouble control1 = isStartTangentFixed() ? first : first
                    .translateRight(factor);
            return new ControlPoints(control1, second.translateRight(factor));
        }

        public ControlPoints translateCrossLR(double factor) {
            if (!isStartTangentFixed()) {
                return new ControlPoints(first.translateLeft(factor), second
                        .translateRight(factor));
            }

            return null;
        }

        public ControlPoints translateCrossRL(double factor) {
            if (!isStartTangentFixed()) {
                return new ControlPoints(first.translateRight(factor), second
                        .translateLeft(factor));
            }

            return null;
        }

        public ControlPoints straighten(double factor) {
            double startFactor = 1.0 - factor;
            double endFactor = factor;

            PointDouble control1 = getStartPoint().scale(startFactor).add(
                    first.scale(endFactor));
            PointDouble control2 = getEndPoint().scale(startFactor).add(
                    second.scale(endFactor));

            return new ControlPoints(control1, control2);
        }

        public void invert() {
            PointDouble temp = first;
            first = second;
            second = temp;
        }
    }

    /**
     * Create a new bezier curve
     * 
     * @param points A list of the four control points to create a bezier curve.
     */
    public CubicBezierCurve(List<PointDouble> points) {
        this(points.get(0), points.get(1), points.get(2), points.get(3));
    }

    /**
     * Create a new bezier curve
     * 
     * @param startPoint The start point of the edge
     * @param control1 The first control point of the curve
     * @param control2 The second control point of the curve
     * @param endPoint The end point of the edge
     */
    public CubicBezierCurve(PointDouble startPoint, PointDouble control1,
            PointDouble control2, PointDouble endPoint) {
        start = startPoint;
        controls = new ControlPoints(control1, control2);
        end = endPoint;

        isStartTangentFixed = false;
    }

    /**
     * 
     * Calculate the point on this curve that corresponds to the given curve
     * fraction.
     * 
     * @param curveFraction The fraction of the curve the point lies on
     * @return The coordinate of the point in the curve at the given fraction
     */
    public PointDouble calcBezierCoordinate(double curveFraction) {
        double startPointScale = 1.0 - curveFraction;
        double endPointScale = curveFraction;

        PointDouble P0 = getStartPoint();
        PointDouble P1 = getFirstControlPoint();
        PointDouble P2 = getSecondControlPoint();
        PointDouble P3 = getEndPoint();

        PointDouble P01 = P0.scale(startPointScale)
                .add(P1.scale(endPointScale));
        PointDouble P12 = P1.scale(startPointScale)
                .add(P2.scale(endPointScale));
        PointDouble P23 = P2.scale(startPointScale)
                .add(P3.scale(endPointScale));

        PointDouble P012 = P01.scale(startPointScale).add(
                P12.scale(endPointScale));
        PointDouble P123 = P12.scale(startPointScale).add(
                P23.scale(endPointScale));

        PointDouble result = P012.scale(startPointScale).add(
                P123.scale(endPointScale));

        return result;
    }

    @Override
    public CubicBezierCurve clone() {
        try {
            return (CubicBezierCurve) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<PointDouble> getControlPoints() {
        ArrayList<PointDouble> points = new ArrayList<PointDouble>();
        points.add(start);
        points.add(controls.getFirst());
        points.add(controls.getSecond());
        points.add(end);
        return points;
    }

    public PointDouble getStartPoint() {
        return start;
    }

    public PointDouble getFirstControlPoint() {
        return controls.getFirst();
    }

    public PointDouble getSecondControlPoint() {
        return controls.getSecond();
    }

    public PointDouble getEndPoint() {
        return end;
    }

    /**
     * Calculate the point in the list of input points that this curve should be
     * split on.
     * 
     * @param points A list of points this spline should pass through.
     * @return The index of the found point in the input point list.
     */
    public int getBestSplitPoint(List<PointDouble> points) {
        double maxMinDistanceToPoint = 0;
        int maxIndex = -1;

        for (PointDouble point : points) {
            double curveFraction = getLastFractionBefore(0.0, point.getY(),
                    CURVE_SAMPLE_SIZE);
            double distanceBefore = point
                    .getDistance(calcBezierCoordinate(curveFraction));
            double distanceAfter = point
                    .getDistance(calcBezierCoordinate(curveFraction
                            + CURVE_SAMPLE_SIZE));

            double minDistanceToPoint;
            if (distanceBefore < distanceAfter) {
                minDistanceToPoint = distanceBefore;
            } else {
                minDistanceToPoint = distanceAfter;
            }

            if (minDistanceToPoint > maxMinDistanceToPoint) {
                maxMinDistanceToPoint = minDistanceToPoint;
                maxIndex = points.indexOf(point);
            }
        }
        return maxIndex;
    }

    /**
     * Retrieves the last fraction that results in a y position on the curve
     * that is smaller than the given y-coordinate
     * 
     * @param startFraction The fraction from which the search should start
     * @param yCoordinate The limiting y-coordinate
     * @param step The size of the steps in fractions that should be taken
     * 
     * @return The fraction right before the limit, -1.0 if the starting
     * fraction already overshoots, > 1.0 if the curve terminates before the
     * limit
     */
    public double getLastFractionBefore(double startFraction,
            double yCoordinate, double step) {
        if (calcBezierCoordinate(startFraction).getY() > yCoordinate) {
            return -1.0;
        }
        double result = startFraction;
        double nextStep = result + step;
        double bYCoordinate = calcBezierCoordinate(nextStep).getY();
        while (result < 1.0 && bYCoordinate < yCoordinate) {
            result = nextStep;
            nextStep += step;
            bYCoordinate = calcBezierCoordinate(nextStep).getY();
        }
        return result;
    }

    /**
     * Replace the current control points with the refined control points and
     * see if it fits. Resets to the original points if it does not.
     * 
     * @param intersectionLines List of horizontal lines that the curve should
     * pass through.
     * 
     * @return true if the new curve passes through all lines, false otherwise
     */
    public boolean fit(List<HorizontalLine> intersectionLines,
            ControlPoints refined) {
        ControlPoints controlsStore = controls;
        controls = refined;

        double curveFraction = 0.0;
        for (HorizontalLine hLine : intersectionLines) {
            curveFraction = getLastFractionBefore(curveFraction, hLine.getY(),
                    CURVE_SAMPLE_SIZE);
            if (curveFraction > 1.0) {
                return true; // went beyond the curve
            }
            if (curveFraction >= 0.0) {
                PointDouble sampleStartPoint = calcBezierCoordinate(curveFraction);
                PointDouble sampleEndPoint = calcBezierCoordinate(curveFraction
                        + CURVE_SAMPLE_SIZE);
                Line lineSegment = new Line(sampleStartPoint, sampleEndPoint);

                if (Math.abs(lineSegment.getDistanceToHorizontalLine(hLine)) > DELTA) {
                    controls = controlsStore;
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isStartTangentFixed() {
        return isStartTangentFixed;
    }

    /**
     * Set the tangent for the first control point
     * 
     * @param tangent A point on the line through start and the first control
     * point
     */
    public void setStartTangent(PointDouble tangent) {
        if (tangent != null) {
            isStartTangentFixed = true;
            double dx = tangent.x - start.x;
            double dy = tangent.y - start.y;

            double dcdt = start.getDistance(controls.getFirst())
                    / start.getDistance(tangent);

            controls.getFirst().x = start.x + dx * dcdt;
            controls.getFirst().y = start.y + dy * dcdt;
        }
    }

    /**
     * Refines this curve to fit through the intersectionlines. Retains the
     * original curve if refinement is unsuccessful;
     * 
     * @param intersectionLines The lines the curve should pass through
     * @return True if refinment was successful, false otherwise
     */
    public boolean refine(List<HorizontalLine> intersectionLines) {
        return refine(intersectionLines, 1.0);
    }

    /**
     * Refines this curve to fit through the intersectionlines. Retains the
     * original curve if refinement is unsuccessful;
     * 
     * @param intersectionLines The lines the curve should pass through
     * @param initialfactor The factor to start refinement (for initially
     * fitting straight curves that better be curved)
     * @return True if refinment was successful, false otherwise
     */
    public boolean refine(List<HorizontalLine> intersectionLines,
            double initialfactor) {
        for (int i = 0; i < refinements.length; i++) {
            if (refine(intersectionLines, initialfactor, refinements[i])) {
                return true;
            }
        }

        return false;
    }

    /**
     * Try to fit the curve by pulling the control points to a side
     * 
     * @param intersectionLines The lines through which the curve should pass
     * @return True if refinement was successful, false otherwise
     */
    private boolean refine(List<HorizontalLine> intersectionLines,
            double initialfactor, int refinementType) {
        double factor = initialfactor;

        for (int i = 0; i < MAX_REFINE_ITERATIONS; i++) {
            ControlPoints refinedPoints = refine(refinementType, factor);
            if (refinedPoints == null) {
                return false;
            }

            if (fit(intersectionLines, refinedPoints)) {
                return true;
            }
            factor *= REFINEMENT_FACTOR_INCREMENT;
        }

        return false;
    }

    /**
     * Refines the curve by the given type
     * 
     * @param refinementType The specific refinement to apply
     * @param factor The factor to apply to the refinement
     * 
     * @return The control points for a refined version of this curve, or null
     * if the refinement type could not be done
     */
    private ControlPoints refine(int refinementType, double factor) {
        switch (refinementType) {
        case LEFT:
            return controls.translateLeft(factor);
        case RIGHT:
            return controls.translateRight(factor);
        case CROSS_L_R:
            return controls.translateCrossLR(factor);
        case CROSS_R_L:
            return controls.translateCrossRL(factor);
        default:
            return null;
        }
    }

    /**
     * Straighten the curve until it fits through the intersection lines.
     * 
     * @param intersectionLines The lines through which the curve should pass
     */
    public void straightenCurve(List<HorizontalLine> intersectionLines) {
        double straightenFactor = STRAIGHTEN_FACTOR_INCREMENT;

        ControlPoints straightCP;
        /*
         * This terminates because a straight line will definitely fit;
         * otherwise, the start- and endpoints of the curve wouldn't exist.
         */
        do {
            straightCP = controls.straighten(straightenFactor);
            straightenFactor += STRAIGHTEN_FACTOR_INCREMENT;
        } while (!fit(intersectionLines, straightCP));

        controls = straightCP;
    }

    public boolean invert() {
        PointDouble temp = start;
        start = end;
        end = temp;
        controls.invert();

        return true;
    }
}
