package org.rascalmpl.eclipse.console.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TestText {
	
	
	Text text;
	
	TestText(Composite parent){
		text = new Text(parent, SWT.MULTI | SWT.LEFT | SWT.H_SCROLL | SWT.V_SCROLL| SWT.READ_ONLY);
		text.setTextLimit(1000);
		for(int i = 0 ; i < 100000 ; i++){
//			text.
			text.append("Jada " + i + " \n");
		}
	}

	public static void main(String[] argv){
		System.out.printf("%d\n",Text.LIMIT);
//		final Display display = new Display ();
//		final Shell shell = new Shell (display, SWT.SHELL_TRIM);
//		shell.setLayout(new FillLayout ());
//		shell.setSize (1000, 800);
//		new TestText(shell);
//		shell.pack();
//		shell.open ();
//		
//		while (!shell.isDisposed ()) {
//			if (display != null && !display.readAndDispatch ())
//				display.sleep ();
//		}
//		display.dispose ();
	}

}
