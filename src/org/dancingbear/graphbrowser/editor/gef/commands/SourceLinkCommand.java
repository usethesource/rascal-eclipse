/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.commands;

import java.io.File;

import org.dancingbear.graphbrowser.GraphBrowserActivator;
import org.dancingbear.graphbrowser.model.IModelNode;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * The command that is fired when a node is double-clicked to go to the source
 * location that is linked to the node
 * 
 * @author Erik Slagter
 * @author Jeroen van Schagen
 * 
 */
public class SourceLinkCommand extends Command {

    private static class FileNotInEclipseWorkspaceException extends Exception {

        private static final long serialVersionUID = 2791620809706151140L;

        public FileNotInEclipseWorkspaceException() {
            super();
        }

        public FileNotInEclipseWorkspaceException(String message) {
            super(message);
        }
    }

    private IModelNode selectedNode;

    private IEditorPart editorPart;

    /**
     * Open the file that is linked to the selected node in a text editor. If
     * the node has no linked file no actions are performed
     */
    @Override
    public void execute() {
        String fileName = null;
        int lineNumber = 0;

        if (selectedNode != null) {
            fileName = getFileName(selectedNode);
            lineNumber = getLineNumber(selectedNode);
        }

        if (fileName == null) {
            // no source location is coupled with node
            return;
        }

        File file = new File(fileName);
        IFile ifile = null;
        try {
            ifile = convertToIFile(file);
        } catch (FileNotInEclipseWorkspaceException e) {
            e.printStackTrace();
        }

        if (ifile != null) {
            // file is part of the active eclipse workspace
            editorPart = openWorkspaceFileEditor(ifile, lineNumber);
        } else {
            // external file that is not in workspace
            editorPart = openBasicFileEditor(file, lineNumber);
        }
    }

    /**
     * Convert a normal java.io.File if and only if the file is part of the
     * workspace the plugin is opened
     * 
     * @param file a java.io.File
     * @return null if the @param file is null
     * @throws FileNotInEclipseWorkspaceException when file is not in eclipse
     * workspace
     */
    private IFile convertToIFile(File file)
            throws FileNotInEclipseWorkspaceException {
        if (file != null) {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IPath location = Path.fromOSString(file.getAbsolutePath());
            IFile ifile = workspace.getRoot().getFileForLocation(location);

            if (ifile == null) {
                throw new FileNotInEclipseWorkspaceException();
            }

            return ifile;
        }
        return null;
    }

    /**
     * Get the line number in the file
     * 
     * @param node
     * @return the line number, if not defined returns 0
     */
    private int getLineNumber(IModelNode node) {
        int lineNumber = 0;
        try {
            lineNumber = Integer.parseInt(node.getProperty("linenumber"));
        } catch (NumberFormatException nfe) {
        }
        return lineNumber;
    }

    /**
     * Get the file name of that is linked to the node
     * 
     * @param node
     * @return the file name, if not defined, return null
     */
    private String getFileName(IModelNode node) {
        return node.getProperty("link");
    }

    /**
     * Open an IFile instance in an Eclipse texteditor at the selected line #
     * 
     * @param file the file that is displayed
     * @param lineNumber the line that is requested
     */
    private IEditorPart openWorkspaceFileEditor(IFile file, int lineNumber) {
        IEditorPart part = null;
        try {
            IWorkbenchPage page = GraphBrowserActivator.getDefault()
                    .getWorkbench().getActiveWorkbenchWindow().getActivePage();

            IEditorDescriptor desc = PlatformUI.getWorkbench()
                    .getEditorRegistry().getDefaultEditor(file.getName());

            String editor = null;
            if (desc == null) {
                editor = "org.eclipse.ui.DefaultTextEditor";
            } else {
                editor = desc.getId();
            }

            part = page.openEditor(new FileEditorInput(file), editor);

            revealInEditor(part, lineNumber);
        } catch (PartInitException e) {
            e.printStackTrace();
        }

        return part;
    }

    /**
     * Open a java.io.File in a basic texteditor
     * 
     * @param file the file that is displayed
     * @param lineNumber
     */
    private IEditorPart openBasicFileEditor(File file, int lineNumber) {

        if (file.exists() && file.isFile()) {
            IWorkbenchPage page = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage();

            IFileStore fileStore = EFS.getLocalFileSystem().getStore(
                    file.toURI());

            IEditorPart editor = null;
            try {
                editor = IDE.openEditorOnFileStore(page, fileStore);
                revealInEditor(editor, lineNumber);
            } catch (PartInitException e) {
                e.printStackTrace();
            }
            return editor;
        }

        return null;
    }

    /**
     * Set the cursor to the given line number in the editor
     * 
     * @param part EditorPart to set cursor on
     * @param lineNr Linenumber to set cursor on
     */
    public void revealInEditor(IEditorPart part, int lineNr) {
        int lineNumber = lineNr;
        if (lineNumber > 0) {
            if (part instanceof ITextEditor) {
                ITextEditor textEditor = (ITextEditor) part;
                try {
                    lineNumber = lineNumber - 1;
                    IDocument document = textEditor.getDocumentProvider()
                            .getDocument(textEditor.getEditorInput());
                    textEditor.selectAndReveal(document
                            .getLineOffset(lineNumber), document
                            .getLineLength(lineNumber));
                } catch (BadLocationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Undo opening editor
     */
    @Override
    public void undo() {
        if (editorPart != null) {
            editorPart.dispose();
        }
        super.undo();
    }

    /**
     * Get selected node
     * 
     * @return the selectedNode
     */
    public IModelNode getSelectedNode() {
        return selectedNode;
    }

    /**
     * Set selected node
     * 
     * @param selectedNode the selectedNode to set
     */
    public void setSelectedNode(IModelNode selectedNode) {
        this.selectedNode = selectedNode;
    }
}