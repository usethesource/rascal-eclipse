package org.rascalmpl.eclipse.preferences;

import org.eclipse.imp.preferences.IPreferencesService;
import org.eclipse.imp.preferences.InstancePreferencesTab;
import org.eclipse.imp.preferences.PreferencesInitializer;
import org.eclipse.imp.preferences.PreferencesTab;
import org.eclipse.imp.preferences.TabbedPreferencesPage;
import org.eclipse.imp.preferences.fields.BooleanFieldEditor;
import org.eclipse.imp.preferences.fields.FieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.rascalmpl.eclipse.Activator;
import static org.rascalmpl.eclipse.preferences.RascalPreferences.enableStaticChecker;

public class Page extends TabbedPreferencesPage {
	private static class Tab extends InstancePreferencesTab {
		public Tab() {
			super(Activator.getInstance().getPreferencesService(), false);
		}

		@Override
		protected FieldEditor[] createFields(TabbedPreferencesPage page,
				Composite parent) {
			BooleanFieldEditor enableStaticFieldEditor = new BooleanFieldEditor(page, this, fPrefService, fLevel, enableStaticChecker, "Enable static checker", parent);
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
