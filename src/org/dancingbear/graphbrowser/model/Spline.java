/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.model;

import java.util.ArrayList;

public class Spline extends ArrayList<CubicCurve> {

    private static final long serialVersionUID = 3391308396954303918L;

    /**
     * @return Start position of the spline
     */
    public Position getStartPosition() {
        if (size() > 0) {
            CubicCurve firstCurve = get(0);
            return firstCurve.getSourcePosition();
        }
        return null;
    }

    /**
     * 
     * @return End position of the spline
     */
    public Position getEndPosition() {
        if (size() > 0) {
            CubicCurve lastCurve = get(size() - 1);
            return lastCurve.getTargetPosition();
        }
        return null;
    }

    public boolean isStraightCurve() {
        for (CubicCurve curve : this) {
            if (!curve.isStraightLine()) {
                return false;
            }
        }

        return true;
    }

}
