package org.rascalmpl.eclipse.plugins;

import org.eclipse.ui.IStartup;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;

public class LoadRascalPluginsFromBundles implements IStartup {

  @Override
  public void earlyStartup() {
    ProjectEvaluatorFactory.getInstance().loadInstalledRascalLibraryPlugins();
  }

}
