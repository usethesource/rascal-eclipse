package org.rascalmpl.eclipse.editor;

import org.rascalmpl.library.lang.rascal.boot.IKernel;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.INode;
import io.usethesource.vallang.IString;

public interface IDESummaryService {
	IConstructor calculate(IKernel kernel, IString moduleName, IConstructor pcfg);
	INode getOutline(IKernel kernel, IConstructor moduleTree);
}