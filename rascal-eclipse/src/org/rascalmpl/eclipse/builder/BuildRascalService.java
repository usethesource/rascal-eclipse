package org.rascalmpl.eclipse.builder;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.ISourceLocation;

public interface BuildRascalService {
	IList compile(IList files, IConstructor pcfg);
	IList compileAll(ISourceLocation folder, IConstructor pcfg);
}
