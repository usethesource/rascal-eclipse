/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator;

import java.util.Hashtable;

/**
 * Constructs and stores figure manipulation instances.
 * 
 * @author Jeroen van Schagen
 * @date 06-03-2009
 */
public final class FigureManipulatorFactory {

    private static FigureManipulatorFactory instance = null;
    private Hashtable<String, AbstractFigureManipulator> manipulators;

    /**
     * Singleton constructor
     */
    private FigureManipulatorFactory() {
        manipulators = new Hashtable<String, AbstractFigureManipulator>();
        manipulators.put("node", new NodeFigureManipulator());
        manipulators.put("edge", new EdgeFigureManipulator());
        manipulators.put("graph", new GraphFigureManipulator());
        manipulators.put("subgraph", new SubgraphFigureManipulator());
    }

    /**
     * Retrieve factory instance.
     * 
     * @return instance
     */
    public static FigureManipulatorFactory getDefault() {
        if (instance == null) {
            instance = new FigureManipulatorFactory();
        }

        return instance;
    }

    /**
     * Retrieve figure manipulator
     * 
     * @return manipulator
     */
    public AbstractFigureManipulator getFigureManipulator(String id) {
        return manipulators.get(id);
    }

}