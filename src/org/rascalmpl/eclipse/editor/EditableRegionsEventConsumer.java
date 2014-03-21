package org.rascalmpl.eclipse.editor;

import java.util.Iterator;

import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.swt.events.VerifyEvent;

public class EditableRegionsEventConsumer implements IEventConsumer{
	
	private ISourceLocation location;

	public EditableRegionsEventConsumer(ISourceLocation location) {
		this.location = location;
	}
	
	private boolean contains(ISourceLocation region, int offset){
		return offset>=region.getOffset() && offset<= region.getOffset()+region.getLength();
	}
	
	private boolean inRegion(IMap regions, int offset){
		Iterator<IValue> iterator = regions.iterator();
		while (iterator.hasNext()){
			ISourceLocation region = (ISourceLocation) iterator.next();
			if (contains(region, offset)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void processEvent(VerifyEvent event) {
		IMap regions = EditableRegionsRegistry.getRegistryForDocument(location);
		if (regions == null)
			return;
 		if (!inRegion(regions, event.start))
			event.doit = false;
	}

}
