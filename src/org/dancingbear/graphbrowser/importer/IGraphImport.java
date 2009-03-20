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

/**
 * Interface that any import class needs to implement
 */
public abstract interface IGraphImport {

    /**
     * Imports a dot file and converts it to an {@link IModelGraph}
     * 
     * @param path Full path of the source file
     * @param modelGraph The datamodel {@link IModelGraph} where all nodes,
     * edges, subgraphs and corresponding properties from the DOT file will be
     * added to.
     * @throws ImportException Exception is thrown if the import file has an
     * invalid markup
     * @trhows IOException Exception is thrown if the importer is unable to
     * import the file
     */
    void importFromPath(String path, IModelGraph modelGraph)
            throws IOException, ImportException;

    /**
     * Imports a dot file and converts it to an {@link IModelGraph}
     * 
     * @param reader The reader used to read the DOT file
     * @param modelGraph The datamodel {@link IModelGraph} where all nodes,
     * edges, subgraphs and corresponding properties from the DOT file will be
     * added to.
     * @throws ImportException Exception is thrown if the import file has an
     * invalid markup
     * @trhows IOException Exception is thrown if the importer is unable to
     * import the file
     */
    void importFromReader(Reader reader, IModelGraph modelGraph)
            throws IOException, ImportException;
}