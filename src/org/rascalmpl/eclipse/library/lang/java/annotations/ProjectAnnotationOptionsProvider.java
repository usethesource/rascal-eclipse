package org.rascalmpl.eclipse.library.lang.java.annotations;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic.Kind;

import org.rascalmpl.library.lang.java.annotations.AnnotationsOptionsProvider;

public class ProjectAnnotationOptionsProvider implements AnnotationsOptionsProvider {

	@Override
	public void init(ProcessingEnvironment processingEnv) {
		processingEnv.getMessager().printMessage(Kind.NOTE, processingEnv.getClass().getName());
	}
	
	@Override
	public Iterable<? extends File> getSourcePath() {
		return Collections.<File>emptyList();
	}

	@Override
	public Iterable<? extends File> getClassPath() {
		return Collections.<File>emptyList();
	}

	@Override
	public Iterable<? extends File> getRascalPath() {
		return Collections.<File>emptyList();
	}

	@Override
	public Map<String, String> getOptions() {
		return Collections.<String,String>emptyMap();
	}
}
