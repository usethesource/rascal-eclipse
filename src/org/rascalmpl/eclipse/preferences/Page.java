package org.rascalmpl.eclipse.preferences;

import static org.rascalmpl.eclipse.preferences.RascalPreferences.enableStaticChecker;

import org.eclipse.imp.preferences.ConfigurationPreferencesTab;
import org.eclipse.imp.preferences.IPreferencesService;
import org.eclipse.imp.preferences.PreferencesInitializer;
import org.eclipse.imp.preferences.PreferencesTab;
import org.eclipse.imp.preferences.TabbedPreferencesPage;
import org.eclipse.imp.preferences.fields.BooleanFieldEditor;
import org.eclipse.imp.preferences.fields.FieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.rascalmpl.eclipse.Activator;

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
					IPreferencesService.INSTANCE_LEVEL, enableStaticChecker, "Enable static checker",
					"If checked, all changed or new Rascal files will be checked when a build is triggered.",
					parent,
					true, true,
					false, false,
					false, false,
					true);
			
			return new FieldEditor[] {
					enableStaticFieldEditor
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
