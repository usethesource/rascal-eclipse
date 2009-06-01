package org.meta_environment.rascal.eclipse.editor;

import org.eclipse.imp.editor.UniversalEditor;

public class RascalEditor extends UniversalEditor {
	

    public RascalEditor() {
        super();
        // used for the expression breakpoints management
        setEditorContextMenuId("rascal.editor.contextMenu");
    }

}
