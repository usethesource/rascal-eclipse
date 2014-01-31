package org.rascalmpl.eclipse.editor;

import java.util.LinkedHashMap;

import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.swt.events.VerifyEvent;

public class EditableRegionsEventConsumer implements IEventConsumer{
	
	private LinkedHashMap<String, ISourceLocation> regions;
	
	public EditableRegionsEventConsumer(ISourceLocation location) {
		regions = EditableRegionsRegistry.getRegistryForDocument(location);
	}
	
	private boolean contains(ISourceLocation region, int offset){
		return offset>=region.getOffset() && offset<= region.getOffset()+region.getLength();
	}
	
	private boolean inRegion(int offset){
		for (ISourceLocation region : regions.values()){
			if (contains(region, offset)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void processEvent(VerifyEvent event) {
		if (regions == null)
			return;
 		if (!inRegion(event.start))
			event.doit = false;
	}

}
