package org.rascalmpl.eclipse.preferences;

import org.eclipse.imp.preferences.IPreferencesService;
import org.rascalmpl.eclipse.Activator;

public class RascalPreferences {
  public static final String enableStaticChecker = "enableStaticChecker";
  private static IPreferencesService service = Activator.getInstance().getPreferencesService();

  public static boolean isStaticCheckerEnabled() {
	  return service.getBooleanPreference(enableStaticChecker);
  }
}
