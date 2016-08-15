package org.rascalmpl.eclipse.plugins;

import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;

import io.usethesource.impulse.language.ILanguageRegistrar;

public class LoadRascalPluginsFromBundles implements ILanguageRegistrar {
  @Override
  public void registerLanguages() {
    ProjectEvaluatorFactory.getInstance().loadInstalledRascalLibraryPlugins();
  }
}
