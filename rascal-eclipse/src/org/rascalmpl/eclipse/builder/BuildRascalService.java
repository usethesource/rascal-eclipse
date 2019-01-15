package org.rascalmpl.eclipse.builder;

import java.util.concurrent.FutureTask;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.ISourceLocation;

public interface BuildRascalService {
	FutureTask<IList> compile(IList files, IConstructor pcfg);
	FutureTask<IList> compileAll(ISourceLocation folder, IConstructor pcfg);
}
