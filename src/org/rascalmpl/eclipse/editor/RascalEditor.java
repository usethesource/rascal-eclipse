package org.rascalmpl.eclipse.editor;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

public class RascalEditor extends UniversalEditor {
	
	
	
    public RascalEditor() {
        super();
        // used for the expression breakpoints management
        setEditorContextMenuId("rascal.editor.contextMenu");
    }
    
    protected void createActions() {
    	super.createActions();
    	
    	IAction action = new Action(){/* Nothing. */};
    	action.setActionDefinitionId(ITextEditorActionDefinitionIds.SHOW_INFORMATION);
        
        setAction(ITextEditorActionConstants.SHOW_INFORMATION, action);
    }
    
    public StyledText getTextWidget() {
	    return this.getSourceViewer().getTextWidget();
	    }
    
    
}
