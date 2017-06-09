package org.rascalmpl.eclipse.plugins;

import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.preferences.RascalPreferences;

import io.usethesource.impulse.language.ILanguageRegistrar;

public class LoadRascalPluginsFromBundles implements ILanguageRegistrar {
  @Override
  public void registerLanguages() {
      if (RascalPreferences.loadInterpretedLanguagesFromBundles()) {
          ProjectEvaluatorFactory.getInstance().loadInstalledRascalLibraryPlugins();
      }
  }
}
