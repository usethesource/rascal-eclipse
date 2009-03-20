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
 * Exports an {@link IModelGraph} to a specific output format
 * 
 * @author Nickolas Heirbaut
 * @author Nico Schoenmaker
 * @date 04-03-2009
 */
public final class ExportFactory {

    private ExportFactory() {
    }

    /**
     * Exports the {@link IModelGraph} to a DOT file
     * 
     * @param filename The filename
     * @throws IOException Exception is thrown if the exporter is unable to
     * export the file to the filesystem
     * @throws ExportException Exception is thrown if the export fails
     */
    public static void exportToDot(String filename, IModelGraph graph)
            throws IOException, ExportException {
        DotExport dotExporter = new DotExport();
        dotExporter.exportFromPath(filename, graph);
    }

    /**
     * Exports the {@link IModelGraph} to a DOT file
     * 
     * @param writer The writer used to write the DOT file
     * @throws IOException Exception is thrown if the exporter is unable to
     * export the file to the filesystem
     * @throws ExportException Exception is thrown if the export fails
     */
    public static void exportToDot(Writer writer, IModelGraph graph)
            throws IOException, ExportException {
        DotExport dotExporter = new DotExport();
        dotExporter.exportFromWriter(writer, graph);
    }
}
