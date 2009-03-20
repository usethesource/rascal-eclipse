/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.figure;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * Utility class for gradient colors.
 * 
 * @author Jeroen van Schagen
 * @date 03-09-2009
 */
public abstract class GradientColors {

    /**
     * Default contrast level.
     */
    public static final double DEFAULT_CONTRAST = 0.4;

    /**
     * Calculate gradient color, using default contrast.
     * 
     * @param bg Background color
     * @return color
     */
    public static RGB getGradientColor(Color bg) {
        return getGradientColor(bg, DEFAULT_CONTRAST);
    }

    /**
     * Calculate gradient color.
     * 
     * @param contrast Contrast on color
     * @param bg Background color
     * @return color
     */
    public static RGB getGradientColor(Color bg, double contrast) {
        int blue = bg.getBlue();
        blue = (int) (blue - (blue * contrast));
        blue = blue > 0 ? blue : 0;

        int red = bg.getRed();
        red = (int) (red - (red * contrast));
        red = red > 0 ? red : 0;

        int green = bg.getGreen();
        green = (int) (green - (green * contrast));
        green = green > 0 ? green : 0;

        return new RGB(red, green, blue);
    }

}