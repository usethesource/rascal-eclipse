/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.exporter;

/**
 * Exception for exporter
 * 
 * @author Nickolas Heirbaut
 * 
 */
public class ExportException extends Exception {

    private static final long serialVersionUID = -7677951068211546812L;

    public ExportException(Exception exception) {
        super(exception);
    }
}
