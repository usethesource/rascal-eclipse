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
package org.rascalmpl.eclipse.debug.ui.breakpoints;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.INode;
import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.visitors.NullVisitor;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.debug.core.breakpoints.RascalSourceLocationBreakpoint;
import org.rascalmpl.eclipse.debug.uri.ProjectURIToStandardLibraryTransformer;
import org.rascalmpl.eclipse.uri.ProjectURIResolver;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.uptr.Factory;

/**
 * Adapter to create line breakpoints in Rascal files.
 */
public class RascalBreakpointAdapter implements IToggleBreakpointsTargetExtension {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
		return getEditor(part) != null;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		ITextEditor textEditor = getEditor(part);
		if (textEditor != null) {
			IResource resource = (IResource) textEditor.getEditorInput().getAdapter(IResource.class);
			ITextSelection textSelection = (ITextSelection) selection;
			int lineNumber = textSelection.getStartLine();
			IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(IRascalResources.ID_RASCAL_DEBUG_MODEL);
			
			for (IBreakpoint breakpoint : breakpoints) {
				if (breakpoint instanceof ILineBreakpoint && resource.equals(breakpoint.getMarker().getResource())) {
					int breakPointLine = ((ILineBreakpoint) breakpoint).getLineNumber();
					if (breakPointLine == (lineNumber + 1)) {
						// remove
						breakpoint.delete();
						return;
					}
				}
			}
			
			/*
			 * Find a source location associated with a line number.			
			 */
			IConstructor parseTree = (IConstructor) ((UniversalEditor) textEditor).getParseController().getCurrentAst();
			parseTree = org.rascalmpl.interpreter.debug.DebugUpdater.pushDownAttributes(parseTree);		
			
			ISourceLocation closestSourceLocation = calculateClosestLocation(parseTree, lineNumber + 1);
			
			if (closestSourceLocation == null) {

				IStatus message = new Status(IStatus.INFO, Activator.PLUGIN_ID, "Breakpoint not created, no 'breakable' annotation associated to parse tree of line.");
				Activator.getInstance().getLog().log(message);
			
			} else {		
				ISourceLocation normalizedSourceLocation = normalizeSourceLocation(resource, closestSourceLocation);			
				
				// create line breakpoint
				RascalSourceLocationBreakpoint lineBreakpoint = new RascalSourceLocationBreakpoint(resource, normalizedSourceLocation);
				DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(lineBreakpoint);
			}
		}
	}
	
	/**
	 * Returns the editor being used to edit a Rascal file, associated with the
	 * given part, or <code>null</code> if none.
	 *  
	 * @param part workbench part
	 * @return the editor being used to edit a Rascal file, associated with the
	 * given part, or <code>null</code> if none
	 */
	private ITextEditor getEditor(IWorkbenchPart part) {
		if (part instanceof ITextEditor) {
			ITextEditor editorPart = (ITextEditor) part;
			IResource resource = (IResource) editorPart.getEditorInput().getAdapter(IResource.class);
			if (resource != null) {
				String extension = resource.getFileExtension();
				if (extension != null && extension.equals(IRascalResources.RASCAL_EXT)) {
					return editorPart;
				}
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension#canToggleBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection) {
		return canToggleLineBreakpoints(part, selection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension#toggleBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		if (canToggleLineBreakpoints(part, selection)) {
			toggleLineBreakpoints(part, selection);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
	}

	/*
	 * Query the closest (i.e. first) source location of a 'breakable' parse tree node associated with a line number?
	 */
	private static ISourceLocation calculateClosestLocation(final IConstructor parseTree, final int lineNumber){
		class OffsetFinder extends NullVisitor<IValue>{
	
			private IValueFactory VF = ValueFactoryFactory.getValueFactory(); 
			
			private ISourceLocation location = null;
			
			public ISourceLocation getSourceLocation() {
				return location;
			}
			
			public IValue visitConstructor(IConstructor o) throws VisitorException{
				IValue locationAnnotation = o.getAnnotation(Factory.Location);
				
				if(locationAnnotation != null){
					ISourceLocation sourceLocation = ((ISourceLocation) locationAnnotation);
					
					if(sourceLocation.getBeginLine() == lineNumber){
						Map<String, IValue> annotations = o.getAnnotations();
						
						if (annotations != null 
								&& annotations.containsKey("breakable")
								&& annotations.get("breakable").equals(VF.bool(true))) {
							location = sourceLocation;
							throw new VisitorException("Stop");
						}
					}
				}
				
				for(IValue child : o){
					child.accept(this);
				}
				
				return null;
			}
			
			public IValue visitNode(INode o) throws VisitorException{
				for(IValue child : o){
					child.accept(this);
				}
				
				return null;
			}
			
			public IValue visitList(IList o) throws VisitorException{
				for(IValue v : o){
					v.accept(this);
				}
				return null;
			}
			
			public IValue visitSet(ISet o) throws VisitorException{
				for(IValue v : o){
					v.accept(this);
				}
				return null;
			}
			
			public IValue visitRelation(IRelation o) throws VisitorException{
				for(IValue v : o){
					v.accept(this);
				}
				return null;
			}
			
			public IValue visitMap(IMap o) throws VisitorException{
				for(IValue v : o){
					v.accept(this);
				}
				return null;
			}
		}
		
		OffsetFinder of = new OffsetFinder();
		try{
			parseTree.accept(of);
		}catch(VisitorException vex){
			// Ignore.
		}
		
		return of.getSourceLocation();
	}

	private static ISourceLocation normalizeSourceLocation(IResource resource, ISourceLocation sourceLocation) { 
		// 1) create a non-encoded "project" schema URI
		URI uriBreakPointLocation = ProjectURIResolver.constructNonEncodedProjectURI(resource.getFullPath());
		
		// 2) transform the "project" schema URI to a "std" schema URI (if necessary)
		try {
			uriBreakPointLocation = new ProjectURIToStandardLibraryTransformer(IRascalResources.RASCAL_STD)
					.getResourceURI(uriBreakPointLocation);
		} catch (IOException e) {
			/* not supposed to happen */
			IStatus message = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Conversion of URI schema 'project' to 'std' failed.");
			Activator.getInstance().getLog().log(message);			
		}
		
		// 3) update the URI in the source location instance
		return updateSourceLocation(sourceLocation, uriBreakPointLocation);
	}
	
	/**
     * Create a copy of a source location, but replace the URI.
     * 
     * @param uri         exact uri where the source is located.
     * @return a value representing a source location, with type SourceLocationType
     */
    private static ISourceLocation updateSourceLocation(ISourceLocation location, URI uri) {
    	IValueFactory valueFactory = ValueFactoryFactory.getValueFactory();
    	ISourceLocation result = null;
    	
    	if (location.hasLineColumn()) {
        	result = valueFactory.sourceLocation(uri, 
					location.getOffset(), location.getLength(), 
					location.getBeginLine(), location.getEndLine(), 
					location.getBeginColumn(),location.getEndColumn());
    	} else if (location.hasOffsetLength()) {
        	result = valueFactory.sourceLocation(uri, 
        			location.getOffset(), location.getLength()); 
    	} else {
    		result = valueFactory.sourceLocation(uri);
    	}
    	
    	return result;
    }
    
}
