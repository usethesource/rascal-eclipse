/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.commands.exception;

/**
 * Exception for when a file is not in an eclipse workspace
 * 
 * @author Erik Slagter
 * 
 */
public class FileNotInEclipseWorkspaceException extends Exception {

    private static final long serialVersionUID = 2791620809706151140L;

    /**
     * Default constructor
     */
    public FileNotInEclipseWorkspaceException() {
        super();
    }

    /**
     * Constructor with exception message
     * 
     * @param message the message
     */
    public FileNotInEclipseWorkspaceException(String message) {
        super(message);
    }
}