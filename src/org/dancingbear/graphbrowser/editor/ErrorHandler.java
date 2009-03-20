package org.dancingbear.graphbrowser.editor;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Error handler for editor
 * 
 * @author Alex Hartog
 * 
 */
public class ErrorHandler {

    /**
     * Display error dialog with exception message
     * 
     * @param e The exception
     * @param viewer The viewer where to display error message from
     */
    public final static void showErrorMessageDialog(final Exception e,
            final GraphicalViewer viewer) {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                MessageDialog.openError(viewer.getControl().getShell(),
                        "Error", e.getMessage());
            }
        });
    }
}
