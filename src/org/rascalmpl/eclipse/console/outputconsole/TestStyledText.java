package org.rascalmpl.eclipse.console.outputconsole;

import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TestStyledText extends Composite {


	StyledText t;
	int i;
	class PrintStuff implements Runnable{
		PrintStream content;
		
		public PrintStuff(CircularByteArray content) {
			this.content = new PrintStream(content);
		}

		@Override
		public void run() {
			int i = 0;
			for(int j = 0 ; ; j++){
				try {
					content.print("Hallo" + i + "\n");
					i++;
					Thread.sleep(2000);
				} catch (InterruptedException e) {
	
				}
				
		}
			
		}
		
	}
	
		TestStyledText(Composite parent) {
			super(parent,SWT.NONE);
			setLayout(new FillLayout(SWT.VERTICAL));
			t = new StyledText(this, SWT.MULTI | SWT.LEFT | SWT.H_SCROLL | SWT.V_SCROLL );
			t.setEditable(false);
			CircularByteArray content = new CircularByteArray(40);
			t.setContent(content);
			Thread t = new Thread(new PrintStuff(content));
			t.start();
//			b = new BoxesElement(this, boxesB);
		}
		

		public static void main(String argv[]){
			final Display display = new Display ();
			final Shell shell = new Shell (display, SWT.SHELL_TRIM);
			shell.setLayout(new FillLayout ());
			shell.setSize (1000, 800);
			new TestStyledText(shell);
			shell.pack();
			shell.open ();
			while (!shell.isDisposed ()) {
				if (display != null && !display.readAndDispatch ())
					display.sleep ();
			}
			display.dispose ();
		}



	
}
