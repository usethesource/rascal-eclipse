/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator;

import java.util.Hashtable;

import org.dancingbear.graphbrowser.model.IPropertyContainer;
import org.eclipse.draw2d.IFigure;

/**
 * Abstract figure manipulations
 * 
 * @author Jeroen van Schagen
 * @date 06-03-2009
 */
public abstract class AbstractFigureManipulator {

    private Hashtable<String, IFigureManipulation> manipulations = new Hashtable<String, IFigureManipulation>();

    /**
     * Add figure manipulation
     * 
     * @param name Manipulation identifier
     * @param manipulation Manipulation instance
     * @return success
     */
    public boolean addManipulator(String name, IFigureManipulation manipulation) {
        manipulations.put(name, manipulation);
        return true;
    }

    /**
     * Add figure manipulations
     * 
     * @param manipulations Manipulations
     * @return success
     */
    public boolean addManipulations(
            Hashtable<String, IFigureManipulation> manipulations) {
        this.manipulations.putAll(manipulations);
        return true;
    }

    /**
     * Manipulate figure
     * 
     * @param model Model with actual data
     * @param figure Figure to be manipulated
     * @return success
     */
    public boolean manipulateFigure(IPropertyContainer model, IFigure figure) {
        for (String key : model.getPropertyKeys()) {
            IFigureManipulation manipulation = manipulations.get(key);
            if (manipulation != null) {
                manipulation.manipulateFigure(figure, model.getProperty(key));
            }
        }

        return true;
    }

    /**
     * Set figure manipulations
     * 
     * @param manipulations Manipulations
     * @return success
     */
    public boolean setManipulations(
            Hashtable<String, IFigureManipulation> manipulations) {
        this.manipulations = manipulations;
        return true;
    }

}