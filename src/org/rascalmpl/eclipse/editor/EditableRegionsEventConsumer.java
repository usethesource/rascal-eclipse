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
	
	private boolean inRegion(int offset){
		for (IRegion region:regions.values()){
			if (offset>=region.getOffset() && offset<= region.getOffset()+region.getLength()){
				return true;
			}
		}
		return false;
	}

	@Override
	public void processEvent(VerifyEvent event) {
 		if (!inRegion(event.start)){
			event.doit = false;
		}else{
			updateRegions(event.start, event.text.length());
		}
		
	}

	private void updateRegions(int start, int length) {
		
		
	}

	public void setRegions(LinkedHashMap<String, IRegion> regions) {
		this.regions = regions;
	}

}
