package org.rascalmpl.eclipse.editor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.pdb.facts.INode;
import org.eclipse.imp.services.ICompareNodeIdentifier;

public class ParseTreeCompareNodeIdentifier implements ICompareNodeIdentifier {

	@Override
	public int getTypeCode(Object o) {
		Assert.isLegal(o instanceof ModelTreeNode || o instanceof INode, 
				"The ParseTreeCompareNodeIdentifier must be used with a ModelTreeNode or an INode.");
		if (o instanceof ModelTreeNode){
			ModelTreeNode node = (ModelTreeNode) o;
			System.out.println("getTypeCode:");
			System.out.println(node.getASTNode());
			System.out.println("-------------");
			return 0;
		} else {
			INode node = (INode) o;
			System.out.println("getTypeCode:");
			System.out.println(node);
			System.out.println("-------------");
			return node.getType().hashCode();
		}
	}

	@Override
	public String getID(Object o) {
		Assert.isLegal(o instanceof ModelTreeNode || o instanceof INode, 
				"The ParseTreeCompareNodeIdentifier must be used with a ModelTreeNode or an INode.");
		if (o instanceof ModelTreeNode){
			ModelTreeNode node = (ModelTreeNode) o;
			System.out.println("getID:");
			System.out.println(node.getASTNode());
			System.out.println("-------------");
			return "ROOT";
		} else {
			INode node = (INode) o;
			System.out.println("getTypeCode:");
			System.out.println(node);
			System.out.println("-------------");
			return node.getName();
		}
 	}

}
