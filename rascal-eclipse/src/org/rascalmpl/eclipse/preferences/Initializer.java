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
		service.setBooleanPreference(IPreferencesService.DEFAULT_LEVEL, RascalPreferences.loadInterpretedLanguagesFromBundles, true);
		service.setBooleanPreference(IPreferencesService.DEFAULT_LEVEL, RascalPreferences.loadInterpretedLanguagesFromProjects, true);
	}

	@Override
	public void clearPreferencesOnLevel(String level) {
		IPreferencesService service = Activator.getInstance().getPreferencesService();
		service.clearPreferenceAtLevel(IPreferencesService.DEFAULT_LEVEL, RascalPreferences.enableRascalCompiler);
		service.clearPreferenceAtLevel(IPreferencesService.DEFAULT_LEVEL, RascalPreferences.bootstrapRascalProject);
		service.clearPreferenceAtLevel(IPreferencesService.DEFAULT_LEVEL, RascalPreferences.loadInterpretedLanguagesFromBundles);
		service.clearPreferenceAtLevel(IPreferencesService.DEFAULT_LEVEL, RascalPreferences.loadInterpretedLanguagesFromProjects);
	}
}
