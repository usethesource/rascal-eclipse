package org.rascalmpl.eclipse.builder;

import java.util.concurrent.CompletableFuture;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.ISourceLocation;

public interface BuildRascalService {
	CompletableFuture<IList> compile(IList files, IConstructor pcfg);
	CompletableFuture<IList> compileAll(ISourceLocation folder, IConstructor pcfg);
}
