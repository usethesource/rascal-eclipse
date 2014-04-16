package org.rascalmpl.eclipse.editor;

import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IMapWriter;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;

public class EditableRegionsTextListener implements ITextListener {

	private IValueFactory vf;
	private ISourceLocation loc;
	
	public EditableRegionsTextListener(IValueFactory vf, ISourceLocation location) {
		this.vf = vf;
		this.loc = location;
	}

	@Override
	public void textChanged(TextEvent event) {
		IMap regions = EditableRegionsRegistry.getRegistryForDocument(loc);
		if (regions == null)
			return;
		ISourceLocation region = getRegionForOffset(regions, event.getOffset());
		if (region != null){
			int delta = event.getText().length() - event.getLength();
			updateRegionsFrom(regions, region, delta);
		}
	}

	private boolean contains(ISourceLocation region, int offset){
		return offset>=region.getOffset() && offset<= region.getOffset()+region.getLength();
	}
	
	private ISourceLocation getRegionForOffset(IMap regions, int offset){
		Iterator<IValue> iterator = regions.iterator();
		while (iterator.hasNext()){
			ISourceLocation region = (ISourceLocation) iterator.next();
			if (contains(region, offset)){
				return region;
			}
		}
		return null;
	}
	
	private synchronized void updateRegionsFrom(IMap regions, ISourceLocation region, int delta) {
		Iterator<Entry<IValue,IValue>> entriesIterator = regions.entryIterator();
		IMapWriter newMap = vf.mapWriter();
		while (entriesIterator.hasNext()){
			Entry<IValue,IValue> entry = entriesIterator.next();
			ISourceLocation r = (ISourceLocation) entry.getKey();
			IString name = (IString) entry.getValue();
			if (r.getOffset()>=region.getOffset()){
				if (r.equals(region)){
					newMap.put(vf.sourceLocation(r, r.getOffset(), r.getLength() + delta), name);
				}else{
					newMap.put(vf.sourceLocation(r, r.getOffset() + delta, r.getLength()), name);
				}
			}
			else
				newMap.put(r, name);
		}
		EditableRegionsRegistry.setRegistryForDocument(loc, newMap.done());
	}

}
