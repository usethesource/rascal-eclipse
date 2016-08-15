package org.rascalmpl.eclipse.preferences;

import org.rascalmpl.eclipse.Activator;

import io.usethesource.impulse.preferences.IPreferencesService;

public class RascalPreferences {
  public static final String enableRascalCompiler = "enableRascalCompiler";
  private static IPreferencesService service = Activator.getInstance().getPreferencesService();

  public static boolean isRascalCompilerEnabled() {
	  return service.getBooleanPreference(IPreferencesService.INSTANCE_LEVEL, enableRascalCompiler);
  }
}
