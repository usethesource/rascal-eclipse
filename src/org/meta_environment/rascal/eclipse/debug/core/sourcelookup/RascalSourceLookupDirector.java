package org.meta_environment.rascal.eclipse.debug.core.sourcelookup;

import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;

import org.meta_environment.rascal.eclipse.debug.core.sourcelookup.RascalSourceLookupParticipant;

public class RascalSourceLookupDirector extends AbstractSourceLookupDirector {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector#initializeParticipants()
	 */
	public void initializeParticipants() {
		addParticipants(new ISourceLookupParticipant[]{new RascalSourceLookupParticipant()});
	}

}
