package org.rascalmpl.eclipse.preferences;

import static org.rascalmpl.eclipse.preferences.RascalPreferences.bootstrapRascalProject;
import static org.rascalmpl.eclipse.preferences.RascalPreferences.enableConceptCompiler;
import static org.rascalmpl.eclipse.preferences.RascalPreferences.enableRascalCompiler;
import static org.rascalmpl.eclipse.preferences.RascalPreferences.liveConceptPreview;
import static org.rascalmpl.eclipse.preferences.RascalPreferences.loadInterpretedLanguagesFromBundles;
import static org.rascalmpl.eclipse.preferences.RascalPreferences.loadInterpretedLanguagesFromProjects;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.rascalmpl.eclipse.Activator;

import io.usethesource.impulse.preferences.ConfigurationPreferencesTab;
import io.usethesource.impulse.preferences.IPreferencesService;
import io.usethesource.impulse.preferences.PreferencesInitializer;
import io.usethesource.impulse.preferences.PreferencesTab;
import io.usethesource.impulse.preferences.TabbedPreferencesPage;
import io.usethesource.impulse.preferences.fields.BooleanFieldEditor;
import io.usethesource.impulse.preferences.fields.FieldEditor;

public class Page extends TabbedPreferencesPage {
	private static class Tab extends ConfigurationPreferencesTab {
		public Tab() {
			super(Activator.getInstance().getPreferencesService(), true);
		}

		@Override
		protected FieldEditor[] createFields(TabbedPreferencesPage page,
				Composite parent) {
			@SuppressWarnings("deprecation")
			BooleanFieldEditor enableCompilerFieldEditor = fPrefUtils.makeNewBooleanField(
					page, this, fPrefService,
					IPreferencesService.INSTANCE_LEVEL, enableRascalCompiler, "Enable Rascal Compiler",
					"If checked, all changed or new Rascal files will be checked when a build is triggered.",
					parent,
					true, true,
					false, false,
					false, false,
					true);
			
			@SuppressWarnings("deprecation")
			BooleanFieldEditor enableBootstrap = fPrefUtils.makeNewBooleanField(
                    page, this, fPrefService,
                    IPreferencesService.INSTANCE_LEVEL, bootstrapRascalProject, "Enable Bootstrapping of Rascal project",
                    "If checked, and the rascal compiler option is also checked, then the rascal compiler will compile the rascal project itself.",
                    parent,
                    true, true,
                    false, false,
                    false, false,
                    true);
			
			@SuppressWarnings("deprecation")
            BooleanFieldEditor loadInterpretedLanguages1 = fPrefUtils.makeNewBooleanField(
                    page, this, fPrefService,
                    IPreferencesService.INSTANCE_LEVEL, loadInterpretedLanguagesFromBundles, "At Eclipse startup time or first use, register languages found in installed plugin bundles",
                    "If checked, the start-up procedure will load language implementations which are bundled into Eclipse plugins at start-up time",
                    parent,
                    true, true,
                    false, false,
                    false, false,
                    true);
			
			@SuppressWarnings("deprecation")
            BooleanFieldEditor loadInterpretedLanguages2 = fPrefUtils.makeNewBooleanField(
                    page, this, fPrefService,
                    IPreferencesService.INSTANCE_LEVEL, loadInterpretedLanguagesFromProjects, "At Eclipse startup time or first use, register languages found in open workspace projects",
                    "If checked, the start-up procedure will load language implementations which are present in open workspace projects at start-up time",
                    parent,
                    true, true,
                    false, false,
                    false, false,
                    true);
			
			@SuppressWarnings("deprecation")
            BooleanFieldEditor enableConceptCompilerField = fPrefUtils.makeNewBooleanField(
                    page, this, fPrefService,
                    IPreferencesService.INSTANCE_LEVEL, enableConceptCompiler, "The experimental concept compiler can be used to improve the quality of the concept files", "",
                    parent,
                    true, true,
                    false, false,
                    false, false,
                    true);
			
			@SuppressWarnings("deprecation")
            BooleanFieldEditor enableLivePreviewField = fPrefUtils.makeNewBooleanField(
                    page, this, fPrefService,
                    IPreferencesService.INSTANCE_LEVEL, liveConceptPreview, "When editing concept files, one at a time, this flag will produce a html preview", "",
                    parent,
                    true, true,
                    false, false,
                    false, false,
                    true);
			
			return new FieldEditor[] {
					enableCompilerFieldEditor,
					enableBootstrap,
					loadInterpretedLanguages1,
					loadInterpretedLanguages2,
					enableConceptCompilerField,
					enableLivePreviewField
			};
		}
	}

	@Override
	protected PreferencesTab[] createTabs(IPreferencesService prefService, TabbedPreferencesPage page, TabFolder tabFolder) {
		Tab tab = new Tab();
		tab.createTabContents(page, tabFolder);
		return new PreferencesTab[] {tab};
	}
	
	@Override
	public PreferencesInitializer getPreferenceInitializer() {
		return new Initializer();
	}
}
