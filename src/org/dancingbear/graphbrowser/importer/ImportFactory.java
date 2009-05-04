/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.importer;

import java.io.IOException;
import java.io.Reader;

import org.dancingbear.graphbrowser.model.IModelGraph;
import org.dancingbear.graphbrowser.model.ModelGraphRegister;

/**
 * Imports files that contains graphs information and converts them to an
 * {@link IModelGraph}
 * 
 * @author Nickolas Heirbaut
 * @author Nico Schoenmaker
 * @date 04-03-2009
 */
public final class ImportFactory {

    private ImportFactory() {
    }

    /**
     * Imports a dot file and converts it to an {@link IModelGraph}
     * 
     * @param path Full path of the DOT file
     * 
     * @return {@link IModelGraph} The datamodel which contains all nodes,
     * edges, subgraphs and corresponding properties from the DOT file.
     * @throws ImportException Exception is thrown if the import file has an
     * invalid markup
     * @throws IOException Exception is thrown if the importer is unable to
     * import the file from the filesystem
     */
    public static IModelGraph importFromDot(String path)
            throws ImportException, IOException {
        IModelGraph graph = ModelGraphRegister.getInstance()
                .forceNewGraph(path);
        DotImport dotImporter = new DotImport();
        dotImporter.importFromPath(path, graph);
        return graph;
    }

    /**
     * Imports a dot file and converts it to an {@link IModelGraph}
     * 
     * @param name The unique ID or name of the graph
     * @param reader The reader used to read the DOT file
     * @return {@link IModelGraph} The datamodel which contains all nodes,
     * edges, subgraphs and corresponding properties from the DOT file.
     * 
     * @throws ImportException Exception is thrown if the import file has an
     * invalid markup
     * @throws IOException Exception is thrown if the importer is unable to
     * import the file from the filesystem
     */
    public static IModelGraph importFromDot(String name, Reader reader)
            throws ImportException, IOException {
        IModelGraph graph = ModelGraphRegister.getInstance()
                .forceNewGraph(name);
        DotImport dotImporter = new DotImport();
        dotImporter.importFromReader(reader, graph);
        return graph;
    }
}