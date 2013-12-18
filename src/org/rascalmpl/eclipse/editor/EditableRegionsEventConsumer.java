package org.rascalmpl.eclipse.editor;

import java.util.LinkedHashMap;

import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.events.VerifyEvent;

public class EditableRegionsEventConsumer implements IEventConsumer{

	private LinkedHashMap<String, IRegion> regions;

	public EditableRegionsEventConsumer(LinkedHashMap<String, IRegion> regions) {
		this.regions = regions;
	}
	
	private boolean contains(IRegion region, int offset){
		return offset>=region.getOffset() && offset<= region.getOffset()+region.getLength();
	}
	
	private boolean inRegion(int offset){
		for (IRegion region:regions.values()){
			if (contains(region, offset)){
				return true;
			}
		}
		return false;
	}

	@Override
	public void processEvent(VerifyEvent event) {
 		if (!inRegion(event.start))
			event.doit = false;
	}

	public void setRegions(LinkedHashMap<String, IRegion> regions) {
		this.regions = regions;
	}

}
