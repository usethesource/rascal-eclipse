package org.rascalmpl.eclipse.preferences;

import org.rascalmpl.eclipse.Activator;

import io.usethesource.impulse.preferences.IPreferencesService;

public class RascalPreferences {
  public static final String enableRascalCompiler = "enableRascalCompiler";
  public static final String bootstrapRascalProject = "bootstrapRascalProject";
  public static final String loadInterpretedLanguagesFromProjects = "loadInterpretedLanguagesFromProjects";
  public static final String loadInterpretedLanguagesFromBundles = "loadInterpretedLanguagesFromBundles";
  public static final String enableConceptCompiler = "enableConceptCompiler";
  public static final String liveConceptPreview = "liveConceptPreview";
  
  private static IPreferencesService service = Activator.getInstance().getPreferencesService();

  public static boolean isRascalCompilerEnabled() {
	  return service.getBooleanPreference(IPreferencesService.INSTANCE_LEVEL, enableRascalCompiler);
  }
  
  public static boolean bootstrapRascalProject() {
      return service.getBooleanPreference(IPreferencesService.INSTANCE_LEVEL, bootstrapRascalProject);
  }
  
  public static boolean loadInterpretedLanguagesFromBundles() {
      return service.getBooleanPreference(IPreferencesService.INSTANCE_LEVEL, loadInterpretedLanguagesFromBundles);
  }
  
  public static boolean loadInterpretedLanguagesFromProjects() {
      return service.getBooleanPreference(IPreferencesService.INSTANCE_LEVEL, loadInterpretedLanguagesFromProjects);
  }
  
  public static boolean conceptCompilerEnabled() {
      return service.getBooleanPreference(IPreferencesService.INSTANCE_LEVEL, enableConceptCompiler);
  }
  
  public static boolean liveConceptPreviewEnabled() {
      return service.getBooleanPreference(IPreferencesService.INSTANCE_LEVEL, liveConceptPreview);
  }
}
