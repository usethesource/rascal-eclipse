/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse;

/*
 * TODO: File needs clean-up and reorganization.
 * TODO: How to avoid duplication of hard-coded IDs from plugin.xml?
 */
public interface IRascalResources {

	public static final String RASCAL_DEFAULT_IMAGE = "rascal_default_image";
	public static final String RASCAL_DEFAULT_OUTLINE_ITEM = "rascal_default_outline_item";
	public static final String RASCAL_FILE = "rascal_file";
	public static final String RASCAL_FILE_WARNING = "rascal_file_warning";
	public static final String RASCAL_FILE_ERROR = "rascal_file_error";
	public static final String AMBIDEXTER = "ambidexter";
	public static final String COPY_TO_CONSOLE = "copy_to_console";

	public static final String ID_RASCAL_ECLIPSE_PLUGIN = "rascal_eclipse";
	
	public static final String ID_RASCAL_DEBUG_MODEL = "rascal.debugModel";

	/**
	 * Source folder name within an Eclipse project.
	 */
	public static final String RASCAL_SRC = "src";

	/**
	 * Rascal source file name extension.
	 */
	public static final String RASCAL_EXT = "rsc";

	/**
	 * Standard library folder name within an Eclipse project.
	 */
	public static final String RASCAL_STD = "rascal";

	public static final String ID_RASCAL_MARKER_TYPE_TEST_RESULTS = "rascal.markerType.testResult";
	public static final String ID_RASCAL_MARKER_TYPE_FOCUS = "rascal.focusMarker";
	
	public static final String ID_RASCAL_MARKER = "rascal_eclipse.rascal_markers";
	public static final String ID_RASCAL_NATURE = "rascal_eclipse.rascal_nature";
	public static final String ID_RASCAL_BUILDER = "rascal_eclipse.rascal_builder";	

	public static final String ID_TERM_MARKER = "rascal_eclipse.term_markers";
	public static final String ID_TERM_NATURE = "rascal_eclipse.term_nature";
	public static final String ID_TERM_BUILDER = "rascal_eclipse.term_builder";
	
	/**
	 * Name of the string substitution variable that resolves to the location of
	 * a local Rascal executable (value <code>rascalExecutable</code>).
	 */
	public static final String VARIABLE_RASCAL_EXECUTABLE = "rascalExecutable";

	/**
	 * Launch configuration attribute key. Value is a path to a rascal program.
	 * The path is a string representing a full path to a Rascal program in the
	 * workspace.
	 */
	public static final String ATTR_RASCAL_PROGRAM = ID_RASCAL_DEBUG_MODEL
			+ ".ATTR_RASCAL_PROGRAM";

	/**
	 * Launch configuration attribute key. Value is an Eclipse project (instance
	 * of IProject). The module path is set relatively to this project and its
	 * referenced projects in the workspace.
	 */
	public static final String ATTR_RASCAL_PROJECT = ID_RASCAL_DEBUG_MODEL
			+ ".ATTR_RASCAL_PROJECT";

	/**
	 * Identifier for the RASCAL launch configuration type (value
	 * <code>rascal.launchType</code>)
	 */
	public static final String ID_RASCAL_LAUNCH_CONFIGURATION_TYPE = "rascal.launchType";

	public static final String RASCAL_EDITOR_MESSAGES = "rascal.editor.messages";

	public static final String LAUNCHTYPE = "rascal.launchType";
	public static final String STD_LIB = "std";
	public static final String ECLIPSE_LIB = "eclipse";

	public static final String ID_RASCAL_NEW_PROJECT_WIZARD = "rascal_eclipse.projectwizard";
	public static final String ID_RASCAL_NEW_FILE_WIZARD = "rascal_eclipse.wizards.NewRascalFile";

	public static final String ID_AMBIDEXTER_REPORT_VIEW_PART = "rascal_eclipse.ambidexter.report";
	
	public static final String ID_RASCAL_OUTPUT_VIEW_PART = "rascal_eclipse.outputview";
	public static final String ID_RASCAL_FIGURE_VIEW_PART = "rascal_eclipse.Figure.viewer";
	public static final String ID_RASCAL_TUTOR_VIEW_PART = "rascal_eclipse.tutorBrowser";
	
}
