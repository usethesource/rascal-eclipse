package org.rascalmpl.eclipse.editor;

import java.util.LinkedHashMap;

import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;

public class EditableRegionsTextListener implements ITextListener {

	private IValueFactory vf;
	private LinkedHashMap<String, ISourceLocation> regions;
	
	public EditableRegionsTextListener(IValueFactory vf, ISourceLocation location) {
		this.vf = vf;
		this.regions = EditableRegionsRegistry.getRegistryForDocument(location);
	}

	@Override
	public void textChanged(TextEvent event) {
		if (regions == null)
			return;
		ISourceLocation region = getRegionForOffset(event.getOffset());
		if (region != null){
			int delta = event.getText().length() - event.getLength();
			updateRegionsFrom(region, delta);
		}
	}

	private boolean contains(ISourceLocation region, int offset){
		return offset>=region.getOffset() && offset<= region.getOffset()+region.getLength();
	}
	
	private ISourceLocation getRegionForOffset(int offset){
		for (ISourceLocation region:regions.values()){
			if (contains(region, offset)){
				return region;
			}
		}
		return null;
	}

	private synchronized void updateRegionsFrom(ISourceLocation region, int delta) {
		boolean found = false;
		for (String name:regions.keySet()){
			ISourceLocation r = regions.get(name);
			if (r.equals(region)){
				found = true;
			}
			if (found){
				if (r.equals(region)){
					regions.put(name, vf.sourceLocation(region, r.getOffset(), r.getLength() + delta));
				}else{
					regions.put(name, vf.sourceLocation(region, r.getOffset() + delta, r.getLength()));
				}
			}
		}
			
	}

}
