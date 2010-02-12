module Reconstruct

import Node;
import Relation;
import Set;
import List;

public alias vertex = str;
public alias lvertex = tuple[vertex id, int label];

private alias lnode = tuple[int label, node nd];

public node reconstruct(list[lvertex] pp) {
	return reconstruct(pp,0,0).nd;
}

private lnode reconstruct(list[lvertex] pp, int rootPos, int depth) {
	list[node] children = [];

	lvertex root = pp[rootPos];

	int i = rootPos+1;
	while(i <= root.label + depth) {
	 	lnode child = reconstruct(pp, i, depth+1);
		children += child.nd;

		i = child.label + (depth + 1) + 1; // + 1 depth for the subtree 
	}	

	lnode result = <root.label, makeNode(root.id, children)>; 
	return result; 
}

public node reconstruct(list[vertex] pre, list[vertex] post) {
	int length = size(pre)-1;
	return makeNode(pre[0], reconstructForest(slice(pre, 1, length), slice(post, 0, length)));
}

private list[node] reconstructForest(list[vertex] pre, list[vertex] post) {
	list[node] children = [];
	int i = 0;
	while(i < size(pre)) {
		vertex child = pre[i];
		int length = 0;
		while(child != post[i + (length)]) {
			length += 1;
		} 
		children += makeNode(child, reconstructForest(slice(pre, i+1, length), slice(post, i, length)));
		i += length +1;  
	}

	return children;
}
	

public node reconstruct(list[vertex] pre, rel[vertex, vertex] parent) {
	return reconstruct(pre, parent, pre[0]);
}	

private node reconstruct(list[vertex] pre, rel[vertex, vertex] parent, vertex root) {
	set[vertex] uo_children = domain(rangeR(parent, {root}));
	list[node] kids = [];
	for (vertex o_child <- pre) {
		if (o_child in uo_children) {
			if (isEmpty(rangeR(parent, {o_child}))) {
				kids += makeNode(o_child);
			} else {
				kids += reconstruct(pre, parent, o_child);
			}		
		}
	}

	return makeNode(root, kids);
}



