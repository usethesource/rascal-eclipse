/*******************************************************************************
 * Copyright (c) 2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.debug.core.breakpoints;

import static org.rascalmpl.eclipse.debug.core.breakpoints.RascalSourceLocationBreakpoint.MARKER_ATTRIBUTE_BEGIN_COLUMN;
import static org.rascalmpl.eclipse.debug.core.breakpoints.RascalSourceLocationBreakpoint.MARKER_ATTRIBUTE_BEGIN_LINE;
import static org.rascalmpl.eclipse.debug.core.breakpoints.RascalSourceLocationBreakpoint.MARKER_ATTRIBUTE_END_COLUMN;
import static org.rascalmpl.eclipse.debug.core.breakpoints.RascalSourceLocationBreakpoint.MARKER_ATTRIBUTE_END_LINE;
import static org.rascalmpl.eclipse.debug.core.breakpoints.RascalSourceLocationBreakpoint.MARKER_TYPE;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.IMarkerUpdater;
import org.rascalmpl.value.ISourceLocation;


/**
 * Marker updater for basic marker attributes and extended attributes that are 
 * defined in {@link RascalSourceLocationBreakpoint}, i.e. attributes that extend 
 * the marker with more detailed position information from {@link ISourceLocation}.
 */
public class SourceLocationMarkerUpdater implements IMarkerUpdater {

	private final static String[] ATTRIBUTES = {
		IMarker.CHAR_START,
		IMarker.CHAR_END,
		MARKER_ATTRIBUTE_BEGIN_LINE,
		MARKER_ATTRIBUTE_END_LINE,
		MARKER_ATTRIBUTE_BEGIN_COLUMN,
		MARKER_ATTRIBUTE_END_COLUMN
	};
	
	@Override
	public String getMarkerType() {
		return MARKER_TYPE;
	}

	@Override
	public String[] getAttribute() {
		return ATTRIBUTES;
	}

	@Override
	public boolean updateMarker(IMarker marker, IDocument document, Position position) {

		if (position == null)
			return true;
		
		if (position.isDeleted())
			return false;

		try {

			// adjust offset / length
			marker.setAttribute(IMarker.CHAR_START, position.getOffset());
			marker.setAttribute(IMarker.CHAR_END, position.getOffset() + position.getLength());			
			
			// set marker line numbers (are 1-base)
			int begLine = document.getLineOfOffset(position.getOffset());
			int endLine = document.getLineOfOffset(position.getOffset() + position.getLength());

			marker.setAttribute(MARKER_ATTRIBUTE_BEGIN_LINE, begLine + 1);
			marker.setAttribute(MARKER_ATTRIBUTE_END_LINE, endLine + 1);

			// set marker column information
			int begCol = position.getOffset() - document.getLineOffset(begLine);
			int endCol = position.getOffset() + position.getLength() - document.getLineOffset(endLine);

			marker.setAttribute(MARKER_ATTRIBUTE_BEGIN_COLUMN, begCol);
			marker.setAttribute(MARKER_ATTRIBUTE_END_COLUMN, endCol);

		} catch (Exception e) {
			// code above is not meant to throw an exception; in case of error throw an unchecked exception instead
			throw new RuntimeException(e);
		}

		return true;
	}

}
