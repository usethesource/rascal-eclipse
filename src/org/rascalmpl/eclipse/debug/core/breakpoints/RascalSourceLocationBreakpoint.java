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
package org.rascalmpl.eclipse.debug.core.breakpoints;

import java.net.URISyntaxException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.LineBreakpoint;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.debug.core.model.RascalDebugTarget;
import org.rascalmpl.eclipse.debug.core.model.RascalThread;
import org.rascalmpl.interpreter.IInterpreterEventListener;
import org.rascalmpl.interpreter.InterpreterEvent;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.ValueFactoryFactory;

import static org.rascalmpl.interpreter.debug.DebugMessageFactory.*;

/**
 * A generalized Rascal source location breakpoint.
 * 
 * TODO: create an own RascalLineBreakpoint class inheriting from this class.
 */
public class RascalSourceLocationBreakpoint extends LineBreakpoint implements IInterpreterEventListener {
	
	/**
	 * Type of the marker.
	 * 
	 * @see IMarker#getType() 
	 */
	protected static final String MARKER_TYPE = "rascal.markerType.sourceLocationBreakpoint";
	
	/*
	 * Custom marker attribute that stores a serialized source location.
	 * 
	 * NOTE: #MARKER_ATTRIBUTE_BEGIN_LINE is a synonym to IMarker#LINE_NUMBER
	 * 
	 * @see IMarker#setAttribute(String, Object)
	 * @see ISourceLocation
	 */
	protected static final String MARKER_ATTRIBUTE_BEGIN_LINE 	= IMarker.LINE_NUMBER;	
	protected static final String MARKER_ATTRIBUTE_END_LINE 	= "lineNumberEnd";
	protected static final String MARKER_ATTRIBUTE_BEGIN_COLUMN = "beginColumn";	
	protected static final String MARKER_ATTRIBUTE_END_COLUMN 	= "endColumn";
	protected static final String MARKER_ATTRIBUTE_URI 			= "uri";
	
	// target currently installed in
	private RascalDebugTarget fTarget;

	// resource associated with the marker
	private IResource resource;

	/**
	 * Default constructor is required for the breakpoint manager
	 * to re-create persisted breakpoints. After instantiating a breakpoint,
	 * the <code>setMarker(...)</code> method is called to restore
	 * this breakpoint's attributes.
	 */
	public RascalSourceLocationBreakpoint() {
		super();
	}
		
	/**
	 * Constructs a line breakpoint on the given resource and a complete 
	 * source location entry. The line number is 1-based (i.e. the first 
	 * line of a file is line number 1, {@link ISourceLocation#getBeginLine()}). 
	 * The Rascal VM uses as well source location objects to identify 
	 * AST items.
	 * 
	 * @param resource file on which to set the breakpoint
	 * @param sourceLocaton fine grained source location of the breakpoint
	 * @throws CoreException if unable to create the breakpoint
	 */
	public RascalSourceLocationBreakpoint(final IResource resource, final ISourceLocation sourceLocation) throws CoreException {

		Assert.isNotNull(resource);
		Assert.isNotNull(sourceLocation);
		Assert.isTrue(sourceLocation.hasOffsetLength());
		Assert.isTrue(sourceLocation.hasLineColumn());
		
		// initialize attributes
		this.resource = resource;
		
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IMarker marker = resource.createMarker(MARKER_TYPE);

				// standard attributes
				marker.setAttribute(IBreakpoint.ID, getModelIdentifier());
				marker.setAttribute(IBreakpoint.ENABLED, Boolean.TRUE);
				marker.setAttribute(IMarker.MESSAGE, "Line Breakpoint: " + resource.getName() + " [line: " + sourceLocation.getBeginLine() + "]");
				
				// hasOffsetLength()
				int charStart = sourceLocation.getOffset();
				int charEnd   = sourceLocation.getLength() + charStart;
									
				marker.setAttribute(IMarker.CHAR_START, charStart);
				marker.setAttribute(IMarker.CHAR_END, charEnd);

				// custom attributes			
				marker.setAttribute(MARKER_ATTRIBUTE_BEGIN_LINE, sourceLocation.getBeginLine());
				marker.setAttribute(MARKER_ATTRIBUTE_END_LINE, sourceLocation.getEndLine());
				marker.setAttribute(MARKER_ATTRIBUTE_BEGIN_COLUMN, sourceLocation.getBeginColumn());
				marker.setAttribute(MARKER_ATTRIBUTE_END_COLUMN, sourceLocation.getEndColumn());
				
				marker.setAttribute(MARKER_ATTRIBUTE_URI, sourceLocation.getURI().toString());
				
				setMarker(marker);
			}
		};
		run(getMarkerRule(resource), runnable);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IBreakpoint#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return IRascalResources.ID_RASCAL_DEBUG_MODEL;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.Breakpoint#setMarker(org.eclipse.core.resources.IMarker)
	 */
	@Override
	public void setMarker(IMarker marker) throws CoreException {
		super.setMarker(marker);
		
		// restore attributes for persisted breakpoints
		resource = marker.getResource();
	}
		
	/**
	 * Returns whether this breakpoint is a run-to-line breakpoint
	 * 
	 * @return whether this breakpoint is a run-to-line breakpoint
	 */
	public boolean isRunToLineBreakpoint() {
		return false;
	}
    
    /**
     * Installs this breakpoint in the given interpreter.
     * 
     * @param target Rascal interpreter
     * @throws CoreException if installation fails
     */
    public void install(RascalDebugTarget target) throws CoreException {
       	fTarget = target;
    	target.addEventListener(this);
    	createRequest(target);
    }
    
    /**
     * Removes this breakpoint from the given interpreter.
     * Removes this breakpoint as an event listener and clears
     * the request for the interpreter.
     * 
     * @param target Rascal interpreter
     * @throws CoreException if removal fails
     */
    public void remove(RascalDebugTarget target) throws CoreException {
    	clearRequest(target);
    	target.removeEventListener(this);
    	fTarget = null;
    }

    /**
     * Create the breakpoint specific request in the target. Subclasses
     * should override.
     * 
     * @param target Rascal interpreter
     * @throws CoreException if request creation fails
     */
    protected void createRequest(RascalDebugTarget target) throws CoreException {
    	target.sendRequest(requestSetBreakpoint(getSourceLocation()));
    }
    
    /**
     * Removes this breakpoint's event request from the target. Subclasses
     * should override.
     * 
     * @param target Rascal interpreter
     * @throws CoreException if clearing the request fails
     */
    protected void clearRequest(RascalDebugTarget target) throws CoreException {
    	target.sendRequest(requestDeleteBreakpoint(getSourceLocation()));
    }    
    
    /**
     * Returns the target this breakpoint is installed in or <code>null</code>.
     * 
     * @return the target this breakpoint is installed in or <code>null</code>
     */
    protected RascalDebugTarget getDebugTarget() {
    	return fTarget;
    }

    /**
     * Returns the resource that is configured or <code>null</code>.
     * 
     * @return the resource that is configured or <code>null</code>.
     */
	protected IResource getResource() {
		return resource;
	}
	
    /**
     * Returns the source location that is configured or <code>null</code>.
     * The source location is reconstructed from the display {@link IMarker} 
     * and used to detect if a breakpoint was hit.
     * 
     * @return the source location that is configured or <code>null</code>.
     */
	protected ISourceLocation getSourceLocation() {
		try {
			return markerToSourceLocation(getMarker());
		} catch (CoreException e) {
			return null;
		}
	}
    	
	private static ISourceLocation markerToSourceLocation(IMarker marker) throws CoreException {
		IValueFactory valueFactory = ValueFactoryFactory.getValueFactory();
		ISourceLocation result = null;
		
		// hasOffsetLength()
		int offset = (Integer) marker.getAttribute(IMarker.CHAR_START);
		int length = (Integer) marker.getAttribute(IMarker.CHAR_END) - offset;

		// hasLineColumn()
		int beginLine = (Integer) marker.getAttribute(MARKER_ATTRIBUTE_BEGIN_LINE);
		int endLine   = (Integer) marker.getAttribute(MARKER_ATTRIBUTE_END_LINE);
		int beginCol  = (Integer) marker.getAttribute(MARKER_ATTRIBUTE_BEGIN_COLUMN);
		int endCol    = (Integer) marker.getAttribute(MARKER_ATTRIBUTE_END_COLUMN);

		String uriString = (String) marker.getAttribute(MARKER_ATTRIBUTE_URI);

		try {
			result = valueFactory.sourceLocation(URIUtil.createFromEncoded(uriString), offset, length, beginLine, endLine, beginCol, endCol);
		} catch (URISyntaxException e) {
			IStatus message = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Persisted URI string of the marker's source location is invalid.", e);

			throw new CoreException(message);
		}
	
		return result;
	}
	   
    /**
     * Notify's the Rascal interpreter that this breakpoint has been hit.
     */
    private final void notifyThread() {
    	if (fTarget != null) {
			try {
				IThread[] threads = fTarget.getThreads();
				if (threads.length == 1) {
	    			RascalThread thread = (RascalThread)threads[0];
	    			thread.suspendedBy(this);
	    		}
			} catch (DebugException e) {
			}    		
    	}
    }
   
	/**
     * Determines if this breakpoint was hit and notifies the thread.
     * 
     * @param event breakpoint event
     */
    private void handleHit(InterpreterEvent event) {
		ISourceLocation hitLocation = (ISourceLocation) event.getData();

		if (hitLocation.equals(getSourceLocation())) {
			notifyThread();
		}
    }
    
	@Override
	public void handleInterpreterEvent(InterpreterEvent event) {
		if (event.getKind() == InterpreterEvent.Kind.SUSPEND
				&& event.getDetail() == InterpreterEvent.Detail.BREAKPOINT) {
			handleHit(event);
		}		
	}

}
