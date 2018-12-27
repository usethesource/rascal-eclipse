package org.rascalmpl.eclipse.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.rascalmpl.eclipse.Activator;

public class BackgroundInitializer {
	public static <T> Future<T> construct(String name, Callable<T> generate) {
		FutureTask<T> result = new FutureTask<>(() -> {
			try {
                return generate.call();
			} catch (Throwable e) {
				Activator.log("Cannot initialize " + name, e);
				return null;
			}
		});
		Thread background = new Thread(result);
		background.setDaemon(true);
		background.setName("Background initializer for: " + name);
		background.start();
		return result;
	}
}
