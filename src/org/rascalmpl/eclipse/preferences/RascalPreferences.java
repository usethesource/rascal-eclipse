package org.rascalmpl.eclipse.preferences;

import io.usethesource.impulse.preferences.IPreferencesService;
import org.rascalmpl.eclipse.Activator;

public class RascalPreferences {
  public static final String enableStaticChecker = "enableStaticChecker";
  private static IPreferencesService service = Activator.getInstance().getPreferencesService();

  public static boolean isStaticCheckerEnabled() {
	  return service.getBooleanPreference(IPreferencesService.INSTANCE_LEVEL, enableStaticChecker);
  }
}
