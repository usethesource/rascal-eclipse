/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.exporter;

import java.io.IOException;
import java.io.Writer;
import org.dancingbear.graphbrowser.model.IModelGraph;

/**
 * Interface that any export class needs to implement
 */
public abstract interface IGraphExport {

    /**
     * Exports a {@link IModelGraph} to a file
     * 
     * @param path The full path to where the output file should be exported
     * (incl. filename and file extension)
     * @param modelGraph The datamodel {@link IModelGraph} that contains all
     * nodes, edges, subgraphs and corresponding properties
     * 
     * @throws IOException Exception is thrown if the exporter is unable to
     * export the file to the filesystem
     * @throws ExportException Exception is thrown if the export fails
     */
    void exportFromPath(String path, IModelGraph modelGraph)
            throws IOException, ExportException;

    /**
     * Exports a {@link IModelGraph} to a file
     * 
     * @param writer The writer that is used to write the output file
     * @param modelGraph The datamodel {@link IModelGraph} that contains all
     * nodes, edges, subgraphs and corresponding properties
     * 
     * @throws IOException Exception is thrown if the exporter is unable to
     * export the file to the filesystem
     * @throws ExportException Exception is thrown if the export fails
     */
    void exportFromWriter(Writer writer, IModelGraph modelGraph)
            throws IOException, ExportException;
}
