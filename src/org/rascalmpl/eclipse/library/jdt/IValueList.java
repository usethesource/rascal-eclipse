package org.rascalmpl.eclipse.library.jdt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;

public class IValueList {
	private static final long serialVersionUID = -6969504437423793359L;
	
	private final IValueFactory values;

	private List<IValue> valueList;
		
	public IValueList(final IValueFactory values) {
		this.values = values;
		
		valueList = new ArrayList<IValue>();
	}
	
	public void add(IValue value) {
		valueList.add(value);
	}
	
	private IValue[] toArray() {
		return valueList.toArray(new IValue[0]);
	}
	
	public IValue asList() {
		return values.list(toArray());
	}
}
