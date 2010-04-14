package org.rascalmpl.eclipse.library.viz;

import java.util.Stack;
import java.util.StringTokenizer;

import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class BoxViewer {

	static Color keyColor =   getColor(SWT.COLOR_RED);
	static Color textColor =   getColor(SWT.COLOR_BLACK);
	static Color numColor =   getColor(SWT.COLOR_BLUE);
	
	static private final String CONSOLE_NAME="BoxView";
	
	private static Color getColor(final int which) {
		Display display = Display.getCurrent();
		if (display != null)
			return display.getSystemColor(which);
		display = Display.getDefault();
		final Color result[] = new Color[1];
		display.syncExec(new Runnable() {
			public void run() {
				synchronized (result) {
					result[0] = Display.getCurrent().getSystemColor(which);					
				}
			}
		});
		synchronized (result) {
			return result[0];
		}
	}

	
	static private MessageConsole findConsole(String name) {
	      ConsolePlugin plugin = ConsolePlugin.getDefault();
	      IConsoleManager conMan = plugin.getConsoleManager();
	      IConsole[] existing = conMan.getConsoles();
	      for (int i = 0; i < existing.length; i++)
	         if (name.equals(existing[i].getName()))
	            return (MessageConsole) existing[i];
	      //no console found, so create a new one
	      // Display.getCurrent().asyncExec(runnable);
	      MessageConsole myConsole = new MessageConsole(name, null);
	      conMan.addConsoles(new IConsole[]{myConsole});
	      return myConsole;
	   }
	
	 static void print(MessageConsole myConsole, IValue v) {
		 final Stack<MessageConsoleStream> stack = new Stack<MessageConsoleStream>();
		 MessageConsoleStream out = myConsole.newMessageStream();
		 out.setColor(textColor);
		 IList rules = (IList) v;
		 StringBuffer b = new StringBuffer();
		 for (int i=0;i<rules.length();i++) {
			 b.append(((IString) rules.get(i)).getValue());
			 b.append("\n");
		 }
		 StringTokenizer t = new StringTokenizer(b.toString(), "\n\b", true);
		 while (t.hasMoreTokens()) {
	         String c = t.nextToken();
	         if (c.equals("\n")) {out.println();}
	         else if (c.equals("\b")) {
	        	 c = t.nextToken();
	        	 if (c.charAt(0)=='{') {
	        		 String key = c.substring(1, 3);
	        		 if (key.equals("bf")) {
	        			 stack.push(out);
	        			 out = myConsole.newMessageStream();
	        			 out.setFontStyle(SWT.BOLD);
	        			 out.print(c.substring(3));
	        		 }
	        		 if (key.equals("it")) {
	        			 stack.push(out);
	        			 out = myConsole.newMessageStream();
	        			 out.setFontStyle(SWT.ITALIC);
	        			 out.print(c.substring(3));
	        		 }
	        		 if (key.equals("nm")) {
	        			 stack.push(out);
	        			 out = myConsole.newMessageStream();
	        			 out.setColor(numColor);
	        			 out.print(c.substring(3));
	        		 }
	        	 } else if (c.charAt(0)=='}') {
                         out = stack.pop();
                         if (c.length()>3) out.print(c.substring(3));
	        	 }
	         }
	         else {
	        	 out.print(c);
	         }
	     }

	 }
	
     static void display(IValue v) {
    	   MessageConsole myConsole = findConsole(CONSOLE_NAME);
    	   myConsole.clearConsole();
   	       print(myConsole, v);
//		   MessageConsoleStream out = myConsole.newMessageStream();
////		   final org.eclipse.swt.graphics.Color keyColor = PlatformUI.getWorkbench().getDisplay().getSystemColor(
////					SWT.COLOR_RED);
//		   Color keyColor =   getColor(SWT.COLOR_RED);
//		   out.setColor(keyColor);
//		   out.setFontStyle(SWT.BOLD); 
//		   out.print("Hello from Generic console sample action:");
//	       out = myConsole.newMessageStream();
//		   // out.setColor(textColor);
//		   out.setFontStyle(SWT.ITALIC);
//		   out.println("Dag Bert");
     }
}
