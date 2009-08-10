package org.meta_environment.rascal.eclipse.lib;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;

public class Schema {

	private static Map<String, StringBuilder> dataEntries = new HashMap<String, StringBuilder>();
	
	private static void addChild(ITuple entry) {
		String parent = ((IString)entry.get(0)).getValue();
		String child = ((IString)entry.get(1)).getValue();
				
		StringBuilder data = dataEntries.get(parent);
		boolean first = (data.charAt(data.length()-1) == '(');
		data.append(first? child : "," + child);
	}
	
	private static void openDataEntries(ISet classes) {
		for(IValue className: classes) {
			String parent = ((IString)className).getValue();
			dataEntries.put(parent, new StringBuilder("data " + parent + " = " + parent.toLowerCase() + "("));
		}
	}
	
	private static void closeDataEntries() {
		for(StringBuilder data: dataEntries.values()) {
			data.append(");");
		}
	}
	
	private static void fillDataEntries(IRelation datadef) {
		openDataEntries(datadef.domain());
		for(IValue data: datadef) {
			addChild((ITuple) data);
		}
		closeDataEntries();
	}
	
	
	
	
	public static void printData(IString path,
			IRelation datadef) throws IOException {
		
		try {
			PrintWriter pw; 
			pw = new PrintWriter(new FileWriter(path.getValue(), false));
		
		
			fillDataEntries(datadef);
			for(StringBuilder data: dataEntries.values()) {
				pw.println(data.toString());
			}
		
			pw.close(); // Without this, the output file may be empty
		} catch (IOException e) {
			System.err.println("Writing to " + path.getValue() + " failed");
		}
	}

}
