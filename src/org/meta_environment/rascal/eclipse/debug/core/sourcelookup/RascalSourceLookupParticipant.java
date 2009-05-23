package org.meta_environment.rascal.eclipse.debug.core.sourcelookup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;

import org.meta_environment.rascal.eclipse.debug.core.model.RascalStackFrame;

/**
 * The Rascal source lookup participant knows how to translate a 
 * rascal stack frame into a source file name 
 */
public class RascalSourceLookupParticipant extends AbstractSourceLookupParticipant {
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupParticipant#getSourceName(java.lang.Object)
	 */
	public String getSourceName(Object object) throws CoreException {
		if (object instanceof RascalStackFrame) {
			return ((RascalStackFrame)object).getSourceName();
		}
		return null;
	}
}
