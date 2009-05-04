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
public class Spline {

    private List<CubicBezierCurve> curves;

    /**
     * Create a new empty spline
     */
    public Spline() {
        curves = new ArrayList<CubicBezierCurve>();
    }

    /**
     * Create a new spline
     * 
     * @param curveList The curves that make up this spline.
     */
    public Spline(List<CubicBezierCurve> curveList) {
        this();
        curves.addAll(curveList);
    }

    /**
     * Create a new spline
     * 
     * @param curve The initial curve of the spline
     */
    public Spline(CubicBezierCurve curve) {
        this();
        curves.add(curve);
    }

    /**
     * 
     * @param curve The curve to add to the spline.
     */
    public void addCurve(CubicBezierCurve curve) {
        curves.add(curve);
    }

    /**
     * Add all curves in the spline parameter to this spline.
     * 
     * @param spline The spline who's curves will be added to this spline.
     */
    public void addSpline(Spline spline) {
        curves.addAll(spline.getCurves());
    }

    /**
     * 
     * @param index The index of the curve we want
     * @return The Curve with the specified index
     */
    public CubicBezierCurve getCurve(int index) {
        return curves.get(index);
    }

    /**
     * 
     * @return The curves that make up this spline.
     */
    public List<CubicBezierCurve> getCurves() {
        return curves;
    }

    /**
     * Retrieve the last control point before the end point of this spline
     * 
     * @return The last control point or null if the spline is not set up yet
     */
    public PointDouble getSecondToLastPoint() {
        if (curves.isEmpty()) {
            return null;
        }

        return curves.get(curves.size() - 1).getSecondControlPoint();
    }

    /**
     * 
     * @return The start point of the spline
     */
    public PointDouble getStartPoint() {
        if (curves.isEmpty()) {
            return null;
        }
        return curves.get(0).getStartPoint();// ControlPoints().get(0);
    }

    /**
     * 
     * @return The end point of the spline
     */
    public PointDouble getEndPoint() {
        if (curves.isEmpty()) {
            return null;
        }
        return curves.get(curves.size() - 1).getEndPoint();// getControlPoints().get(curves.size()
        // - 1);
    }

    /**
     * Retrieves whether a tangent has been set on the beginning of this spline
     * (Usually because it has to connect smoothly to a previous spline)
     * 
     * @return True if there is a tangent set, false otherwise.
     */
    public boolean isStartTangentFixed() {
        return curves.get(0).isStartTangentFixed();
    }

    /**
     * 
     * @return The number of curves in this spline
     */
    public int numOfCurves() {
        return curves.size();
    }

    /**
     * 
     * @return Whether the invert has succeeded
     */
    public boolean invert() {
        List<CubicBezierCurve> curvesInverted = new ArrayList<CubicBezierCurve>();
        for (int index = curves.size() - 1; index >= 0; index--) {
            CubicBezierCurve curve = curves.get(index);
            curve.invert();
            curvesInverted.add(curve);
        }

        curves = curvesInverted;

        return (curves != null);
    }

    /**
     * @return cloned spline
     */
    public Spline clone() {
        Spline clonedSpline = new Spline();

        for (CubicBezierCurve curve : getCurves()) {
            clonedSpline.addCurve(curve.clone());
        }

        return clonedSpline;
    }

}
