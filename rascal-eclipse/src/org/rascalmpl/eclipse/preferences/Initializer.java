package org.rascalmpl.eclipse.preferences;

import org.rascalmpl.eclipse.Activator;

import io.usethesource.impulse.preferences.IPreferencesService;
import io.usethesource.impulse.preferences.PreferencesInitializer;

public class Initializer extends PreferencesInitializer {
	@Override
	public void initializeDefaultPreferences() {
		IPreferencesService service = Activator.getInstance().getPreferencesService();
		service.setBooleanPreference(IPreferencesService.DEFAULT_LEVEL, RascalPreferences.enableRascalCompiler, true);
		service.setBooleanPreference(IPreferencesService.DEFAULT_LEVEL, RascalPreferences.bootstrapRascalProject, false);
	}

	@Override
	public void clearPreferencesOnLevel(String level) {
		IPreferencesService service = Activator.getInstance().getPreferencesService();
		service.clearPreferenceAtLevel(IPreferencesService.DEFAULT_LEVEL, RascalPreferences.enableRascalCompiler);
		service.clearPreferenceAtLevel(IPreferencesService.DEFAULT_LEVEL, RascalPreferences.bootstrapRascalProject);
	}
}
