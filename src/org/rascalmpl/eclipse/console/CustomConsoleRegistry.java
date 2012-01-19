package org.rascalmpl.eclipse.console;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.rascalmpl.eclipse.console.internal.RascalIOConsole;
import org.rascalmpl.interpreter.result.ICallableValue;

public class CustomConsoleRegistry {
	
	private final IConsoleManager fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
	private final Map<String, IConsole> consoles = new HashMap<String, IConsole>();
	
	static private class InstanceKeeper {
		public static CustomConsoleRegistry sInstance = new CustomConsoleRegistry();
	}
	
	public static CustomConsoleRegistry getInstance() {
		return InstanceKeeper.sInstance;
	}
	
	private CustomConsoleRegistry() { }

	public void registerConsole(String name, String startText, TypeFactory tf, IValueFactory vf, ICallableValue newLineCallback) {
		if (consoles.containsKey(name)) {
			synchronized (consoles) {
				IConsole oldConsole = consoles.get(name);
				if (oldConsole != null) {
					fConsoleManager.removeConsoles(new IConsole[] { oldConsole});
				}
			}
		}
		IConsole newConsole = new RascalIOConsole(name, startText, tf, vf, newLineCallback);
		synchronized (consoles) {
			consoles.put(name, newConsole);
			fConsoleManager.addConsoles(new IConsole[] { newConsole });
		}
	}
	
}
