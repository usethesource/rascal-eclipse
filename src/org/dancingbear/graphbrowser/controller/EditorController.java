/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.dancingbear.graphbrowser.controller.action.DotLayoutAction;
import org.dancingbear.graphbrowser.controller.events.SaveEventHandler;
import org.dancingbear.graphbrowser.editor.gef.ui.parts.IEditorEventHandler;
import org.dancingbear.graphbrowser.exporter.ExportException;
import org.dancingbear.graphbrowser.exporter.ExportFactory;
import org.dancingbear.graphbrowser.importer.ImportException;
import org.dancingbear.graphbrowser.importer.ImportFactory;
import org.dancingbear.graphbrowser.layout.DirectedGraphToModelConverter;
import org.dancingbear.graphbrowser.layout.Layout;
import org.dancingbear.graphbrowser.layout.ModelToDirectedGraphConverter;
import org.dancingbear.graphbrowser.layout.model.DirectedGraph;
import org.dancingbear.graphbrowser.model.IModelGraph;
import org.eclipse.jface.action.IAction;

/**
 * Controller for GraphEditor
 * 
 * @author Jeroen van Lieshout
 * @author Menno Middel
 * 
 */
public class EditorController {

	private static final int ANIMATION_TIME = 500;
	private final IGraphEditor editor;
	private Layout lastLayout;

	/**
	 * Creates a new EditorController
	 * 
	 * @param fileName Filename of dot file
	 * @param reader Reader instance
	 * @param editor Editor to display graph in
	 * @throws IOException IOException when file not opened properly
	 * @throws ImportException ImportException when file not opened properly
	 */
	public EditorController(String fileName, InputStreamReader reader,
			IGraphEditor editor) throws IOException, ImportException {
		this.editor = editor;
		IModelGraph graph = loadGraph(fileName, reader);
		editor.setGraph(graph);
		registerContextMenuActions();
		registerEventHandlers();
	}

	/**
	 * Creates a new EditorController
	 * 
	 * @param path Path of file
	 * @param editor Editor to display graph in
	 * @throws IOException IOException when file not opened properly
	 * @throws ImportException ImportException when file not opened properly
	 */
	public EditorController(String path, IGraphEditor editor)
	throws IOException, ImportException {
		this.editor = editor;
		IModelGraph graph;
		graph = loadGraph(path);
		editor.setGraph(graph);
		registerContextMenuActions();

	}

	/**
	 * Creates a new EditorController
	 * 
	 * @param graph Graph to display
	 * @param editor Editor to display graph in
	 */
	public EditorController(IModelGraph graph, IGraphEditor editor) {
		this.editor = editor;
		editor.setGraph(graph);
		registerContextMenuActions();
		registerEventHandlers();
	}

	/**
	 * Retrieve a graph from file system and store in controller instance.
	 * 
	 * @param dotFileName Filename of dotfile
	 * @return graph Graph from the given dotfile
	 * @throws IOException Exception by opening file
	 * @throws ImportException Exception by importing file
	 */
	private static IModelGraph loadGraph(String dotFileName)
	throws IOException, ImportException {
		IModelGraph graph = null;

		graph = ImportFactory.importFromDot(dotFileName);

		return graph;
	}

	/**
	 * Retrieves a graph from reader and store in controller instance.
	 * 
	 * We don't want a propagate the dependency of the Exporter exception, so we
	 * reform it to a normal IOException.
	 * 
	 * @param graphName The name of the given graph
	 * @param dotStream The stream to read the dot file from
	 * @return graph The graph constructed from dot file
	 * @throws IOException
	 * @throws ImportException
	 * @throws IOException Exception when file cannot be opened
	 */
	private static IModelGraph loadGraph(String graphName, Reader dotStream)
	throws ImportException, IOException {
		IModelGraph graph = ImportFactory.importFromDot(graphName, dotStream);
		return graph;
	}

	/**
	 * Apply the layout on the graph in the editor
	 * 
	 */
	public void applyLayout(Layout l) {
		IModelGraph graph = editor.getGraph();
		final ModelToDirectedGraphConverter modelToGraphConv = new ModelToDirectedGraphConverter();
		final DirectedGraphToModelConverter graphToModelConvert = new DirectedGraphToModelConverter();

		// Convert graph to directed graph (= model for the layouts)
		DirectedGraph directedGraph = modelToGraphConv.convertToGraph(graph
				.getName());

		// Apply layout
		l.visit(directedGraph);
		lastLayout = l;

		// Store graph
		graphToModelConvert.convertToModel(directedGraph, graph.getName());
	}

	/**
	 * Save graph to given dot file
	 * 
	 * @param graph Graph to save
	 * @param dotFileName Filename of dotfile to save
	 * @throws ExportException graph could not be exported
	 * @throws IOException file could not be written
	 */
	public void saveDotFile(IModelGraph graph, String dotFileName)
	throws IOException, ExportException {
		ExportFactory.exportToDot(dotFileName, graph);
	}

	private void registerContextMenuActions() {
		IAction relayoutAction = new DotLayoutAction(this);
		editor.addContextMenuActionItem(relayoutAction);
	}

	private void registerEventHandlers() {
		IEditorEventHandler saveEventHandler = new SaveEventHandler(this);
		editor.addEditorEventHandler(saveEventHandler);
	}

	/**
	 * Get editor which this controller handles
	 * 
	 * @return editor Instance of grapheditor
	 */
	public IGraphEditor getEditor() {
		return editor;
	}

	public Layout getLastLayout() {
		return lastLayout;
	}
	
}
