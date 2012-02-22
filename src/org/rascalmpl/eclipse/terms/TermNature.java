package org.rascalmpl.eclipse.terms;

import org.eclipse.imp.builder.ProjectNatureBase;
import org.eclipse.imp.runtime.IPluginLog;
import org.rascalmpl.eclipse.Activator;

public class TermNature extends ProjectNatureBase {
	public static final String BUILDER_ID = "rascal_eclipse.term_builder";
	public static final String NATURE_ID = "rascal_eclipse.term_nature";

	@Override
	public String getNatureID() {
		return NATURE_ID;
	}

	@Override
	public String getBuilderID() {
		return BUILDER_ID;
	}

	@Override
	public IPluginLog getLog() {
		return Activator.getInstance();
	}

	@Override
	protected void refreshPrefs() {
		// don't know
	}
}
