/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.manipulations;

import java.util.Hashtable;
import java.util.Set;

import org.dancingbear.graphbrowser.editor.draw2d.figure.shapes.CircleShape;
import org.dancingbear.graphbrowser.editor.draw2d.figure.shapes.DefaultShape;
import org.dancingbear.graphbrowser.editor.draw2d.figure.shapes.DiamondShape;
import org.dancingbear.graphbrowser.editor.draw2d.figure.shapes.HeptagonShape;
import org.dancingbear.graphbrowser.editor.draw2d.figure.shapes.HexagonShape;
import org.dancingbear.graphbrowser.editor.draw2d.figure.shapes.HouseShape;
import org.dancingbear.graphbrowser.editor.draw2d.figure.shapes.InvTrapeziumShape;
import org.dancingbear.graphbrowser.editor.draw2d.figure.shapes.InvTriangleShape;
import org.dancingbear.graphbrowser.editor.draw2d.figure.shapes.OctagonShape;
import org.dancingbear.graphbrowser.editor.draw2d.figure.shapes.ParallelogramShape;
import org.dancingbear.graphbrowser.editor.draw2d.figure.shapes.RectangleShape;
import org.dancingbear.graphbrowser.editor.draw2d.figure.shapes.TrapeziumShape;
import org.dancingbear.graphbrowser.editor.draw2d.figure.shapes.TriangleShape;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Shape;

/**
 * Construct and maintain shape instances, used to improve performance
 * 
 * @author Jeroen van Schagen
 * @author Ka-Sing Chou
 * @date 16-03-2009
 */
public final class ShapeParser {

    private Hashtable<ShapeConstants, Class<? extends Shape>> shapes;
    private static ShapeParser instance = null;

    /**
     * Singleton constructor
     */
    private ShapeParser() {
        shapes = new Hashtable<ShapeConstants, Class<? extends Shape>>();
        shapes.put(ShapeConstants.BOX, RectangleShape.class);
        shapes.put(ShapeConstants.RECTANGLE, RectangleShape.class);
        shapes.put(ShapeConstants.RECT, RectangleShape.class);
        shapes.put(ShapeConstants.ELLIPSE, Ellipse.class);
        shapes.put(ShapeConstants.TRIANGLE, TriangleShape.class);
        shapes.put(ShapeConstants.DIAMOND, DiamondShape.class);
        shapes.put(ShapeConstants.TRAPEZIUM, TrapeziumShape.class);
        shapes.put(ShapeConstants.PLAINTEXT, DefaultShape.class);
        shapes.put(ShapeConstants.NONE, DefaultShape.class);
        shapes.put(ShapeConstants.HEXAGON, HexagonShape.class);
        shapes.put(ShapeConstants.POLYGON, HeptagonShape.class);
        shapes.put(ShapeConstants.HOUSE, HouseShape.class);
        shapes.put(ShapeConstants.OCTAGON, OctagonShape.class);
        shapes.put(ShapeConstants.PARALLELOGRAM, ParallelogramShape.class);
        shapes.put(ShapeConstants.INVTRAPEZIUM, InvTrapeziumShape.class);
        shapes.put(ShapeConstants.INVTRIANGLE, InvTriangleShape.class);
        shapes.put(ShapeConstants.CIRCLE, CircleShape.class);
    }

    /**
     * Get the shapes that can be parsed
     * 
     * @return list of possible shapes
     */
    public Set<?> getPossibleShapes() {
        return shapes.keySet();
    }

    /**
     * Retrieve default instance.
     * 
     * @return instance
     */
    public static ShapeParser getDefault() {
        if (instance == null) {
            instance = new ShapeParser();
        }

        return instance;
    }

    /**
     * Construct a new figure object, based on the referenced shape.
     * 
     * @param shape Shape
     * @return Figure
     * @throws IllegalAccessException Access to figure class denied
     * @throws InstantiationException Figure cannot be instantiated
     */
    public Shape parseShape(ShapeConstants shape)
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException {
        if (shapes.containsKey(shape)) {
            return shapes.get(shape).newInstance();
        }
        return null;
    }

    /**
     * Construct a new figure object, based on the referenced shape name.
     * 
     * @param value Shape name
     * @return Figure
     * @throws IllegalAccessException Access to figure class denied
     * @throws InstantiationException Figure cannot be instantiated
     * @throws IllegalArgumentException Invalid shape name
     */
    public Shape parseShape(String value) throws InstantiationException,
            IllegalAccessException, IllegalArgumentException {
        return parseShape(ShapeConstants.valueOf(value.toUpperCase()));
    }

}