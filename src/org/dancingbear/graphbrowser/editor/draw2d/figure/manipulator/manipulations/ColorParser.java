/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.manipulations;

import java.util.Hashtable;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * Color parser
 * 
 * @author Ka-Sing Chou
 * @date 16-03-2009
 */
public class ColorParser {
    public final static Color DEFAULT_COLOR = ColorConstants.white;
    private static Hashtable<String, Color> colors = new Hashtable<String, Color>();

    // fill the colors table at initialization
    static {
        addColor("red", ColorConstants.red);
        addColor("gray", ColorConstants.gray);
        addColor("green", ColorConstants.green);
        addColor("blue", ColorConstants.blue);
        addColor("black", ColorConstants.black);
        addColor("cyan", ColorConstants.cyan);
        addColor("darkBlue", ColorConstants.darkBlue);
        addColor("darkGray", ColorConstants.darkGray);
        addColor("lightBlue", ColorConstants.lightBlue);
        addColor("lightGray", ColorConstants.lightGray);
        addColor("orange", ColorConstants.orange);
        addColor("yellow", ColorConstants.yellow);
        addColor("pink", new Color(Display.getCurrent(), 255, 0, 255));
        addColor("purple", new Color(Display.getCurrent(), 128, 0, 128));
    }

    /**
     * Gets the colour from the inputname.
     * 
     * @param name The color that could be any type (Hexadecimal, names)
     * @return The color object
     */
    public static Color getColor(String name) {
        Color color = DEFAULT_COLOR;
        // Hexadecimal
        if (name.contains("#")) {
            color = getRGBFromHex(name);
        } else if (colors.containsKey(name)) {
            color = colors.get(name);
        }

        return color;
    }

    /**
     * Converts hexadecimal colors to RGB colors
     * 
     * @param hexColor The color in hexadecimal value
     * @return The color in RGB value
     */
    private static Color getRGBFromHex(String hexColor) {
        Color color = DEFAULT_COLOR;
        if (hexColor.length() == 7) {
            try {
                int red = Integer.valueOf(hexColor.substring(1, 3), 16)
                        .intValue();
                int green = Integer.valueOf(hexColor.substring(3, 5), 16)
                        .intValue();
                int blue = Integer.valueOf(hexColor.substring(5, 7), 16)
                        .intValue();
                if (red <= 255 && green <= 255 && blue <= 255) {

                    color = new Color(Display.getCurrent(), red, green, blue);
                }
            } catch (NumberFormatException e) {
            }
        }
        return color;
    }

    /**
     * Adds the constant colors in Hashtable
     * 
     * @param name The name of the color that is used as key
     * @param color The value of the color
     */
    private static void addColor(String name, Color color) {
        colors.put(name, color);
    }
}