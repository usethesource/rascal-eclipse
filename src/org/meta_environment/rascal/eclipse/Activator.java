package org.meta_environment.rascal.eclipse;

import org.eclipse.imp.runtime.PluginBase;

public class Activator extends PluginBase {
	public static final String kPluginID = "rascal_eclipse";
	public static final String kLanguageName = "Rascal";

	private static class InstanceKeeper {
		static Activator sInstance = new Activator();
	}
	
	public static Activator getInstance() {
		return InstanceKeeper.sInstance;
	}

	public String getID() {
		return kPluginID;
	}

	@Override
	public String getLanguageID() {
		return kLanguageName;
	}
}
