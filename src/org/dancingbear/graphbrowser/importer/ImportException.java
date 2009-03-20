/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.importer;

/**
 * Import Exception
 * 
 * @author Nickolas Heirbaut
 * 
 */
public class ImportException extends Exception {

    private static final long serialVersionUID = 3085859743509436782L;

    public ImportException(Exception exception) {
        super(exception);
    }
}
