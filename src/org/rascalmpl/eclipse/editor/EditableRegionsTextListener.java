package org.rascalmpl.eclipse.editor;

import java.util.LinkedHashMap;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextEvent;

public class EditableRegionsTextListener implements ITextListener {

	private LinkedHashMap<String, IRegion> regions;

	public EditableRegionsTextListener(LinkedHashMap<String, IRegion> regions) {
		this.regions = regions;
	}

	@Override
	public void textChanged(TextEvent event) {
		IRegion region = getRegionForOffset(event.getOffset());
		if (region != null){
			int delta = event.getText().length() - event.getLength();
			updateRegionsFrom(region, delta);
		}
	}

	private boolean contains(IRegion region, int offset){
		return offset>=region.getOffset() && offset<= region.getOffset()+region.getLength();
	}
	
	private IRegion getRegionForOffset(int offset){
		for (IRegion region:regions.values()){
			if (contains(region, offset)){
				return region;
			}
		}
		return null;
	}

	private synchronized void updateRegionsFrom(IRegion region, int delta) {
		boolean found = false;
		for (String name:regions.keySet()){
			IRegion r = regions.get(name);
			if (r.equals(region)){
				found = true;
			}
			if (found){
				if (r.equals(region)){
					regions.put(name, new Region(r.getOffset(), r.getLength()+ delta));
				}else{
					regions.put(name, new Region(r.getOffset() + delta, r.getLength()));
				}
			}
		}
			
	}

	public void setRegions(LinkedHashMap<String, IRegion> regions) {
		this.regions = regions;
	}
}
