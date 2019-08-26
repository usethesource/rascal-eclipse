package org.rascalmpl.eclipse.editor;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.INode;
import io.usethesource.vallang.IString;

public interface IDESummaryService {
	IConstructor calculate(IString moduleName, IConstructor pcfg);
	INode getOutline(IConstructor moduleTree);
}