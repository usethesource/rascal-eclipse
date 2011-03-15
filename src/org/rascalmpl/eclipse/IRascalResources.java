package org.rascalmpl.eclipse;

public interface IRascalResources {

	public static final String RASCAL_DEFAULT_IMAGE = "rascal_default_image";
	public static final String RASCAL_DEFAULT_OUTLINE_ITEM = "rascal_default_outline_item";
	public static final String RASCAL_FILE = "rascal_file";
	public static final String RASCAL_FILE_WARNING = "rascal_file_warning";
	public static final String RASCAL_FILE_ERROR = "rascal_file_error";

	public static final String ID_RASCAL_DEBUG_MODEL = "rascal.debugModel";
	public static final String ID_RASCAL_NATURE = "rascal.nature";

	public static final String RASCAL_SRC = "src";
	public static final String RASCAL_EXT = "rsc";

	public static final String ID_RASCAL_MARKER_TYPE_TEST_RESULTS = "rascal.markerType.testResult";
	public static final String ID_RASCAL_MARKER_TYPE_FOCUS = "rascal.focusMarker";
	
	/**
	 * Name of the string substitution variable that resolves to the
	 * location of a local Rascal executable (value <code>rascalExecutable</code>).
	 */
	public static final String VARIABLE_RASCAL_EXECUTABLE = "rascalExecutable";
	
	/**
	 * Launch configuration attribute key. Value is a path to a rascal
	 * program. The path is a string representing a full path
	 * to a Rascal program in the workspace. 
	 */
	public static final String ATTR_RASCAL_PROGRAM = ID_RASCAL_DEBUG_MODEL + ".ATTR_RASCAL_PROGRAM";

	/**
	 * Launch configuration attribute key. Value is an Eclipse project (instance of IProject).
	 * The module path is set relatively to this project and its referenced projects
	 * in the workspace. 
	 */
	public static final String ATTR_RASCAL_PROJECT = ID_RASCAL_DEBUG_MODEL + ".ATTR_RASCAL_PROJECT";
	
	/**
	 * Identifier for the RASCAL launch configuration type
	 * (value <code>rascal.launchType</code>)
	 */
	public static final String ID_RASCAL_LAUNCH_CONFIGURATION_TYPE = "rascal.launchType";
	public static final String ID_RASCAL_EDITOR = "rascal.editor";

	public static final String RASCAL_EDITOR_MESSAGES = "rascal.editor.messages";

}
