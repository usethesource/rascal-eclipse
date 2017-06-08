package org.rascalmpl.eclipse.preferences;

import static org.rascalmpl.eclipse.preferences.RascalPreferences.enableRascalCompiler;
import static org.rascalmpl.eclipse.preferences.RascalPreferences.bootstrapRascalProject;

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
			BooleanFieldEditor enableStaticFieldEditor = fPrefUtils.makeNewBooleanField(
					page, this, fPrefService,
					IPreferencesService.INSTANCE_LEVEL, enableRascalCompiler, "Enable Rascal Compiler",
					"If checked, all changed or new Rascal files will be checked when a build is triggered.",
					parent,
					true, true,
					false, false,
					false, false,
					true);
			
			BooleanFieldEditor enableBootstrap = fPrefUtils.makeNewBooleanField(
                    page, this, fPrefService,
                    IPreferencesService.INSTANCE_LEVEL, bootstrapRascalProject, "Enable Rascal Compiler",
                    "If checked, and the rascal compiler option is also checked, then the rascal compiler will compile the rascal project itself.",
                    parent,
                    true, true,
                    false, false,
                    false, false,
                    true);
			
			return new FieldEditor[] {
					enableStaticFieldEditor,
					enableBootstrap
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
