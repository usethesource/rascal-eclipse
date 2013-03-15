package org.rascalmpl.eclipse.plugins;

import org.eclipse.imp.language.ILanguageRegistrar;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;

public class LoadRascalPluginsFromBundles implements ILanguageRegistrar {
  @Override
  public void registerLanguages() {
    ProjectEvaluatorFactory.getInstance().loadInstalledRascalLibraryPlugins();
  }
}
