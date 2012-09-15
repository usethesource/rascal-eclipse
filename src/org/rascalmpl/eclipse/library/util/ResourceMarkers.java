/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
*******************************************************************************/
package org.rascalmpl.eclipse.library.util;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.library.lang.java.jdt.internal.JDT;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;

public class ResourceMarkers {
	private final IValueFactory values;
	private final JDT jdt;
	
	public ResourceMarkers(IValueFactory values){
		super();
		this.values = values;
		this.jdt = new JDT(values);
	}

	public void removeMessageMarkers(ISourceLocation loc, IEvaluatorContext ctx) {
		IFile file = jdt.getIFileForLocation(loc);
		try {
			file.deleteMarkers(IRascalResources.ID_RASCAL_MARKER, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
			throw RuntimeExceptionFactory.javaException("Failed to remove markers: " + ce.getMessage(), null, Arrays.toString(ce.getStackTrace()));
		}
	}

	private void removeMessageMarkers(ISet markers, IEvaluatorContext ctx) {
		for (IValue msg : markers) {
			IConstructor marker = (IConstructor) msg;
			if (! marker.getType().getName().equals("Message"))
				throw RuntimeExceptionFactory.illegalArgument(marker, null, null);
			removeMessageMarkers((ISourceLocation)marker.get(1), ctx);
		}
		return;
	}
	
	public void addMessageMarkers(ISet markers, IEvaluatorContext ctx) {
		removeMessageMarkers(markers, ctx);
		
		for (IValue msg : markers) {
			IConstructor marker = (IConstructor) msg;
			if (! marker.getType().getName().equals("Message"))
				throw RuntimeExceptionFactory.illegalArgument(marker, null, null);
			IFile file = jdt.getIFileForLocation((ISourceLocation)marker.get(1));
			try {
				int severity = IMarker.SEVERITY_INFO;
				if (marker.getName().equals("error"))
					severity = IMarker.SEVERITY_ERROR;
				else if (marker.getName().equals("warning"))
					severity = IMarker.SEVERITY_WARNING;

				IString markerMessage = (IString)marker.get(0);
				ISourceLocation markerLocation = (ISourceLocation)marker.get(1);

		        String[] attributeNames= new String[] {
		                IMarker.LINE_NUMBER, 
		                IMarker.CHAR_START, 
		                IMarker.CHAR_END, 
		                IMarker.MESSAGE, 
		                IMarker.PRIORITY, 
		                IMarker.SEVERITY
		        };

		        Object[] values= new Object[] {
		                markerLocation.getBeginLine(), 
		                markerLocation.getOffset(), 
		                markerLocation.getOffset() + markerLocation.getLength(), 
		                markerMessage.getValue(), 
		                IMarker.PRIORITY_HIGH, 
		                severity
		        };

		        IMarker m = file.createMarker(IRascalResources.ID_RASCAL_MARKER);
		        m.setAttributes(attributeNames, values);
			} catch (CoreException ce) {
				throw RuntimeExceptionFactory.javaException("Failed to add markers: " + ce.getMessage(), null, Arrays.toString(ce.getStackTrace()));
			}
		}
	}
}
