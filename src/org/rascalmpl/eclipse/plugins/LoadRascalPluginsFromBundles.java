package org.rascalmpl.eclipse.plugins;

import io.usethesource.impulse.language.ILanguageRegistrar;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;

public class LoadRascalPluginsFromBundles implements ILanguageRegistrar {
  @Override
  public void registerLanguages() {
    ProjectEvaluatorFactory.getInstance().loadInstalledRascalLibraryPlugins();
  }
}
