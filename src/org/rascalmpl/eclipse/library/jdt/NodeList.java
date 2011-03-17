package org.rascalmpl.eclipse.library.jdt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.imp.pdb.facts.INode;

public class NodeList {
	private static final long serialVersionUID = -6969504437423793359L;
	
	private List<INode> nodeList;
	
	public NodeList() {
		nodeList = new ArrayList<INode>();
	}
	
	public void add(INode node) {
		nodeList.add(node);
	}
	
	public INode[] toArray() {
		return nodeList.toArray(new INode[0]);
	}
}
