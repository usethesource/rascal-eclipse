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
import java.util.List;

public class ComputeSplines extends GraphVisitor {

    final double DELTA = 1.0e-6;
    private final static double CURVE_SAMPLE_SIZE = 0.05;
    private static final double INITIAL_REFINE_FACTOR = 3.0;

    final double POINT_PADDING = 0;

    private final ArrayList<ArrayList<Box>> allBoxes = new ArrayList<ArrayList<Box>>();
    List<Spline> splines = new ArrayList<Spline>();

    protected Spline getSpline(List<PointDouble> bendpoints,
            List<HorizontalLine> intersectionLines) {
        if (bendpoints.size() == 2) {
            return getStraightSpline(bendpoints);
        }

        return getCurvedSpline(getStraightSpline(bendpoints).getCurve(0),
                bendpoints, intersectionLines);
    }

    protected Spline getStraightSpline(List<PointDouble> bendpoints) {
        PointDouble start = bendpoints.get(0);
        PointDouble end = bendpoints.get(bendpoints.size() - 1);
        double endScale = 1.0 / 3.0;
        double startScale = 1.0 - endScale;
        PointDouble control1 = start.scale(startScale).add(end.scale(endScale));
        PointDouble control2 = end.scale(startScale).add(start.scale(endScale));

        return new Spline(new CubicBezierCurve(start, control1, control2, end));
    }

    private Spline getCurvedSpline(CubicBezierCurve base,
            List<PointDouble> bendpoints, List<HorizontalLine> intersectionLines) {

        if (bendpoints.size() > 2) {
            if (base.refine(intersectionLines)) {
                return getSimpleSpline(base);
            }

            return splitCurve(base, bendpoints, intersectionLines);
        }

        return getSimpleSpline(base);
    }

    private Spline splitCurve(CubicBezierCurve base,
            List<PointDouble> bendpoints, List<HorizontalLine> intersectionLines) {
        int splitPoint = base.getBestSplitPoint(bendpoints);

        List<PointDouble> sub1_bendpoints = bendpoints.subList(0,
                splitPoint + 1);
        List<PointDouble> sub2_bendpoints = bendpoints.subList(splitPoint,
                bendpoints.size());

        Spline top = getCurvedSpline(getStraightSpline(sub1_bendpoints)
                .getCurve(0), sub1_bendpoints, intersectionLines);
        PointDouble tangent = top.getSecondToLastPoint().getMirroredPoint(
                top.getEndPoint());

        CubicBezierCurve bottom = getStraightSpline(sub2_bendpoints)
                .getCurve(0);
        bottom.setStartTangent(tangent);

        top.addSpline(getCurvedSpline(bottom, sub2_bendpoints,
                intersectionLines));

        return top;
    }

    /**
     * Calculate the intersections between boxes.
     * 
     * @param boxes A list of boxes
     * @return Intersection lines of boxes
     */
    protected List<HorizontalLine> computeIntersectionLinesArray(
            ArrayList<Box> boxes) {
        List<HorizontalLine> intersectionLines = new ArrayList<HorizontalLine>();

        for (int boxIterator = 0; boxIterator < boxes.size() - 1; boxIterator++) {
            Box boxAtTop = boxes.get(boxIterator);
            Box boxAtBottom = boxes.get(boxIterator + 1);

            PointDouble left = new PointDouble(Math.max(boxAtTop.getLeft(),
                    boxAtBottom.getLeft()), boxAtBottom.getTop());
            double right = Math
                    .min(boxAtTop.getRight(), boxAtBottom.getRight());

            intersectionLines.add(new HorizontalLine(left, right));
        }

        return intersectionLines;
    }

    /**
     * 
     * @param boxes A list of boxes
     * @param spline A spline through these boxes
     * @return The smallest boxes that still contain this spline
     */
    protected ArrayList<Box> computeMinimumSizeBoxes(ArrayList<Box> boxes,
            Spline spline) {
        ArrayList<Box> optimalBoxes = new ArrayList<Box>();

        double curveFraction = 0;
        int curveIndex = 0;
        CubicBezierCurve currentCurve = spline.getCurve(curveIndex);
        for (Box box : boxes) {
            double optimalLeftX = box.getRight();
            double optimalRightX = box.getLeft();

            do {
                PointDouble splineCoordinate = currentCurve
                        .calcBezierCoordinate(curveFraction);

                boolean coordinateInBox = (splineCoordinate.y >= box.getTop())
                        && (splineCoordinate.y <= box.getBottom());
                boolean coordinateBelowBox = splineCoordinate.y > box
                        .getBottom();

                if (coordinateInBox) {
                    if (splineCoordinate.x < optimalLeftX) {
                        optimalLeftX = splineCoordinate.x;
                    }
                    if (splineCoordinate.x > optimalRightX) {
                        optimalRightX = splineCoordinate.x;
                    }
                } else if (coordinateBelowBox) {
                    // This box has been analysed
                    break;
                }
                curveFraction += CURVE_SAMPLE_SIZE / 10;
                if (curveFraction > 1) {
                    curveIndex++;

                    if (curveIndex >= spline.numOfCurves()) {
                        // All splines have been analysed
                        break;
                    }

                    currentCurve = spline.getCurve(curveIndex);
                    curveFraction = 0;
                }
            } while (true);

            double boxWidth = optimalRightX - optimalLeftX;
            double boxHeight = box.getBottom() - box.getTop();
            Box optimalBox = new Box(optimalLeftX, box.getTop(), boxWidth,
                    boxHeight);
            optimalBoxes.add(optimalBox);
        }
        return optimalBoxes;
    }

    /**
     * Retrieves the horizontal line in the list with the greatest distance to
     * the direct line.
     * 
     * @param directLine The straight line from start to end
     * @param lines The horizontal lines
     * 
     * @return the line horizontally farthest from direct line, null if all
     * intersect.
     */
    private HorizontalLine getLineWithGreatestDistanceToDirect(Line directLine,
            List<HorizontalLine> lines) {
        double maxDistance = 0.0;
        HorizontalLine maxLine = null;

        PointDouble linePos = null;
        for (HorizontalLine line : lines) {
            linePos = directLine.getPositionAt(line.getY());
            if (linePos != null
                    && !(line.getStart().x < linePos.x && line.getEnd().x > linePos.x)) {
                double distance = Math.min(
                        linePos.getDistance(line.getStart()), linePos
                                .getDistance(line.getEnd()));

                if (distance > maxDistance) {
                    maxDistance = distance;
                    maxLine = line;
                }
            }
        }

        return maxLine;
    }

    /**
     * Retrieve the point on the given horizontal line closest to the direct
     * line
     * 
     * @param directLine The line from the start point to the end point
     * @param intersectionLines The intersection line with the greatest distance
     * to the direct line
     * 
     * @return The point on diffLine closest to directLine
     */
    private PointDouble getBendPoint(Line directLine,
            List<HorizontalLine> intersectionLines) {
        HorizontalLine intersectionLine = getLineWithGreatestDistanceToDirect(
                directLine, intersectionLines);
        if (intersectionLine != null) {
            double distance = directLine
                    .getDistanceToHorizontalLine(intersectionLine);

            return (distance > 0) ? intersectionLine.getStart()
                    : intersectionLine.getEnd();
        }

        return null;
    }

    /**
     * @param intersectionLines The lines through which the spline should go
     * @param startPoint The start point of the spline
     * @param endPoint The end point of the spline
     * @return A list of bendpoints which should guide the spline
     */
    protected BendPointList computeBendPointArray(
            List<HorizontalLine> intersectionLines, PointDouble startPoint,
            PointDouble endPoint) {

        PointDouble bendpoint = getBendPoint(new Line(startPoint, endPoint),
                intersectionLines);

        if (bendpoint == null) { // direct line is good enough
            return getStraightLineBendPoints(startPoint, endPoint);
        }

        // divide the section in two at the bendpoint and recurse
        BendPointList bendpoints = computeBendPointArray(intersectionLines,
                startPoint, bendpoint);
        bendpoints.addAll(computeBendPointArray(intersectionLines, bendpoint,
                endPoint));

        return bendpoints;
    }

    /**
     * Builds a list of bendpoints for a straight line
     * 
     * @param startPoint The start point of the spline
     * @param endPoint The end point of the spline
     * 
     * @return A list of bendpoints which should guide the spline
     */
    private BendPointList getStraightLineBendPoints(PointDouble startPoint,
            PointDouble endPoint) {
        BendPointList bendpointList = new BendPointList();
        bendpointList.add(startPoint);
        bendpointList.add(endPoint);

        return bendpointList;
    }

    /**
     * Basically both a strict set and list of bendpoints.
     * 
     * @author Joppe
     */
    private static class BendPointList extends ArrayList<PointDouble> {
        private static final long serialVersionUID = 3847426191804362945L;

        @Override
        public boolean add(PointDouble element) {
            if (this.contains(element)) {
                return true;
            }
            return super.add(element);
        }

        @Override
        public boolean addAll(Collection<? extends PointDouble> points) {
            for (PointDouble point : points) {
                if (this.contains(point)) {
                    this.remove(point);
                }
            }
            return super.addAll(points);
        }
    }

    /**
     * Compute a spline given a certain edge between two nodes.
     * 
     * @param fullEdge The edge for which a spline has to be computed.
     * @return The computed spline
     */
    protected Spline computeSpline(FullEdge fullEdge) {
        Spline result = new Spline();
        ArrayList<Box> boxes = fullEdge.getBoxes();

        // (1) Compute intersections of boxes
        List<HorizontalLine> intersectionLines = computeIntersectionLinesArray(boxes);

        // (2) Compute bendpoints
        List<PointDouble> bendpoints = computeBendPointArray(intersectionLines,
                fullEdge.getEdgeStartPosition(), fullEdge.getEdgeEndPosition());

        // (3) Calculate control points of the spline.
        result = getSpline(bendpoints, intersectionLines);

        // (4) Calculate minimum size boxes.
        computeMinimumSizeBoxes(boxes, result);

        return result;
    }

    // FIXME inline this!
    private Spline getSimpleSpline(CubicBezierCurve curve) {
        return new Spline(curve);
    }

    /**
     * @param bendpoints The bendpoints of the spline to be made
     * @return The relative distances between the bendpoints
     */
    protected List<Double> getRelativeDistances(List<PointDouble> bendpoints) {
        ArrayList<Double> travelledDistances = new ArrayList<Double>();
        double fullDistance = 0.0;

        for (int i = 0; i < bendpoints.size() - 1; i++) {
            double pointsDistance = bendpoints.get(i).distance(
                    bendpoints.get(i + 1));
            fullDistance += pointsDistance;
            travelledDistances.add(Double.valueOf(fullDistance));
        }

        ArrayList<Double> relativeDistances = new ArrayList<Double>();

        // for first point too
        relativeDistances.add(Double.valueOf(0.0));
        for (Double travelled : travelledDistances) {
            relativeDistances.add(Double.valueOf(travelled.floatValue()
                    / fullDistance));
        }

        return relativeDistances;

    }

    /**
     * The computeSplines-visiting function
     * 
     * @param g the directed graph to visit
     */
    @Override
    public void visit(DirectedGraph g) {
        NodeList nodes = g.getNodes();
        for (Node node : nodes) {
            if (!node.isVirtualNode()) {
                for (Edge edge : node.getOutgoing()) {
                    // if (null != nodes.getNodeById(edge.getTarget().getId()))
                    // {
                    Spline spline = computeSpline(new FullEdge(edge, g));
                    edge.setSpline(spline);
                    // }
                }
            }
        }
    }
}
