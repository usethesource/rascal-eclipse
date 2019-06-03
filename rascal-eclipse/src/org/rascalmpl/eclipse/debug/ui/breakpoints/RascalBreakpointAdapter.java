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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.debug.core.breakpoints.RascalSourceLocationBreakpoint;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.uptr.ITree;
import org.rascalmpl.values.uptr.ProductionAdapter;
import org.rascalmpl.values.uptr.RascalValueFactory;
import org.rascalmpl.values.uptr.TreeAdapter;
import org.rascalmpl.values.uptr.visitors.TreeVisitor;

import io.usethesource.impulse.editor.UniversalEditor;
import io.usethesource.impulse.services.IASTFindReplaceTarget;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;

/**
 * Adapter to create line breakpoints in Rascal files.
 */
public class RascalBreakpointAdapter implements IToggleBreakpointsTargetExtension {
	
	/**
	 * AST enriched with debugging annotations.
	 * 
	 * @param editor the current IMP editor
	 * @return debugging enriched AST
	 */
	private ITree getAST(IASTFindReplaceTarget editor) {
		return (ITree) editor.getParseController().getCurrentAst();		
	}
	
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
			if (resource == null || !resource.exists()) {
				return;
			}
			
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
			IASTFindReplaceTarget astAwareEditor = (IASTFindReplaceTarget) textEditor;
			ISourceLocation closestSourceLocation = calculateClosestLocation(getAST(astAwareEditor), lineNumber + 1);
			
			if (closestSourceLocation == null) {

				IStatus message = new Status(IStatus.INFO, Activator.PLUGIN_ID, "Breakpoint not created, no 'breakable' annotation associated to parse tree of line.");
				Activator.getInstance().getLog().log(message);
			
			} else {		
				// create line breakpoint
				RascalSourceLocationBreakpoint lineBreakpoint = new RascalSourceLocationBreakpoint(resource, closestSourceLocation);
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
		if (part instanceof UniversalEditor) {
		  UniversalEditor editorPart = (UniversalEditor) part;
		  
		  if (editorPart.getParseController().getPath().getFileExtension().equals(IRascalResources.RASCAL_EXT)) {
		    return editorPart;
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
		class OffsetFinder extends TreeVisitor<Exception> {
	
			private IValueFactory VF = ValueFactoryFactory.getValueFactory(); 
			
			private ISourceLocation location = null;
			
			public ISourceLocation getSourceLocation() {
				return location;
			}
			
			@Override
			public ITree visitTreeAppl(ITree arg) throws Exception {
				if (location != null) {
					return arg; // found already something so we are done
				}
				
				IValue locationAnnotation = TreeAdapter.getLocation(arg);
				
				if (locationAnnotation != null) {
					ISourceLocation sourceLocation = ((ISourceLocation) locationAnnotation);
					
					if (sourceLocation.getBeginLine() == lineNumber) {
						IConstructor prod = TreeAdapter.getProduction(arg);
						
						if (ProductionAdapter.hasAttribute(prod, VF.constructor(RascalValueFactory.Attr_Tag, VF.node("breakable")))) {
							location = sourceLocation;
							return arg;
						}
					}
				}
				
				if (TreeAdapter.isLexical(arg)) {
					return null;
				}
				
				for (IValue child : TreeAdapter.getASTArgs(arg)) {
					ITree tree = (ITree) child;
					ISourceLocation treeLoc = TreeAdapter.getLocation(tree);
					if (treeLoc == null) {
						continue; // we are below annotated structure, no use continuing
					}
					
					// dive only into the tree which may contain the line
					if (treeLoc.getBeginLine() <= lineNumber && lineNumber <= treeLoc.getEndLine()) {
						tree.accept(this);
					}
				}
				
				return null;
			}
			
			@Override
			public ITree visitTreeAmb(ITree arg) throws Exception {
				// for robustness sake, but this should not happen
				return ((ITree) arg.getAlternatives().iterator().next()).accept(this);
			}
			
			@Override
			public ITree visitTreeChar(ITree arg) throws Exception {
				return null;
			}
			
			@Override
			public ITree visitTreeCycle(ITree arg) throws Exception {
				return null;
			}
		}
		
		OffsetFinder of = new OffsetFinder();
		try{
			parseTree.accept(of);
		}catch(Exception vex){
			// Ignore.
		  ;
		}
		
		return of.getSourceLocation();
	}
}
